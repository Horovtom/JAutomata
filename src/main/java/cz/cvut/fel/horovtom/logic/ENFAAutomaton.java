package cz.cvut.fel.horovtom.logic;

import cz.cvut.fel.horovtom.logic.abstracts.Automaton;

import java.util.HashMap;

//TODO: IMPLEMENT
public class ENFAAutomaton extends Automaton {
    public ENFAAutomaton(String[] strings, String[] strings1, HashMap<String, HashMap<String, String>> transitions, String[] initialString, String[] acceptingString) {
        super(strings, strings1, transitions, initialString, acceptingString);
    }

    @Override
    public DFAAutomaton reduce() {
        return null;
    }

    @Override
    protected int[] getPossibleTransitions(int state, int letter) {
        return new int[0];
    }
}
