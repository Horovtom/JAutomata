package cz.cvut.fel.horovtom.logic;

import cz.cvut.fel.horovtom.logic.abstracts.Automaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

//TODO: IMPLEMENT
public class ENFAAutomaton extends Automaton {
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
     * Used for copy constructor
     */
    private ENFAAutomaton(String[] q, String[] sigma, HashMap<Integer, HashMap<Integer, int[]>> transitions, int[] initialStates, int[] acceptingStates) {
        this.Q = Arrays.copyOf(q, q.length);
        this.sigma = Arrays.copyOf(sigma, sigma.length);
        this.initialStates = Arrays.copyOf(initialStates, initialStates.length);
        this.acceptingStates = Arrays.copyOf(acceptingStates, acceptingStates.length);
        this.transitions = new HashMap<>();
        for (int s = 0; s < q.length; s++) {
            HashMap<Integer, int[]> curr = new HashMap<>();
            HashMap<Integer, int[]> currentRow = transitions.get(s);
            for (int l = 0; l < sigma.length; l++) {
                int[] currentTransitions = currentRow.get(l);
                curr.put(l, Arrays.copyOf(currentTransitions, currentTransitions.length));
            }
            this.transitions.put(s, curr);
        }
    }

    @Override
    public DFAAutomaton reduce() {

        return null;
    }

    @Override
    protected int[] getPossibleTransitions(int state, int letter) {
        int[] closure = getEpsilonClosure(state);
        TreeSet<Integer> returning = new TreeSet<>();
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
        return this.sigma[0].equals("\\epsilon") || this.sigma[0].equals("ε");
    }
}
