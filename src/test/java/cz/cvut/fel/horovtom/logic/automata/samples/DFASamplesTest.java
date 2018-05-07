package cz.cvut.fel.horovtom.logic.automata.samples;

import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DFASamplesTest {

    @Test
    public void testRegex010w() {
        DFAAutomaton a = AutomatonSamples.DFASamples.regex010w();
        assertTrue(a != null);
        assertTrue(a.acceptsWord("010"));
        Random r = new Random();
        for (int h = 0; h < 1000; h++) {
            int i1 = r.nextInt(100);
            StringBuilder sb = new StringBuilder("010");
            for (int i = 0; i < i1; i++) {
                if (r.nextBoolean()) {
                    sb.append("0");
                } else {
                    sb.append("1");
                }
            }
            assertTrue("Automaton should accept word: " + sb.toString(), a.acceptsWord(sb.toString()));
        }
        assertFalse(a.acceptsWord(""));
        assertFalse(a.acceptsWord("0"));
        assertFalse(a.acceptsWord("1"));
        assertFalse(a.acceptsWord("00"));
        assertFalse(a.acceptsWord("01"));
        assertFalse(a.acceptsWord("10"));
        assertFalse(a.acceptsWord("11"));
        assertFalse(a.acceptsWord("110"));
        assertFalse(a.acceptsWord("101001"));
    }

    @Test
    public void testRegex101w() {
        DFAAutomaton a = AutomatonSamples.DFASamples.regex101w();
        assertTrue(a != null);
        assertTrue(a.acceptsWord("101"));
        Random r = new Random();
        for (int h = 0; h < 1000; h++) {
            int i1 = r.nextInt(100);
            StringBuilder sb = new StringBuilder("101");
            for (int i = 0; i < i1; i++) {
                if (r.nextBoolean()) {
                    sb.append("0");
                } else {
                    sb.append("1");
                }
            }
            assertTrue("Automaton should accept word: " + sb.toString(), a.acceptsWord(sb.toString()));
        }
        assertFalse(a.acceptsWord(""));
        assertFalse(a.acceptsWord("0"));
        assertFalse(a.acceptsWord("1"));
        assertFalse(a.acceptsWord("00"));
        assertFalse(a.acceptsWord("01"));
        assertFalse(a.acceptsWord("10"));
        assertFalse(a.acceptsWord("11"));
        assertFalse(a.acceptsWord("110"));
        assertFalse(a.acceptsWord("010001"));
    }

    @Test
    public void testAtLeastThreeAs() {
        DFAAutomaton a = AutomatonSamples.DFASamples.atLeastThreeAs();
        assertTrue(a != null);
        assertFalse(a.acceptsWord(""));
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            int i1 = r.nextInt(300) + 1;
            StringBuilder sb = new StringBuilder(i1);
            int aCounter = 0;
            for (int i2 = 0; i2 < i1; i2++) {
                if (r.nextInt(8) == 1) {
                    aCounter++;
                    sb.append('a');
                } else {
                    sb.append('b');
                }
            }
            if (aCounter >= 3)
                assertTrue("Automaton should accept word: " + sb.toString(), a.acceptsWord(sb.toString()));
            else
                assertFalse("Automaton should not accept word: " + sb.toString(), a.acceptsWord(sb.toString()));

        }
    }

    @Test
    public void testContainsAAA() {
        DFAAutomaton a = AutomatonSamples.DFASamples.containsAAA();
        assertTrue(a != null);
        assertFalse(a.acceptsWord(""));
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            int i1 = r.nextInt(300) + 1;
            StringBuilder sb = new StringBuilder(i1);
            int aCounter = 0;
            for (int i2 = 0; i2 < i1; i2++) {
                if (r.nextBoolean()) {
                    aCounter++;
                    sb.append('a');
                } else {
                    sb.append('b');
                    if (aCounter < 3) aCounter = 0;
                }
            }
            if (aCounter >= 3)
                assertTrue("Automaton should accept word: " + sb.toString(), a.acceptsWord(sb.toString()));
            else
                assertFalse("Automaton should not accept word: " + sb.toString(), a.acceptsWord(sb.toString()));

        }
    }
}
