package cz.cvut.fel.horovtom.logic.dfa;

import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.logic.Automaton;
import cz.cvut.fel.horovtom.logic.samples.Samples;
import org.junit.Test;

import java.util.HashMap;

import static cz.cvut.fel.horovtom.logic.dfa.DFADefinitionTest.testWords;
import static org.junit.Assert.*;

public class DFAReduceTest {
    @Test
    public void testReductionDFA1() {
        DFAAutomaton dfa = Samples.getDFA1();
        dfa = dfa.reduce();
        assertEquals("Reduction did not remove all unnessesary states!", 5, dfa.getQSize());
        assertEquals("Reduction removed letters, which it shouldn't!", 2, dfa.getSigmaSize());
        assertTrue("Reduction changed the outcome of accepts!", dfa.acceptsWord(new String[]{"a", "a", "a", "b", "a", "b", "b", "a"}));
        assertFalse("Reduction changed the outcome of accepts!", dfa.acceptsWord(new String[]{"a", "a", "a", "b", "a", "b", "b", "b"}));
        assertTrue("Reduction changed the outcome of accepts!", dfa.acceptsWord(new String[]{"b", "b", "b", "a", "a", "b"}));
        assertFalse("Reduction changed the outcome of accepts!", dfa.acceptsWord(new String[]{"b", "a", "a", "a", "a"}));
    }

    @Test
    public void testSampleDFA() {
        String[] states = new String[]{"S", "A", "B", "C"};
        String[] letters = new String[]{"a", "b"};

        HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
        HashMap<String, String> a = new HashMap<>();
        a.put("a", "A");
        a.put("b", "A");
        transitions.put("S", a);
        a = new HashMap<>();
        a.put("a", "A");
        a.put("b", "B");
        transitions.put("A", a);
        a = new HashMap<>();
        a.put("a", "C");
        a.put("b", "S");
        transitions.put("B", a);
        a = new HashMap<>();
        a.put("a", "A");
        a.put("b", "C");
        transitions.put("C", a);
        String initial = "A";
        String[] accepting = new String[]{"S", "C"};
        Automaton dfa = new DFAAutomaton(states, letters, transitions, initial, accepting);
        dfa = dfa.getReduced();
        testWords(dfa);
    }

}
