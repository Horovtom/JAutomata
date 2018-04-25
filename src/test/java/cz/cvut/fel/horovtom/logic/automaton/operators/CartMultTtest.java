package cz.cvut.fel.horovtom.logic.automaton.operators;

import cz.cvut.fel.horovtom.logic.Automaton;
import cz.cvut.fel.horovtom.logic.functionals.FunctionalCreator;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CartMultTtest {

    @Test
    public void testDivisibilityAutomata1() {
        Automaton a = FunctionalCreator.getDivisibilityAutomaton(2, 10);
        Automaton b = FunctionalCreator.getDivisibilityAutomaton(3, 10);
        assertTrue(a.acceptsWord("1024"));
        assertFalse(a.acceptsWord("1025"));
        assertTrue(b.acceptsWord("5208"));
        assertFalse(b.acceptsWord("5209"));
        Automaton c = Automaton.getIntersection(a, b);
        assertTrue(c != null);
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            final int i1 = r.nextInt(100000) + 1;
            if (i1 % 6 == 0) {
                assertTrue("Automaton should accept word: " + String.valueOf(i1), c.acceptsWord(String.valueOf(i1)));
            } else {
                assertFalse("Automaton should not accept word: " + String.valueOf(i1), c.acceptsWord(String.valueOf(i1)));
            }
        }
    }

    @Test
    public void testDivisibilityAutomata2() {
        Automaton a = FunctionalCreator.getDivisibilityAutomaton(12, 16);
        Automaton b = FunctionalCreator.getDivisibilityAutomaton(13, 16);
        Automaton c = Automaton.getIntersection(a, b);
        assertTrue(c != null);
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            int random = r.nextInt(100000);
            if (random % 208 == 0) {
                assertTrue("Automaton should accept word: " + Integer.toHexString(random), c.acceptsWord(Integer.toHexString(random)));
            } else {
                assertFalse("Automaton should not accept word: " + Integer.toHexString(random), c.acceptsWord(Integer.toHexString(random)));
            }
        }
    }
}
