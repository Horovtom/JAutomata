package cz.cvut.fel.horovtom.logic;

import cz.cvut.fel.horovtom.logic.abstracts.Automaton;

import java.util.HashMap;

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
        //TODO: IMPL


        return new int[0];
    }

    @Override
    public Automaton copy() {
        //TODO: IMPL

        return null;
    }
}
