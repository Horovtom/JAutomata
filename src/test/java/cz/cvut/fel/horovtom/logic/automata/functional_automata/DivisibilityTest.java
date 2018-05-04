package cz.cvut.fel.horovtom.logic.automata.functional_automata;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.functionals.FunctionalCreator;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DivisibilityTest {
    @Test
    public void testDivisibility1() {
        Automaton a = FunctionalCreator.getDivisibilityAutomaton(2, 2);
        assertTrue(a.acceptsWord("10110010"));
        assertTrue(a.acceptsWord("1011100"));
        assertTrue(a.acceptsWord("111001111101110"));
        assertFalse(a.acceptsWord("1101"));
        assertFalse(a.acceptsWord("11111001"));
        assertFalse(a.acceptsWord("100100100101000111"));
        assertFalse(a.acceptsWord("101111"));
    }

    @Test
    public void testDivisibility2() {
        Automaton a = FunctionalCreator.getDivisibilityAutomaton(3, 5);
        assertFalse(a.acceptsWord("142"));
        assertFalse(a.acceptsWord("4024"));
        assertFalse(a.acceptsWord("3031"));
        assertTrue(a.acceptsWord("30003022"));
        assertFalse(a.acceptsWord("3030001"));
        assertTrue(a.acceptsWord("3234"));
        assertTrue(a.acceptsWord("112244"));
    }

    @Test
    public void testDivisibility3() {
        Automaton a = FunctionalCreator.getDivisibilityAutomaton(7, 16);
        assertTrue(a.acceptsWord("896"));
        assertTrue(a.acceptsWord("E9CF55"));
        assertTrue(a.acceptsWord("372EAAC3"));
        assertFalse(a.acceptsWord("598F8B9"));
        assertFalse(a.acceptsWord("82003A30"));
        assertFalse(a.acceptsWord("E0D48"));
        assertFalse(a.acceptsWord("D97D5DA073"));
    }

    @Test
    public void testDivisibility4() {
        Automaton a = FunctionalCreator.getDivisibilityAutomaton(34, 61);
        assertTrue(a.acceptsWord("UpCpe"));
        assertTrue(a.acceptsWord("UgKbX"));
        assertTrue(a.acceptsWord("2CK"));
        assertFalse(a.acceptsWord("2CX"));
        assertFalse(a.acceptsWord("DvUDBhR0"));
        assertFalse(a.acceptsWord("5w48gK"));
    }
}
