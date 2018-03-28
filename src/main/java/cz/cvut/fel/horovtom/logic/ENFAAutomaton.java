package cz.cvut.fel.horovtom.logic;

import cz.cvut.fel.horovtom.logic.abstracts.Automaton;

import java.util.HashMap;
import java.util.TreeSet;

//TODO: IMPLEMENT
public class ENFAAutomaton extends Automaton {
    public ENFAAutomaton(String[] strings, String[] strings1, HashMap<String, HashMap<String, String>> transitions, String[] initialString, String[] acceptingString) {
        initializeQSigma(strings, strings1);
        initializeTransitionsCompact(transitions);
        initializeInitAcc(initialString, acceptingString);
    }

    @Override
    public DFAAutomaton reduce() {
        //TODO: IMPL
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
        //TODO: IMPL

        return null;
    }

    @Override
    public boolean hasEpsilonTransitions() {
        return this.sigma[0].equals("\\epsilon");
        //TODO: TEST THIS

    }
}
