package cz.cvut.fel.horovtom.automata.logic;

import cz.cvut.fel.horovtom.automata.logic.reducers.DFAReducer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class DFAAutomaton extends Automaton {
    private final static Logger LOGGER = Logger.getLogger(DFAAutomaton.class.getName());

    /**
     * Interactive constructor used for console initialization by user
     */
    public DFAAutomaton() {
        this(System.in);
    }

    /**
     * The same as {@link #DFAAutomaton(String[], String[], HashMap, String, String[])}, but it will parse the acceptings string by commans into state names
     *
     * @param Q           State names array
     * @param sigma       Letter names array
     * @param transitions from name to name by name
     * @param initial     name of initial state
     * @param acceptings  names of accepting states separated by commas
     */
    public DFAAutomaton(String[] Q, String[] sigma, HashMap<String, HashMap<String, String>> transitions, String initial, String acceptings) throws InvalidAutomatonDefinitionException {
        initializeQSigma(Q, sigma);
        initializeTransitionsCompact(transitions);
        StringTokenizer st = new StringTokenizer(acceptings, ",");

        ArrayList<String> targs = new ArrayList<>();
        while (st.hasMoreTokens()) {
            targs.add(st.nextToken());
        }

        initializeInitAcc(new String[]{initial}, targs.toArray(new String[]{}));
    }

    @Override
    public ENFAAutomaton getENFA() {
        ENFAAutomaton enfaAutomaton = new ENFAAutomaton(Arrays.copyOf(Q, Q.length), Arrays.copyOf(sigma, sigma.length), getTransitions(), new int[]{initialStates[0]}, Arrays.copyOf(acceptingStates, acceptingStates.length));
        enfaAutomaton.setDescription(description);
        return enfaAutomaton;
    }

    @Override
    public NFAAutomaton getNFA() {
        NFAAutomaton nfaAutomaton = new NFAAutomaton(Arrays.copyOf(Q, Q.length), Arrays.copyOf(sigma, sigma.length), getTransitions(), new int[]{initialStates[0]}, Arrays.copyOf(acceptingStates, acceptingStates.length));
        nfaAutomaton.setDescription(description);
        return nfaAutomaton;
    }

    @Override
    public DFAAutomaton getDFA() {
        return this;
    }

    /**
     * This constructor loads the interactive definition of an automaton from specified input stream
     */
    public DFAAutomaton(InputStream in) {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            System.out.println("Interactive mode\n" +
                    "Now you will be asked to enter specifics for this DFA automaton.");
            LOGGER.info("Asking user to enter state names...");
            System.out.println("Enter state names on a single line, separated by spaces:");

            String line = br.readLine();
            StringTokenizer st = new StringTokenizer(line);
            this.Q = new String[st.countTokens()];
            LOGGER.info("Loaded states:");
            for (int i = 0; i < this.Q.length; i++) {
                this.Q[i] = st.nextToken();
                LOGGER.info(i + ": " + this.Q[i]);
            }
            System.out.println("Loaded " + this.Q.length + " states.\n");


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
            System.out.println("Enter transitions for this automaton. Separate state names by spaces: ");
            System.out.print("state \t");
            for (String s : this.sigma) {
                System.out.print(s + " ");
            }
            System.out.print("\n");
            int i = 0;
            this.transitions = new HashMap<>();
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
                    for (int i1 = 0; i1 < this.sigma.length; i1++) {
                        curr.put(i1, new int[]{getStateIndex(st.nextToken())});
                    }
                }
                i++;
            }
            System.out.println("Specify initial state: ");
            line = br.readLine();
            st = new StringTokenizer(line);
            if (st.countTokens() != 1) {
                LOGGER.warning("More than 1 initial state specified. Getting first one, ignoring rest");
            }
            this.initialStates = new int[]{this.getStateIndex(st.nextToken())};
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
     * Explicit definition of an DFAAutomaton
     *
     * @param Q           States
     * @param sigma       Letters
     * @param transitions from name to name by name
     * @param initial     names of initial states
     * @param accepting   names of accepting states
     */
    public DFAAutomaton(String[] Q, String[] sigma, HashMap<String, HashMap<String, String>> transitions,
                        String initial, String[] accepting) throws InvalidAutomatonDefinitionException {
        initializeQSigma(Q, sigma);
        initializeTransitionsCompact(transitions);
        initializeInitAcc(new String[]{initial}, accepting);
    }

    /**
     * Constructor used for initialization of reduced automaton
     */
    public DFAAutomaton(String[] q, String[] sigma, HashMap<Integer, HashMap<Integer, Integer>> reducedTransitions, int reducedInitial, int[] reducedAccepting) {
        this.Q = q;
        this.sigma = sigma;
        this.transitions = new HashMap<>();
        for (int state = 0; state < q.length; state++) {
            HashMap<Integer, int[]> current = new HashMap<>();
            this.transitions.put(state, current);
            HashMap<Integer, Integer> currentRow = reducedTransitions.get(state);
            for (int letter = 0; letter < sigma.length; letter++) {
                current.put(letter, new int[]{currentRow.get(letter)});
            }
        }
        this.initialStates = new int[]{reducedInitial};
        this.acceptingStates = reducedAccepting;
    }

    @Override
    public DFAAutomaton reduce() {
        //Transfer transitions to DFAReducer format:
        HashMap<Integer, HashMap<Integer, Integer>> formattedTransitions = new HashMap<>();
        for (int s = 0; s < this.Q.length; s++) {
            HashMap<Integer, Integer> row = new HashMap<>();
            formattedTransitions.put(s, row);
            HashMap<Integer, int[]> rowOriginal = this.transitions.get(s);
            for (int letter = 0; letter < this.sigma.length; letter++) {
                row.put(letter, rowOriginal.get(letter)[0]);
            }
        }
        DFAReducer reductor = new DFAReducer(formattedTransitions, this.initialStates[0], this.acceptingStates);
        if (reductor.wasReduced()) {
            return this;
        }
        HashMap<Integer, HashMap<Integer, Integer>> reducedTransitions = reductor.getReducedTransitions();
        int[] reducedAccepting = reductor.getReducedAccepting();
        int reducedInitial = reductor.getReducedInitial();

        String[] q = reductor.getReducedQ();
        String[] sigma = Arrays.copyOf(this.sigma, this.sigma.length);

        DFAAutomaton dfa = new DFAAutomaton(q, sigma,
                reducedTransitions, reducedInitial, reducedAccepting);
        dfa.setDescription(description);
        return dfa;
    }

    @Override
    protected int[] getPossibleTransitions(int state, int letter) {
        return new int[]{transitions.get(state).get(letter)[0]};
    }

    @Override
    public Automaton copy() {
        String[] Q = Arrays.copyOf(this.Q, this.Q.length);
        String[] sigma = Arrays.copyOf(this.sigma, this.sigma.length);
        HashMap<Integer, HashMap<Integer, Integer>> transitions = new HashMap<>();
        for (int state = 0; state < this.Q.length; state++) {
            HashMap<Integer, Integer> curr = new HashMap<>();
            transitions.put(state, curr);
            HashMap<Integer, int[]> currOrig = this.transitions.get(state);
            for (int letter = 0; letter < this.sigma.length; letter++) {
                int[] arr = currOrig.get(letter);
                curr.put(letter, arr[0]);
            }
        }
        int[] accepting = Arrays.copyOf(this.acceptingStates, this.acceptingStates.length);
        int initial = this.initialStates[0];
        DFAAutomaton dfaAutomaton = new DFAAutomaton(Q, sigma, transitions, initial, accepting);
        dfaAutomaton.setDescription(description);
        return dfaAutomaton;
    }

    @Override
    public boolean hasEpsilonTransitions() {
        return false;
    }

    @Override
    public boolean acceptsWord(String[] word) {
        if (this.reduced == null) {
            this.getReduced();
        }

        if (this.reduced == this) {
            int currentState = this.initialStates[0];
            for (String s : word) {
                int index = this.getLetterIndex(s);
                if (index == -1) {
                    LOGGER.warning("Unknown letter passed: " + s);
                    //System.err.println("Unknown letter: " + s);
                    return false;
                }
                currentState = transitions.get(currentState).get(index)[0];
            }
            for (int acceptingState : this.acceptingStates) {
                if (acceptingState == currentState) return true;
            }
            return false;
        } else {
            return this.reduced.acceptsWord(word);
        }
    }
}
