package cz.cvut.fel.horovtom.logic;

import cz.cvut.fel.horovtom.logic.abstracts.Automaton;
import cz.cvut.fel.horovtom.logic.reducers.NFAReducer;

import java.util.Arrays;

public class NFAAutomaton extends Automaton {
    @Override
    public DFAAutomaton reduce() {
        NFAReducer reducer = new NFAReducer(this.Q, this.sigma, this.transitions, this.initialStates, this.acceptingStates);

        //TODO: COMPLETE
        return null;
    }

    @Override
    protected int[] getPossibleTransitions(int state, int letter) {
        int[] transitions = this.transitions.get(state).get(letter);
        return Arrays.copyOf(transitions, transitions.length);
    }
}
