package cz.cvut.fel.horovtom.logic.automaton;

import cz.cvut.fel.horovtom.logic.Automaton;
import cz.cvut.fel.horovtom.logic.samples.Samples;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class EpsilonTransitionsTest {
    @Test
    public void testHasEpsilonTransitionsDFA() {
        Automaton automaton = Samples.getDFA1();
        assertFalse("DFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
        automaton = Samples.getDFA2();
        assertFalse("DFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
        automaton = Samples.getDFA3();
        assertFalse("DFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
    }

    @Test
    public void testHasEpsilonTransitionsNFA() {
        Automaton automaton = Samples.getNFA1();
        assertFalse("NFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
        automaton = Samples.getNFA2();
        assertFalse("NFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
        automaton = Samples.getNFA3();
        assertFalse("NFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
        automaton = Samples.getNFA4();
        assertFalse("NFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
    }
}
