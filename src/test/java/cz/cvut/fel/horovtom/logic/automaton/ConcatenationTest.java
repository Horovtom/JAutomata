package cz.cvut.fel.horovtom.logic.automaton;

import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.logic.abstracts.Automaton;
import cz.cvut.fel.horovtom.logic.samples.Samples;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConcatenationTest {

    @Test
    public void testDFASimple() {
        /*
            Q1: ab*

                a,b
            >,0,2,1
             ,1,1,1
            <,2,2,2
         */
        String[] Q = new String[]{"0", "1", "2"};
        String[] sigma = new String[]{"a", "b"};
        String initial = "0";
        String[] accepting = new String[]{"2"};
        HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
        HashMap<String, String> current = new HashMap<>();
        current.put("a", "2");
        current.put("b", "1");
        transitions.put("0", current);
        current = new HashMap<>();
        current.put("a", "1");
        current.put("b", "1");
        transitions.put("1", current);
        current = new HashMap<>();
        current.put("a", "2");
        current.put("b", "2");
        transitions.put("2", current);

        Automaton dfa1 = new DFAAutomaton(Q, sigma, transitions, initial, accepting);

        /*
            Q2: bb*a(a+b)*

                a,b
            >,0,1,2
             ,1,1,1
             ,2,3,2
            <,3,3,3
         */

        Q = new String[]{"0", "1", "2", "3"};
        accepting = new String[]{"3"};
        transitions = new HashMap<>();
        current = new HashMap<>();
        current.put("a", "1");
        current.put("b", "2");
        transitions.put("0", current);
        current = new HashMap<>();
        current.put("a", "1");
        current.put("b", "1");
        transitions.put("1", current);
        current = new HashMap<>();
        current.put("a", "3");
        current.put("b", "2");
        transitions.put("2", current);
        current = new HashMap<>();
        current.put("a", "3");
        current.put("b", "3");
        transitions.put("3", current);
        DFAAutomaton dfa2 = new DFAAutomaton(Q, sigma, transitions, initial, accepting);
        assertTrue(dfa2.getQ() != Q);

        Automaton concatenation = Automaton.getConcatenation(dfa1, dfa2);
        assertTrue(concatenation != null);

        assertTrue("Concatenated automaton should accept 'abbbbbabbbb'", concatenation.acceptsWord(new String[]{
                "a", "b", "b", "b", "b", "b", "a", "b", "b", "b", "b"}));
        assertFalse("Concatenated automaton should not accept 'aaa'", concatenation.acceptsWord(new String[]{
                "a", "a", "a"
        }));
        assertFalse("Concatenated automaton should not accept 'abbbb'", concatenation.acceptsWord(new String[]{
                "a", "b", "b", "b", "b"
        }));
        assertTrue("Concatenated automaton should accept 'aaaba'", concatenation.acceptsWord(new String[]{
                "a", "a", "a", "b", "a"
        }));
    }

    /**
     * Tests concatenation on two automatons with different alphabets
     */
    @Test
    public void testDFAComplicated() {

        //L = {w | w starts and ends with the same character}
        DFAAutomaton dfa1 = Samples.getDFA1();
        //L = {w | w = \\alpha*
        DFAAutomaton dfa2 = Samples.getDFA3();

        Automaton concatenation = Automaton.getConcatenation(dfa1, dfa2);
        assertTrue(concatenation != null);
        String[] sigma = concatenation.getSigma();
        assertEquals("Concatenated sigma size should be the sum of the two sizes!", 4, sigma.length);
        String[] Q = concatenation.getQ();
        assertEquals("Concatenated states size should be the sum of the two sizes!", 9, Q.length);

        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            boolean valid = true;
            int length1 = random.nextInt(130) + 2;
            int length2 = random.nextInt(60) + 1;
            String[] in = new String[length1 + length2];

            int character = random.nextInt(2) + 1;
            if (character == 1) {
                in[0] = "a";
            } else {
                in[0] = "b";
            }
            for (int i1 = 1; i1 < length1 - 1; i1++) {
                in[i1] = random.nextInt(2) + 1 == 1 ? "a" : "b";
            }
            int otherCharacter = random.nextInt(2) + 1;
            if (otherCharacter == 1)
                in[length1 - 1] = "a";
            else
                in[length1 - 1] = "b";
            if (otherCharacter != character) valid = false;

            for (int i1 = 0; i1 < length2; i1++) {
                if (random.nextInt(2) + 1 == 1) {
                    in[i1 + length1] = "\\alpha";
                } else {
                    in[i1 + length1] = "\\beta";
                    valid = false;
                }
            }

            if (valid) {
                assertTrue("Automaton should accept word: " + Arrays.toString(in), concatenation.acceptsWord(in));
            } else {
                assertFalse("Automaton should not accept word: " + Arrays.toString(in), concatenation.acceptsWord(in));
            }
        }

        assertFalse("Automaton should not accept word that has invalid characters", concatenation.acceptsWord(new String[]{"a", "b", "b", "a", "c"}));
    }
}
