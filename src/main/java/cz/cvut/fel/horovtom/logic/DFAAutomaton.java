package cz.cvut.fel.horovtom.logic;

import cz.cvut.fel.horovtom.logic.abstracts.Automaton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
                        String initial, String[] accepting) {
        super(Q, sigma, transitions, new String[]{initial}, accepting);
    }

    @Override
    public DFAAutomaton reduce() {
        //TODO: IMPLEMENT
        throw new UnsupportedOperationException();
    }

    @Override
    protected int[] getPossibleTransitions(int state, int letter) {
        return new int[]{transitions.get(state).get(letter)[0]};
    }

    @Override
    public boolean acceptsWord(String[] word) {
        int currentState = this.initialStates[0];
        for (String s : word) {
            int index = this.getLetterIndex(s);
            if (index == -1) {
                LOGGER.warning("Unknown letter passed: " + s);
                System.err.println("Unknown letter: " + s);
                return false;
            }
            currentState = transitions.get(currentState).get(index)[0];
        }
        for (int acceptingState : this.acceptingStates) {
            if (acceptingState == currentState) return true;
        }
        return false;
    }
}
