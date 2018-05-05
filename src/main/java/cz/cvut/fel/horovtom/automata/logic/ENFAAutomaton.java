package cz.cvut.fel.horovtom.automata.logic;

import cz.cvut.fel.horovtom.automata.logic.reducers.ENFAReducer;
import cz.cvut.fel.horovtom.automata.tools.Pair;
import cz.cvut.fel.horovtom.automata.tools.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

public class ENFAAutomaton extends Automaton {
    private static final Logger LOGGER = Logger.getLogger(ENFAAutomaton.class.getName());

    public ENFAAutomaton(String[] strings, String[] strings1, HashMap<String, HashMap<String, String>> transitions, String[] initialString, String[] acceptingString) {
        initializeQSigma(strings, strings1);
        initializeTransitionsCompact(transitions);
        initializeInitAcc(initialString, acceptingString);
    }

    /**
     * This constructor has array of states as a value in transitions map.
     *
     * @param states      Array of strings, representing states of the automaton
     * @param sigma       Array of strings, representing letters of the automaton
     * @param initials    Array of strings, containing state names that are considered initial states
     * @param acceptings  Array of strings, containing state names that are considered accepting states
     * @param transitions <pre>HashMap< State, < Letter, TargetState[] > ></pre>, where State is the source state, Letter is the transition label and TargetState is the set of targets. <br>
     *                    State &times; Letter &rarr; TargetState[]=P(Q), <br>where P(Q) = {X | X ⊆ Q}
     */
    public ENFAAutomaton(String[] states, String[] sigma, String[] initials, String[] acceptings, HashMap<String, HashMap<String, String[]>> transitions) {
        super(states, sigma, transitions, initials, acceptings);
    }

    /**
     * Interactive constructor used for initialization by human
     */
    public ENFAAutomaton() {
        this(System.in);
    }

    @Override
    public ENFAAutomaton getENFA() {
        return this;
    }

    @Override
    public NFAAutomaton getNFA() {
        if (hasEpsilonTransitions()) {
            ENFAReducer reducer = new ENFAReducer(Q, sigma, transitions, initialStates, acceptingStates, 0);
            NFAAutomaton nfaAutomaton = new NFAAutomaton(reducer.getQ(), reducer.getSigma(), reducer.getTransitions(), new int[]{reducer.getInitial()}, reducer.getAccepting());
            nfaAutomaton.setDescription(description);
            return nfaAutomaton;
        }
        NFAAutomaton nfaAutomaton = new NFAAutomaton(Q, sigma, transitions, initialStates, acceptingStates);
        nfaAutomaton.setDescription(description);
        return nfaAutomaton;
    }

    @Override
    public DFAAutomaton getDFA() {
        return getReduced();
    }

    /**
     * This constructor is used for initialization by indices in Q and Sigma
     */
    public ENFAAutomaton(String[] q, String[] sigma, HashMap<Integer, HashMap<Integer, int[]>> transitions, int[] initialStates, int[] acceptingStates) {
        this.Q = Arrays.copyOf(q, q.length);
        this.sigma = Arrays.copyOf(sigma, sigma.length);
        this.initialStates = Arrays.copyOf(initialStates, initialStates.length);
        this.acceptingStates = Arrays.copyOf(acceptingStates, acceptingStates.length);
        this.transitions = Utilities.getCopyOfHashMap(transitions);
        refactorTransitions();
    }

    /**
     * This constructor loads the interactive definition of an automaton from specified input stream
     */
    public ENFAAutomaton(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            System.out.println("Interactive mode\n" +
                    "Now you will be asked to enter specifics for this NFA automaton.");
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
            System.out.println("Enter letter names on a single line, separated by spaces. Use letter 'eps' for marking epsilon transitions: ");
            LOGGER.info("Asking user to enter letter names...");
            line = br.readLine();
            st = new StringTokenizer(line);
            int epsIndex = -1;
            this.sigma = new String[st.countTokens()];
            LOGGER.info("Loaded letters:");
            for (int i = 0; i < this.sigma.length; i++) {
                this.sigma[i] = st.nextToken();
                for (String epsilonName : epsilonNames) {
                    if (epsilonName.equals(this.sigma[i])) {
                        if (epsIndex != -1) {
                            LOGGER.warning("Entered multiple epsilon letters!");
                            System.err.println("Entered multiple epsilon letters!");
                        }
                        epsIndex = i;
                    }
                }
                LOGGER.info(i + ": " + this.sigma[i]);
            }
            System.out.println("Loaded " + this.sigma.length + " letters.\n");
            if (epsIndex == -1) {
                LOGGER.fine("No epsilon transitions");
                System.out.println("Loaded no epsilon transitions");
            }

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

            if (epsIndex != 0 && epsIndex != -1)
                refactorEpsilonStates(epsIndex);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method shifts epsilon letter in current automaton to index 0
     */
    private void refactorEpsilonStates(int epsilon) {
        boolean found = false;
        for (String epsilonName : epsilonNames) {
            if (this.sigma[epsilon].equals(epsilonName)) {
                found = true;
                break;
            }
        }
        if (!found) {
            LOGGER.warning("There was no epsilon letter on index: " + epsilon);
            return;
        }

        HashMap<Integer, HashMap<Integer, int[]>> newTransitions = new HashMap<>(this.transitions.size());
        HashMap<Integer, int[]> current;

        for (int state = 0; state < this.Q.length; state++) {
            current = new HashMap<>();
            newTransitions.put(state, current);
            current.put(0, this.transitions.get(state).get(epsilon));
            int currentLetter = 1;
            for (int letter = 0; letter < this.sigma.length; letter++) {
                if (letter == epsilon) continue;
                current.put(currentLetter++, this.transitions.get(state).get(letter));
            }
        }

        this.transitions = newTransitions;

        String[] newSigma = new String[this.sigma.length];
        newSigma[0] = this.sigma[epsilon];
        int currentLetter = 1;
        for (int i = 0; i < this.sigma.length; i++) {
            if (i == epsilon) continue;
            newSigma[currentLetter++] = this.sigma[i];
        }

        this.sigma = newSigma;
    }

    @Override
    public DFAAutomaton reduce() {

        if (!this.hasEpsilonTransitions()) {
            NFAAutomaton nfa = new NFAAutomaton(this.Q, this.sigma, this.transitions, this.initialStates, this.acceptingStates);
            return nfa.getReduced();
        }

        ENFAReducer reducer = new ENFAReducer(this.Q, this.sigma, this.transitions, this.initialStates, this.acceptingStates, 0);
        NFAAutomaton nfa = new NFAAutomaton(reducer.getQ(), reducer.getSigma(), reducer.getTransitions(), new int[]{reducer.getInitial()}, reducer.getAccepting());
        return nfa.getReduced();
    }

    @Override
    protected int[] getPossibleTransitions(int state, int letter) {
        int[] closure = getEpsilonClosure(state);
        Set<Integer> returning = new HashSet<>();
        for (int i : closure) {
            int[] curr = this.transitions.get(i).get(letter);
            for (int i1 : curr) {
                returning.add(i1);
            }
        }

        return returning.stream().mapToInt(a -> a).toArray();
    }

    @Override
    public Automaton copy() {
        return new ENFAAutomaton(this.Q, this.sigma, this.transitions, this.initialStates, this.acceptingStates);
    }

    @Override
    public boolean hasEpsilonTransitions() {
        if (this.sigma.length > 0) {
            for (String epsilonName : epsilonNames) {
                if (this.sigma[0].equals(epsilonName)) return true;
            }
        }
        return false;
    }
}
