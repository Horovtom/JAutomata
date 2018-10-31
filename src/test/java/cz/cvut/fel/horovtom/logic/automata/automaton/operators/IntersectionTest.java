package cz.cvut.fel.horovtom.logic.automata.automaton.operators;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.functionals.FunctionalCreator;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IntersectionTest {

    @Test
    public void testDivisibilityAutomata3() {
        Automaton a = FunctionalCreator.getDivisibilityAutomaton(2, 16);
        Automaton b = FunctionalCreator.getDivisibilityAutomaton(3, 7);
        Automaton c = Automaton.getIntersection(a, b);
        assertTrue(c.acceptsWord("6"));
        assertFalse(c.acceptsWord("FA"));
        assertFalse(c.acceptsWord("9F40"));
        assertFalse(c.acceptsWord("325DA"));
        assertTrue(c.acceptsWord("123216"));
    }

    @Test
    public void test2() {
        String[] Q = new String[]{"a", "b", "c"};
        String[] sigma = new String[]{"0", "1"};
        HashMap<Integer, HashMap<Integer, Integer>> transitions = new HashMap<>();
        HashMap<Integer, Integer> curr = new HashMap<>();
        transitions.put(0, curr);
        curr.put(0, 1);
        curr.put(1, 2);
        curr = new HashMap<>();
        transitions.put(1, curr);
        curr.put(0, 1);
        curr.put(1, 1);
        curr = new HashMap<>();
        transitions.put(2, curr);
        curr.put(0, 0);
        curr.put(1, 0);
        /*
        1((0+1)1)*

        +---+---+---+---+
        |   |   | 0 | 1 |
        +---+---+---+---+
        | > |a  | b | c |
        +---+---+---+---+
        |   |b  | b | b |
        +---+---+---+---+
        | < |c  | a | a |
        +---+---+---+---+
         */
        Automaton a = new DFAAutomaton(Q, sigma, transitions, 0, new int[]{2});

        Q = new String[]{"d", "e"};
        sigma = new String[]{"0", "1"};
        transitions = new HashMap<>();
        curr = new HashMap<>();
        transitions.put(0, curr);
        curr.put(1, 0);
        curr.put(0, 1);
        curr = new HashMap<>();
        transitions.put(1, curr);
        curr.put(0, 1);
        curr.put(1, 1);
        /*
        1*

        +----+---+---+---+
        |    |   | 0 | 1 |
        +----+---+---+---+
        | <> |d  | e | d |
        +----+---+---+---+
        |    |e  | e | e |
        +----+---+---+---+
         */
        Automaton b = new DFAAutomaton(Q, sigma, transitions, 0, new int[]{0});
        Automaton c = Automaton.getIntersection(a, b);

        assertTrue(c.acceptsWord("111"));
        assertFalse(c.acceptsWord("11"));
        assertTrue(c.acceptsWord("1111111"));
        assertFalse(c.acceptsWord(""));
        assertFalse(c.acceptsWord("101"));

    }

    @Test
    public void test1() throws FileNotFoundException, UnsupportedEncodingException {
        //(a*b + b*a)
        Automaton a = AutomatonSamples.DFASamples.regex1();
        //(a*b*)
        Automaton b = AutomatonSamples.DFASamples.regex2();

        Automaton c = Automaton.getIntersection(a, b);
        System.out.println(c.exportToString().getBorderedPlainText());
        assertTrue(c.acceptsWord("aaaaaaab"));
        assertFalse(c.acceptsWord("bbbbbbba"));
        assertFalse(c.acceptsWord("aaaaabbb"));
        assertFalse(c.acceptsWord("aaaabb"));
        assertTrue(a.acceptsWord("a"));
        assertTrue(b.acceptsWord("a"));
        assertTrue(c.acceptsWord("a"));
        assertTrue(c.acceptsWord("b"));
        assertFalse(c.acceptsWord("ba"));
    }

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
            String s = Integer.toHexString(random);
            s = s.toUpperCase();
            if (random % 156 == 0) {
                assertTrue("Automaton should accept word: " + s, c.acceptsWord(s));
            } else {
                assertFalse("Automaton should not accept word: " + s, c.acceptsWord(s));
            }
        }
    }
}
