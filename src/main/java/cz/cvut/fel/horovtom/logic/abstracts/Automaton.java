package cz.cvut.fel.horovtom.logic.abstracts;

import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.logging.Logger;

public abstract class Automaton {
    private final static Logger LOGGER = Logger.getLogger(Automaton.class.getName());

    protected Automaton() {
    }

    protected String[] Q, sigma;

    protected int[] initialStates;

    protected int[] acceptingStates;

    protected HashMap<Integer, HashMap<Integer, int[]>> transitions;

    public Automaton(String[] Q, String[] sigma, HashMap<String, HashMap<String, String>> transitions, String[] initials, String[] accepting) {
        HashMap<String, Integer> stringIntStates = new HashMap<>();
        HashMap<Integer, HashMap<Integer, int[]>> trans = new HashMap<>();
        for (int i = 0; i < Q.length; i++) {
            String from = Q[i];
            stringIntStates.put(from, i);
            HashMap<Integer, int[]> curr = new HashMap<>();
            trans.put(i, curr);
            for (int l = 0; l < sigma.length; l++) {
                String by = sigma[l];
                String to = transitions.get(from).get(by);
                if (!stringIntStates.containsKey(to)) {
                    for (int i1 = 1; i1 < Q.length; i1++) {
                        if (Q[i1].equals(to)) {
                            stringIntStates.put(to, i1);
                            break;
                        }
                    }
                }
                curr.put(l, new int[]{stringIntStates.get(to)});
            }
        }

        int[] acc = new int[accepting.length];
        for (int i = 0; i < acc.length; i++) {
            acc[i] = stringIntStates.get(accepting[i]);
        }
        this.Q = Q;
        this.sigma = sigma;
        this.transitions = trans;
        int[] init = new int[initials.length];
        for (int i = 0; i < init.length; i++) {
            init[i] = stringIntStates.get(initials[i]);
        }

        this.initialStates = init;
        this.acceptingStates = acc;
    }

    public int getQSize() {
        return Q.length;
    }

    public int getSigmaSize() {
        return sigma.length;
    }

    /**
     * @param letterName
     * @return -1 if the letter is not in sigma
     */
    protected int getLetterIndex(String letterName) {
        for (int i = 0; i < sigma.length; i++) {
            if (sigma[i].equals(letterName)) return i;
        }
        return -1;
    }

    /**
     * @param stateName
     * @return -1 if stateName is not in Q
     */
    protected int getStateIndex(String stateName) {
        for (int i = 0; i < Q.length; i++) {
            if (Q[i].equals(stateName)) return i;
        }
        return -1;
    }

    /**
     * @return byval copy of Q
     */
    public String[] getQ() {
        return Arrays.copyOf(Q, Q.length);
    }

    /**
     * @return byval copy of sigma
     */
    public String[] getSigma() {
        return Arrays.copyOf(sigma, sigma.length);
    }

    @Override
    public String toString() {
        return "Automaton" + getAutomatonTablePlainText();
    }

    /**
     * @return String containing formatted table as plain text
     */
    public String getAutomatonTablePlainText() {
        //TODO: IMPLEMENT
        throw new NotImplementedException();
    }

    /**
     * @return String containing formatted table as html
     */
    public String getAutomatonTableHTML() {
        //TODO: IMPLEMENT
        throw new NotImplementedException();
    }

    /**
     * @return String containing formatted table as tex code
     */
    public String getAutomatonTableTEX() {
        //TODO: IMPLEMENT
        throw new NotImplementedException();
    }

    /**
     * @return String containing code for TEX package TIKZ. This code draws a diagram of this automaton
     */
    public String getAutomatonTIKZ() {
        //TODO: IMPLEMENT
        throw new NotImplementedException();
    }

    public abstract DFAAutomaton reduce();

    public boolean acceptsWord(String[] word) {
        //TODO: VERBOSE Workflow?
        ArrayList<HashSet<Integer>> possibilities = new ArrayList<>(2);
        possibilities.add(new HashSet<>(this.Q.length));
        possibilities.add(new HashSet<>(this.Q.length));
        int c = 1, n;
        HashSet<Integer> current = possibilities.get(0);
        HashSet<Integer> next;
        //init
        for (int initialState : initialStates) {
            current.add(initialState);
        }

        //Progress
        for (String letter : word) {
            c = (c + 1) % 2;
            n = (c + 1) % 2;
            int letterIndex = getLetterIndex(letter);
            current = possibilities.get(c);
            next = possibilities.get(n);
            for (Integer currentState : current) {
                int[] p = getPossibleTransitions(currentState, letterIndex);
                for (int toAddState : p) {
                    next.add(toAddState);
                }
            }
            if (next.size() == 0) return false;
        }

        //Final eval
        c = (c + 1) % 2;
        current = possibilities.get(c);
        for (Integer integer : current) {
            if (isAcceptingState(integer)) return true;
        }
        return false;
    }

    /**
     * @return true if the specified state belongs to accepting states
     */
    protected boolean isAcceptingState(int stateIndex) {
        for (int acceptingState : acceptingStates) {
            if (acceptingState == stateIndex) return true;
        }
        return false;
    }

    /**
     * This function will return an array of all states, that automaton can end up in after
     * transitioning from specified state by specified letter.
     * <p>
     * For DFA this will be array of only one element, for NFA it might be multiple elements
     */
    protected abstract int[] getPossibleTransitions(int state, int letter);
}
