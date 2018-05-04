package cz.cvut.fel.horovtom.logic.automata.automaton.operators;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.*;

public class KleenyTest {
    @Test
    public void kleeny1() {
        ENFAAutomaton enfa = AutomatonSamples.ENFASamples.oneLetter();
        Automaton kleeny = enfa.getKleene();
        assertEquals("Kleeny automaton should have the same number of states as the original", 4, kleeny.getQSize());
        assertEquals("Kleeny should have the same number of letters with epsilon transition as the original", 3, kleeny.getSigmaSize());

        //Original accepted {a, b}, kleeny accepts {a, b}+
        assertFalse("Automaton should not accept empty word ", kleeny.acceptsWord(new String[0]));
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 1000; i++) {
            StringBuilder sb = new StringBuilder();
            int length = random.nextInt(1000) + 1;
            String[] word = new String[length];
            for (int i1 = 0; i1 < length; i1++) {
                word[i1] = random.nextBoolean() ? "a" : "b";
                sb.append(word[i1]);
            }
            assertTrue("Automaton should accept word: " + sb.toString(), kleeny.acceptsWord(word));
        }
        DFAAutomaton reduced = kleeny.getReduced();
        assertEquals("Reduced automaton should have only 2 states", 2, reduced.getQSize());
    }

    @Test
    public void kleeny2() {
        ENFAAutomaton aab = AutomatonSamples.ENFASamples.regex2();
        Automaton kleeny = aab.getKleene();
        assertFalse(kleeny.acceptsWord(new String[0]));
        assertTrue(kleeny.acceptsWord(new String[]{"a", "a", "b"}));
        assertTrue(kleeny.acceptsWord(new String[]{"a", "a", "b", "a", "a", "b", "a", "a", "b"}));
        assertFalse(kleeny.acceptsWord(new String[]{"a", "b", "a"}));
        assertFalse(kleeny.acceptsWord(new String[]{"a", "a", "b", "a"}));

        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 1000; i++) {
            StringBuilder sb = new StringBuilder();
            ArrayList<String> word = new ArrayList<>();
            int length = random.nextInt(1000) + 1;
            boolean lastB = true, secToLastB = false, valid = true;
            for (int i1 = 0; i1 < length; i1++) {
                if (random.nextBoolean()) {
                    word.add("a");
                    sb.append("a");
                    if (valid) {
                        if (lastB) {
                            lastB = false;
                            secToLastB = true;
                        } else {
                            if (secToLastB) {
                                lastB = false;
                                secToLastB = false;
                            } else {
                                valid = false;
                            }
                        }
                    }
                } else {
                    word.add("b");
                    sb.append("b");
                    if (valid) {
                        if (lastB || secToLastB) {
                            valid = false;
                        }
                        lastB = true;
                    }
                }
            }
            valid = valid && length % 3 == 0;

            if (valid) {
                assertTrue("Automaton should accept word: " + sb.toString(), kleeny.acceptsWord(word.toArray(new String[]{})));
            } else {
                assertFalse("Automaton should not accept word: " + sb.toString(), kleeny.acceptsWord(word.toArray(new String[]{})));
            }
        }
    }

}
