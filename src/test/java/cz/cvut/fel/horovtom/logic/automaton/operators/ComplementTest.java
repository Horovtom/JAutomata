package cz.cvut.fel.horovtom.logic.automaton.operators;

import cz.cvut.fel.horovtom.logic.Automaton;
import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.logic.samples.Samples;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ComplementTest {
    @Test
    public void test1() {
        Automaton troy = Samples.getNFA_troy();
        Automaton troyComplement = troy.getComplement();
        assertTrue(troyComplement != null);

        for (int j = 0; j < 1000; j++) {
            ArrayList<String> word = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            Random r = new Random();
            for (int i = 0; i < 1000; i++) {
                switch (r.nextInt(4)) {
                    case 0:
                        sb.append("t");
                        word.add("t");
                        break;
                    case 1:
                        sb.append("r");
                        word.add("r");
                        break;
                    case 2:
                        sb.append("o");
                        word.add("o");
                        break;
                    case 3:
                        sb.append("y");
                        word.add("y");
                        break;
                    default:
                        System.err.println("Wrong number passed from random!");
                        assertTrue(false);
                }
            }
            if (sb.indexOf("troy") != -1) {
                assertTrue("Troy automaton should accept this string: " + sb.toString(), troy.acceptsWord(word));
                assertFalse("Troy complement automaton should not accept this string: " + sb.toString(), troyComplement.acceptsWord(word));
            } else {
                assertFalse("Troy automaton should not accept this string: " + sb.toString(), troy.acceptsWord(word));
                assertTrue("Troy complement automaton should accept this string: " + sb.toString(), troyComplement.acceptsWord(word));
            }
        }
    }

    @Test
    public void testEmpty() {
        String[] Q = {"0"};
        String[] sigma = {"a"};
        HashMap<Integer, HashMap<Integer, int[]>> transitions = new HashMap<>();
        ENFAAutomaton a = new ENFAAutomaton(Q, sigma, transitions, new int[]{0}, new int[]{0});
        assertTrue(a != null);
        assertTrue(a.acceptsWord(""));
        for (int i = 1; i < 1000; i++) {
            assertFalse(a.acceptsWord(new String(new char[i]).replace("\0", "a")));
        }
        Automaton b = a.getComplement();
        assertFalse(b.acceptsWord(""));
        for (int i = 1; i < 1000; i++) {
            String word = new String(new char[i]).replace("\0", "a");
            assertTrue("Word should be accepted: " + word, b.acceptsWord(word));
        }
    }

    @Test
    public void test2() {
        DFAAutomaton dfa3 = Samples.getDFA3();
        Automaton complement = dfa3.getComplement();
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            boolean onlyAlphas = true;
            StringBuilder sb = new StringBuilder();
            ArrayList<String> word = new ArrayList<>();
            for (int i1 = 0; i1 < r.nextInt(60) + 1; i1++) {
                if (r.nextInt(10) > 1) {
                    sb.append("\\alpha");
                    word.add("\\alpha");
                } else {
                    sb.append("\\beta");
                    word.add("\\beta");
                    onlyAlphas = false;
                }
            }
            if (onlyAlphas) {
                assertTrue("DFA3 should accept this string: " + sb.toString(), dfa3.acceptsWord(word));
                assertFalse("DFA3 complement should not accept this string: " + sb.toString(), complement.acceptsWord(word));
            } else {
                assertFalse("DFA3 should not accept this string: " + sb.toString(), dfa3.acceptsWord(word));
                assertTrue("DFA3 complement should accept this string: " + sb.toString(), complement.acceptsWord(word));
            }
        }

        assertTrue("DFA3 should accept empty word!", dfa3.acceptsWord(new String[]{}));
        assertFalse("DFA3 complement should not accept empty word!", complement.acceptsWord(new String[]{}));
    }
}
