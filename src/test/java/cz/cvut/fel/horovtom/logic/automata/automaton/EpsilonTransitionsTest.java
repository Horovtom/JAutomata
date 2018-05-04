package cz.cvut.fel.horovtom.logic.automata.automaton;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class EpsilonTransitionsTest {
    @Test
    public void testHasEpsilonTransitionsDFA() {
        Automaton automaton = AutomatonSamples.DFASamples.startEndSame();
        assertFalse("DFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
        automaton = AutomatonSamples.DFASamples.lolipopNumbers();
        assertFalse("DFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
        automaton = AutomatonSamples.DFASamples.alphaStar();
        assertFalse("DFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
    }

    @Test
    public void testHasEpsilonTransitionsNFA() {
        Automaton automaton = AutomatonSamples.NFASamples.aWa();
        assertFalse("NFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
        automaton = AutomatonSamples.NFASamples.custom1();
        assertFalse("NFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
        automaton = AutomatonSamples.NFASamples.regex1();
        assertFalse("NFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
        automaton = AutomatonSamples.NFASamples.onlyAlpha();
        assertFalse("NFA should not have any epsilon transitions!", automaton.hasEpsilonTransitions());
    }
}
