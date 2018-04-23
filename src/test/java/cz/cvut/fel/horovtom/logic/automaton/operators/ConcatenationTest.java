package cz.cvut.fel.horovtom.logic.automaton.operators;

import cz.cvut.fel.horovtom.logic.Automaton;
import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.logic.samples.Samples;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.*;

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
        assertEquals("Concatenated sigma size should be 5", 5, sigma.length);
        String[] Q = concatenation.getQ();
        assertEquals("Concatenated states size should be the sum of the two sizes!", 9, Q.length);

        Random random = new Random(System.currentTimeMillis());
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

    @Test
    public void testENFA1() {
        ENFAAutomaton enfa1 = Samples.getENFA1();
        Automaton enfa2 = enfa1.copy();
        Automaton concatenation = Automaton.getConcatenation(enfa1, enfa2);

        assertTrue(concatenation != null);
        assertTrue("Automaton should accept 'aa'", concatenation.acceptsWord(new String[]{"a", "a"}));
        assertTrue("Automaton should accept 'ab'", concatenation.acceptsWord(new String[]{"a", "b"}));
        assertTrue("Automaton should accept 'ba'", concatenation.acceptsWord(new String[]{"b", "a"}));
        assertTrue("Automaton should accept 'bb'", concatenation.acceptsWord(new String[]{"b", "b"}));

        ArrayList<String> word;
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 1000; i++) {
            int letters = random.nextInt(50) + 1;
            if (letters != 2) {
                word = new ArrayList<>();
                for (int i1 = 0; i1 < letters; i1++) {
                    if (random.nextBoolean()) {
                        word.add("a");
                    } else {
                        word.add("b");
                    }
                }

                if (concatenation.acceptsWord(word.toArray(new String[]{}))) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : word) {
                        sb.append(s);
                    }
                    assertFalse("Automaton should not accept word: " + sb.toString(), true);
                }
            }
        }

    }

    @Test
    public void testENFA2() {
        ENFAAutomaton a = Samples.getENFA2();
        ENFAAutomaton b = Samples.getENFA3();
        Automaton concatenation = Automaton.getConcatenation(a, b);
        assertEquals(10, concatenation.getQSize());
        assertEquals(4, concatenation.getSigmaSize());

        //(a+b*)a(aa+c+a)

        assertTrue(concatenation.acceptsWord(new String[]{"a", "c"}));
        assertTrue(concatenation.acceptsWord(new String[]{"a", "a", "a"}));
        assertTrue(concatenation.acceptsWord(new String[]{"b", "b", "b", "b", "a", "c"}));
        assertTrue(concatenation.acceptsWord(new String[]{"a", "a", "a", "a"}));
        assertTrue(concatenation.acceptsWord(new String[]{"b", "b", "a", "a"}));
        assertTrue(concatenation.acceptsWord(new String[]{"a", "a"}));
        assertTrue(concatenation.acceptsWord(new String[]{"a", "c"}));
        assertFalse(concatenation.acceptsWord(new String[]{"c"}));

        Random random = new Random(System.currentTimeMillis());
        outer:
        for (int i = 0; i < 1000; i++) {
            StringBuilder sb = new StringBuilder();
            ArrayList<String> word = new ArrayList<>();
            int count = random.nextInt(1000) + 1;
            for (int i1 = 0; i1 < count; i1++) {
                int next = random.nextInt(2);
                if (next == 0) {
                    sb.append("a");
                    word.add("a");
                } else if (next == 1) {
                    sb.append("b");
                    word.add("b");
                } else {
                    sb.append("c");
                    word.add("c");
                }
            }
            boolean acceptable = true;
            int j = 0;
            if (count < 2) {
                acceptable = false;
            } else {
                if (word.get(j).equals("a")) {
                    //A start:
                    if (word.get(++j).equals("a")) {
                        if (word.size() == j + 1
                                //It was the a case from L2 and it is ok...
                                ||
                                //It is the aa case from L2 and it is ok...
                                (word.get(++j).equals("a") && word.size() == j + 1)
                                ||
                                //It is the (a + b*)a part, we have L2 left
                                testL2(word, j)) {
                            assertTrue("Automaton should accept word: " + sb.toString(), concatenation.acceptsWord(word.toArray(new String[]{})));
                            continue;
                        } else {
                            acceptable = false;
                        }
                    } else {
                        //We got to L2 prematurely
                        j--;
                    }
                } else if (word.get(j).equals("b")) {
                    j++;
                    if (word.size() <= j) {
                        acceptable = false;
                    } else {
                        while (word.get(j).equals("b")) {
                            j++;
                            if (word.size() <= j) {
                                if (concatenation.acceptsWord(word.toArray(new String[]{}))) {
                                    assertFalse("Automaton should not accept word: " + sb.toString(), true);
                                }
                                continue outer;
                            }
                        }
                        if (!word.get(j++).equals("a")) acceptable = false;
                    }
                }
            }

            if (acceptable && testL2(word, j)) {
                assertTrue("Automaton should accept word: " + sb.toString(), concatenation.acceptsWord(word.toArray(new String[]{})));
            } else {
                assertFalse("Automaton should accept word: " + sb.toString(), concatenation.acceptsWord(word.toArray(new String[]{})));
            }

        }
        Automaton reduced = concatenation.getReduced();
        System.out.println(reduced);
    }

    @Test
    public void test1() {
        DFAAutomaton dfa1 = Samples.getDFA1();
        DFAAutomaton dfa2 = Samples.getDFA2();
        Automaton concatenation = Automaton.getConcatenation(dfa1, dfa2);
        System.out.println(concatenation.getQSize());
        Automaton reduced = concatenation.getReduced();
        System.out.println(reduced.getQSize());
        System.out.println(reduced.getKleene().getReduced());
    }

    private static boolean testL2(ArrayList<String> s, int currIndex) {
        if (s.size() == currIndex + 1) {
            return s.get(currIndex).equals("a") || s.get(currIndex).equals("c");
        } else if (s.size() == currIndex + 2) {
            return s.get(currIndex).equals("a") && s.get(currIndex + 1).equals("a");
        }

        return false;
    }


}
