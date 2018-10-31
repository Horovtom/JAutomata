package cz.cvut.fel.horovtom.logic.automata.automaton.operators;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.functionals.FunctionalCreator;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class UnionTest {
    @Test
    public void testLengthDivisibility() {
        Automaton a = FunctionalCreator.getLengthDivisibilityAutomaton(new char[]{'a', 'b'}, 7);
        Automaton b = FunctionalCreator.getLengthDivisibilityAutomaton(new char[]{'a', 'b'}, 13);
        Automaton union = Automaton.getUnion(a, b);
        assertTrue(union != null);
        assertTrue(a.acceptsWord(""));
        assertTrue(b.acceptsWord(""));
        assertTrue(union.acceptsWord(""));
        assertFalse(a.acceptsWord("aabaab"));
        assertFalse(b.acceptsWord("aabaab"));
        assertFalse(union.acceptsWord("aabaab"));


        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            StringBuilder sb = new StringBuilder(i);
            for (int i1 = 0; i1 < i; i1++) {
                if (r.nextBoolean())
                    sb.append('a');
                else
                    sb.append('b');
            }

            if (i % 7 == 0 || i % 13 == 0)
                assertTrue("Automaton should accept word: " + sb.toString(), union.acceptsWord(sb.toString()));
            else
                assertFalse("Automaton should not accept word: " + sb.toString(), union.acceptsWord(sb.toString()));
        }


    }

    @Test
    public void test1() throws FileNotFoundException, UnsupportedEncodingException {
        DFAAutomaton lolipopAutomaton = AutomatonSamples.DFASamples.lolipop();
        Automaton troyAutomaton = AutomatonSamples.NFASamples.troy();
        Automaton union = Automaton.getUnion(lolipopAutomaton, troyAutomaton);

        assertTrue("Failed to create union, even though it should be possible", union != null);
        Random r = new Random();
        //Both alphabet's at once
        String[] alphabeth = new String[]{"l", "i", "p", "o", "t", "r", "y"};
        for (int i = 0; i < 1000; i++) {
            boolean lolipopValid = true;
            boolean lolipopFound = false;
            boolean troyValid = true;
            boolean troyFound = false;
            int lolipopCounter = 0;
            int troyCounter = 0;

            int length = r.nextInt(1000);
            StringBuilder sb = new StringBuilder();
            ArrayList<String> word = new ArrayList<>();
            for (int i1 = 0; i1 < length; i1++) {
                int curr = r.nextInt(alphabeth.length);
                sb.append(alphabeth[curr]);
                word.add(alphabeth[curr]);

                //If character from the other word appears, it is not valid
                if (curr > 3) lolipopValid = false;
                if (curr < 3) troyValid = false;

                //If we still can find lolipop in the word:
                if (lolipopValid && !lolipopFound) {
                    switch (curr) {
                        case 0:
                            if (lolipopCounter == 0 || lolipopCounter == 2) lolipopCounter++;
                            else lolipopValid = false;
                            break;
                        case 1:
                            if (lolipopCounter == 3) lolipopCounter++;
                            else lolipopValid = false;
                            break;
                        case 2:
                            if (lolipopCounter == 4) lolipopCounter++;
                            else if (lolipopCounter == 6) {
                                lolipopCounter = 7;
                                lolipopFound = true;
                            } else lolipopValid = false;
                            break;
                        case 3:
                            if (lolipopCounter == 1 || lolipopCounter == 5) lolipopCounter++;
                            else lolipopValid = false;
                            break;
                        default:
                            lolipopValid = false;
                    }
                }
                //If we still can find troy in the word
                if (troyValid && !troyFound) {
                    switch (curr) {
                        case 3:
                            if (troyCounter == 2) troyCounter = 3;
                            else troyCounter = 0;
                            break;
                        case 4:
                            troyCounter = 1;
                            break;
                        case 5:
                            if (troyCounter == 1) troyCounter = 2;
                            else troyCounter = 0;
                            break;
                        case 6:
                            if (troyCounter == 3) {
                                troyCounter = 4;
                                troyFound = true;
                            }
                            break;
                        default:
                            troyValid = false;
                    }
                }
            }
            if ((lolipopValid && lolipopFound) || (troyValid && troyFound)) {
                assertTrue("Union should accept the word: " + sb.toString(), union.acceptsWord(word.toArray(new String[]{})));
            } else {
                assertFalse("Union should not accept the word: " + sb.toString(), union.acceptsWord(word.toArray(new String[]{})));
            }
        }

        //Troy alphabeth:
        alphabeth = new String[]{"t", "r", "o", "y"};
        for (int i = 0; i < 1000; i++) {
            int troyCounter = 0;
            boolean troyFound = false;

            int length = r.nextInt(1000);
            StringBuilder sb = new StringBuilder();
            ArrayList<String> word = new ArrayList<>();

            for (int i1 = 0; i1 < length; i1++) {
                int curr = r.nextInt(alphabeth.length);
                sb.append(alphabeth[curr]);
                word.add(alphabeth[curr]);
                if (!troyFound) {
                    switch (curr) {
                        case 0:
                            troyCounter = 1;
                            break;
                        case 1:
                            if (troyCounter == 1) troyCounter = 2;
                            else troyCounter = 0;
                            break;
                        case 2:
                            if (troyCounter == 2) troyCounter = 3;
                            else troyCounter = 0;
                            break;
                        case 3:
                            if (troyCounter == 3) {
                                troyCounter = 4;
                                troyFound = true;
                            } else troyCounter = 0;
                            break;
                    }
                }
            }

            if (troyFound) {
                assertTrue("Union should accept the word: " + sb.toString(), union.acceptsWord(word.toArray(new String[]{})));
            } else {
                assertFalse("Union should not accept the word: " + sb.toString(), union.acceptsWord(word.toArray(new String[]{})));
            }
        }

        //Lolipop alphabeth
        alphabeth = new String[]{"l", "o", "i", "p"};
        for (int i = 0; i < 1000; i++) {
            boolean lolipopFound = false;
            int lolipopCounter = 0;

            int length = r.nextInt(1000);
            StringBuilder sb = new StringBuilder();
            ArrayList<String> word = new ArrayList<>();
            for (int i1 = 0; i1 < length; i1++) {
                int curr = r.nextInt(alphabeth.length);
                sb.append(alphabeth[curr]);
                word.add(alphabeth[curr]);

                if (!lolipopFound) {
                    switch (curr) {
                        case 0:
                            if (lolipopCounter == 0 || lolipopCounter == 2) lolipopCounter++;
                            else lolipopCounter = 1;
                            break;
                        case 2:
                            if (lolipopCounter == 3) lolipopCounter++;
                            else lolipopCounter = 0;
                            break;
                        case 3:
                            if (lolipopCounter == 4) lolipopCounter = 5;
                            else if (lolipopCounter == 6) {
                                lolipopCounter = 7;
                                lolipopFound = true;
                            } else lolipopCounter = 0;
                            break;
                        case 1:
                            if (lolipopCounter == 1 || lolipopCounter == 5) lolipopCounter++;
                            else lolipopCounter = 0;
                            break;
                    }
                }
            }

            if (lolipopFound) {
                assertTrue("Union should accept the word: " + sb.toString(), union.acceptsWord(word.toArray(new String[]{})));
            } else {
                assertFalse("Union should not accept the word: " + sb.toString(), union.acceptsWord(word.toArray(new String[]{})));
            }
        }
    }
}
