package cz.cvut.fel.horovtom.logic;

import cz.cvut.fel.horovtom.logic.abstracts.Automaton;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class DFAAutomaton extends Automaton {
    private final static Logger LOGGER = Logger.getLogger(DFAAutomaton.class.getName());

    /**
     * Interactive constructor used for console initialization by user
     */
    public DFAAutomaton() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
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
            while(i < this.Q.length) {
                HashMap<Integer, int[]> curr = this.transitions.get(i);
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
            //TODO: IMPLEMENT

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
        throw new NotImplementedException();
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
            currentState = transitions.get(currentState).get(index)[0];
        }
        for (int acceptingState : this.acceptingStates) {
            if (acceptingState == currentState) return true;
        }
        return false;
    }
}
