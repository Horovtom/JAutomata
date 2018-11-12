package cz.cvut.fel.horovtom.logic.automata.functional_automata;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.functionals.FunctionalCreator;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LengthDivisibilityTest {
    @Test
    public void testSingleChar() {
        Automaton a = FunctionalCreator.getLengthDivisibilityAutomaton(new char[]{'a'}, 2);
        assertTrue(a != null);
        assertTrue(a.acceptsWord(""));
        assertFalse(a.acceptsWord("a"));
        assertTrue(a.acceptsWord("aa"));
        assertFalse(a.acceptsWord("aaa"));
        assertTrue(a.acceptsWord("aaaa"));
        for (int i = 5; i < 100; i++) {
            StringBuilder sb = new StringBuilder(i);
            for (int i1 = 0; i1 < i; i1++) {
                sb.append('a');
            }
            if (i % 2 == 0) {
                assertTrue("Automaton should accept word: " + sb.toString(), a.acceptsWord(sb.toString()));
            } else {
                assertFalse("Automaton should not accept word: " + sb.toString(), a.acceptsWord(sb.toString()));
            }
        }

        a = FunctionalCreator.getLengthDivisibilityAutomaton(new char[]{'a'}, 10);
        assertTrue(a != null);
        assertTrue(a.acceptsWord(""));
        for (int i = 1; i < 1000; i++) {
            StringBuilder sb = new StringBuilder(i);
            for (int i1 = 0; i1 < i; i1++) {
                sb.append('a');
            }
            if (i % 10 == 0)
                assertTrue("Automaton should accept word: " + sb.toString(), a.acceptsWord(sb.toString()));
            else
                assertFalse("Automaton should not accept word: " + sb.toString(), a.acceptsWord(sb.toString()));
        }
    }

    @Test
    public void testTwoCharacters() {
        Automaton a = FunctionalCreator.getLengthDivisibilityAutomaton(new char[]{'a', 'b'}, 3);
        assertTrue(a != null);
        assertTrue(a.acceptsWord(""));
        Random r = new Random();
        for (int i = 1; i < 1000; i++) {
            StringBuilder sb = new StringBuilder(i);
            for (int i1 = 0; i1 < i; i1++) {
                if (r.nextBoolean())
                    sb.append('a');
                else
                    sb.append('b');
            }
            if (i % 3 == 0)
                assertTrue("Automaton should accept word: " + sb.toString(), a.acceptsWord(sb.toString()));
            else
                assertFalse("Automaton should not accept word: " + sb.toString(), a.acceptsWord(sb.toString()));
        }
    }

    @Test
    public void testFiveCharacters() {
        String[] inp = new String[]{"a", "b", "c", "d", "e"};
        Automaton a = FunctionalCreator.getLengthDivisibilityAutomaton(new char[]{'a', 'b', 'c', 'd', 'e'}, 65);
        assertTrue(a != null);
        assertTrue(a.acceptsWord(""));
        Random r = new Random();

        for (int i = 1; i < 7000; i++) {
            StringBuilder sb = new StringBuilder(i);
            String[] word = new String[i];
            for (int i1 = 0; i1 < i; i1++) {
                int ra = r.nextInt(5);
                sb.append(inp[ra]);
                word[i1] = inp[ra];
            }
            if (i % 65 == 0)
                assertTrue("Automaton should accept word: " + sb.toString(), a.acceptsWord(word));
            else
                assertFalse("Automaton should not accept word: " + sb.toString(), a.acceptsWord(word));
        }
    }
}
