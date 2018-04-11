package cz.cvut.fel.horovtom.logic;

import cz.cvut.fel.horovtom.logic.abstracts.Automaton;
import cz.cvut.fel.horovtom.logic.reducers.NFAReducer;
import cz.cvut.fel.horovtom.tools.Utilities;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

public class NFAAutomaton extends Automaton {
    private static final Logger LOGGER = Logger.getLogger(NFAAutomaton.class.getName());

    /**
     * Interactive constructor used for console initialization by user
     */
    public NFAAutomaton() {
        this(System.in);
    }

    /**
     * This constructor loads the interactive definition of an automaton from specified input stream
     */
    public NFAAutomaton(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            System.out.println("Interactive mode\n" +
                    "Now you will be asked to enter specifics for this DFA automaton.");
            LOGGER.info("Asking user to enter state names...");
            System.out.println("Enter state names on a single line, separated by spaces:");

            //LOAD STATES
            String line = br.readLine();
            StringTokenizer st = new StringTokenizer(line);
            this.Q = new String[st.countTokens()];
            LOGGER.info("Loaded states:");
            for (int i = 0; i < this.Q.length; i++) {
                this.Q[i] = st.nextToken();
                LOGGER.info(i + ": " + this.Q[i]);
            }
            System.out.println("Loaded " + this.Q.length + " states.\n");

            //LOAD LETTERS
            System.out.println("Enter letter names on a single line, separated by spaces: ");
            LOGGER.info("Asking user to enter letter names...");
            line = br.readLine();
            st = new StringTokenizer(line);
            this.sigma = new String[st.countTokens()];
            LOGGER.info("Loaded letters:");
            for (int i = 0; i < this.sigma.length; i++) {
                this.sigma[i] = st.nextToken();
                LOGGER.info(i + ": " + this.sigma[i]);
            }
            System.out.println("Loaded " + this.sigma.length + " letters.\n");

            //LOAD TRANSITIONS
            System.out.println("Enter transitions for this automaton. Separate cells by spaces and states by commas. If nothing is in a cell, mark it by '-'");
            //Print header
            System.out.print("state \t");
            for (String s : this.sigma) {
                System.out.print(s + " ");
            }
            System.out.print("\n");
            int i = 0;
            this.transitions = new HashMap<>();
            outer:
            while (i < this.Q.length) {
                HashMap<Integer, int[]> curr = new HashMap<>();
                this.transitions.put(i, curr);

                System.out.print(this.Q[i] + " \t");
                line = br.readLine();
                st = new StringTokenizer(line);
                if (st.countTokens() != this.sigma.length) {
                    System.err.println("Invalid number of tokens on this line! Try again!");
                    i--;
                } else {
                    //Get the cell
                    for (int letter = 0; letter < sigma.length; letter++) {
                        String tok = st.nextToken();
                        if (tok.equals("-")) {
                            curr.put(letter, new int[0]);
                            continue;
                        }

                        int current = 0;
                        ArrayList<Integer> cell = new ArrayList<>();
                        while (current < tok.length()) {
                            //Get next token from letter
                            Pair<Integer, String> res = Utilities.getNextToken(tok, current, ',');
                            current = res.getKey();
                            if (res.getValue().length() == 0) {
                                break;
                            }
                            int ind = this.getStateIndex(res.getValue());
                            if (ind == -1) {
                                System.err.println("Invalid state name: " + res.getValue());
                                continue outer;
                            }
                            cell.add(ind);
                            if (current < 0) break;
                        }
                        curr.put(letter, cell.stream().mapToInt(a -> a).toArray());
                    }
                }
                i++;
            }

            //GET INITIAL STATES
            System.out.println("Specify initial states on one line separated by spaces: ");
            line = br.readLine();
            st = new StringTokenizer(line);
            if (st.countTokens() == 0) {
                LOGGER.info("0 initial states specified.");
                System.err.println("0 initial states specified.");
            }

            ArrayList<Integer> initials = new ArrayList<>();

            while (st.hasMoreTokens()) {
                String state = st.nextToken();
                int curr = this.getStateIndex(state);
                if (curr == -1) {
                    LOGGER.info("State " + state + " does not exist! Ignoring!");
                    System.err.println("State " + state + " does not exist! Ignoring!");
                    continue;
                }
                initials.add(curr);
            }
            this.initialStates = initials.stream().mapToInt(a -> a).toArray();

            //GET ACCEPTING STATES
            System.out.println("Specify accepting states on one line separated by spaces: ");
            line = br.readLine();
            st = new StringTokenizer(line);
            int ind;
            boolean[] used = new boolean[this.Q.length];
            ArrayList<Integer> accept = new ArrayList<>();
            int count = st.countTokens();
            for (int i1 = 0; i1 < count; i1++) {
                ind = this.getStateIndex(st.nextToken());
                if (!used[ind]) {
                    accept.add(ind);
                    used[ind] = true;
                }
            }
            this.acceptingStates = accept.stream().mapToInt(val -> val).toArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This constructor has array of states as a value in transitions map.
     *
     * @param Q           Array of strings, representing states of the automaton
     * @param sigma       Array of strings, representing letters of the automaton
     * @param initial     Array of strings, containing state names that are considered initial states
     * @param accepting   Array of strings, containing state names that are considered accepting states
     * @param transitions <pre>HashMap< State, < Letter, TargetState[] > ></pre>, where State is the source state, Letter is the transition label and TargetState is the set of targets. <br>
     *                    State &times; Letter &rarr; TargetState[]=P(Q), <br>where P(Q) = {X | X ⊆ Q}
     */
    public NFAAutomaton(String[] Q, String[] sigma, String[] initial, String[] accepting, HashMap<String, HashMap<String, String[]>> transitions) {
        super(Q, sigma, transitions, initial, accepting);
    }

    /**
     * Constructor used to initialize the variables. It is used by children of this abstract class.
     *
     * @param Q           State names of the automaton
     * @param sigma       Letter names of the automaton
     * @param transitions Table of transitions in text form. The value of this map can be empty, or state names separated by commas
     * @param initial     Initial state names of the automaton in text form.
     * @param accepting   Accepting state names of the automaton in text form
     */
    public NFAAutomaton(String[] Q, String[] sigma, HashMap<String, HashMap<String, String>> transitions, String[] initial, String[] accepting) {
        initializeQSigma(Q, sigma);
        initializeTransitionsCompact(transitions);
        initializeInitAcc(initial, accepting);
    }

    /**
     * Constructor used to initialize by reducers
     */
    NFAAutomaton(String[] q, String[] sigma, HashMap<Integer, HashMap<Integer, int[]>> transitions, int[] initialStates, int[] acceptingStates) {
        this.Q = Arrays.copyOf(q, q.length);
        this.sigma = Arrays.copyOf(sigma, sigma.length);
        //Copy transitions
        this.transitions = new HashMap<>();
        for (int i = 0; i < q.length; i++) {
            HashMap<Integer, int[]> curr = new HashMap<>();
            this.transitions.put(i, curr);
            HashMap<Integer, int[]> currRow = transitions.get(i);
            for (int l = 0; l < sigma.length; l++) {
                int[] targets = currRow.get(l);
                curr.put(l, Arrays.copyOf(targets, targets.length));
            }
        }
        this.initialStates = Arrays.copyOf(initialStates, initialStates.length);
        this.acceptingStates = Arrays.copyOf(acceptingStates, acceptingStates.length);
    }

    @Override
    public DFAAutomaton reduce() {
        NFAReducer reducer = new NFAReducer(this.Q, this.sigma, this.transitions, this.initialStates, this.acceptingStates);
        DFAAutomaton dfa = new DFAAutomaton(reducer.getReducedQ(), reducer.getReducedSigma(), reducer.getReducedTransitions(), reducer.getReducedInitial(), reducer.getReducedAccepting());
        dfa = dfa.getReduced();
        return dfa;
    }

    @Override
    protected int[] getPossibleTransitions(int state, int letter) {
        int[] transitions = this.transitions.get(state).get(letter);
        return Arrays.copyOf(transitions, transitions.length);
    }

    @Override
    public Automaton copy() {
        String[] Q = Arrays.copyOf(this.Q, this.Q.length);
        String[] sigma = Arrays.copyOf(this.sigma, this.sigma.length);
        HashMap<Integer, HashMap<Integer, int[]>> transitions = new HashMap<>();
        for (int state = 0; state < this.Q.length; state++) {
            HashMap<Integer, int[]> curr = new HashMap<>();
            transitions.put(state, curr);
            HashMap<Integer, int[]> currOrig = this.transitions.get(state);
            for (int letter = 0; letter < this.sigma.length; letter++) {
                int[] arr = currOrig.get(letter);
                curr.put(letter, Arrays.copyOf(arr, arr.length));
            }
        }
        int[] accepting = Arrays.copyOf(this.acceptingStates, this.acceptingStates.length);
        int[] initial = Arrays.copyOf(this.initialStates, this.initialStates.length);

        return new NFAAutomaton(Q, sigma, transitions, initial, accepting);
    }

    @Override
    public boolean hasEpsilonTransitions() {
        return false;
    }
}
