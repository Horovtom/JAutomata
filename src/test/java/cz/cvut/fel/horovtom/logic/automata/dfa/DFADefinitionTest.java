package cz.cvut.fel.horovtom.logic.automata.dfa;

import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import cz.cvut.fel.horovtom.automata.tools.Utilities;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DFADefinitionTest {
    private static final Logger LOGGER = Logger.getLogger(DFADefinitionTest.class.getName());

    public static void testWords(Automaton automaton) {
        LOGGER.fine("Testing word: a*bab* should be in L\nActual word generated: \n");

        ArrayList<String> word = new ArrayList<>();
        StringBuilder s = new StringBuilder();
        for (int j = 0; j < Utilities.getRandInt(0, 6000); ++j) {
            word.add("a");
            s.append("a");
        }
        word.add("b");
        s.append("b");
        word.add("a");
        s.append("a");
        for (int k = 0; k < Utilities.getRandInt(0, 6000); ++k) {
            word.add("b");
            s.append("b");
        }

        s.append("\n");
        LOGGER.fine(s.toString());
        assertTrue("Automaton does not accept word a*bab*, which it should", automaton.acceptsWord(word.toArray(new String[0])));

        s = new StringBuilder();
        s.append("Testing word: bbba* should not be in L\nActual word generated: \n");
        ArrayList<String> word2 = new ArrayList<>();
        word2.add("b");
        word2.add("b");
        word2.add("b");
        s.append("bbb");
        for (int i = 0; i < Utilities.getRandInt(0, 6000); ++i) {
            word2.add("a");
            s.append("a");
        }
        s.append("\n");
        LOGGER.fine(s.toString());
        assertFalse("Automaton accepts bbba*, which it should not", automaton.acceptsWord(word2.toArray(new String[0])));

        s = new StringBuilder();
        s.append("Testing word: a*baaa*b should not be in L\nActual word generated: \n");
        ArrayList<String> word3 = new ArrayList<>();
        for (int l = 0; l < Utilities.getRandInt(0, 6000); ++l) {
            word3.add("a");
            s.append("a");
        }
        word3.add("b");
        s.append("b");
        word3.add("a");
        s.append("a");
        word3.add("a");
        s.append("a");
        for (int m = 0; m < Utilities.getRandInt(0, 6000); ++m) {
            word3.add("a");
            s.append("a");
        }
        word3.add("b");
        s.append("b\n");
        LOGGER.fine(s.toString());
        assertFalse("Automaton accepted word a*baaa*b, which it should not", automaton.acceptsWord(word3.toArray(new String[0])));

        s = new StringBuilder();
        s.append("Testing word: abb should be in L\n");
        String[] word4 = new String[]{"a", "b", "b"};
        assertTrue("Automaton does not accept word abb, which it should", automaton.acceptsWord(word4));

        LOGGER.fine("Testing empty word, it should not be in L\n");
        assertFalse("Automaton did not accepted empty word, which it should not", automaton.acceptsWord(new String[0]));

        ArrayList<String> word5 = new ArrayList<>();
        s = new StringBuilder();
        s.append("Testing word: b^(3*i)bab*abb should be in L\nActual word generated: \n");
        for (int r = 0; r < 3 * Utilities.getRandInt(0, 6000); ++r) {
            word5.add("b");
            s.append("b");
        }
        word5.add("b");
        s.append("b");
        word5.add("a");
        s.append("a");
        for (int n = 0; n < Utilities.getRandInt(0, 6000); ++n) {
            word5.add("b");
            s.append("b");
        }
        word5.add("a");
        s.append("a");
        word5.add("b");
        s.append("b");
        word5.add("b");
        s.append("b\n");

        LOGGER.fine(s.toString());
        assertTrue("Automaton did not accept b^(3*i)bab*abb, which it should be accepting", automaton.acceptsWord(word5.toArray(new String[0])));


        String[] word6 = new String[]{"b", "a"};
        LOGGER.fine("Testing word: ba should be in L\n");
        assertTrue("Automaton did not accept ba, which it should be accepting", automaton.acceptsWord(word6));
    }

    @Test
    public void testDefinitionExplicit() {
        //      a   b
        // <S   A   A
        // >A   A   B
        //  B   C   S
        // <C   A   C

        String[] states = new String[]{"S", "A", "B", "C"};
        String[] letters = new String[]{"a", "b"};

        HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
        HashMap<String, String> a = new HashMap<>();
        a.put("a", "A");
        a.put("b", "A");
        transitions.put("S", a);
        a = new HashMap<>();
        a.put("a", "A");
        a.put("b", "B");
        transitions.put("A", a);
        a = new HashMap<>();
        a.put("a", "C");
        a.put("b", "S");
        transitions.put("B", a);
        a = new HashMap<>();
        a.put("a", "A");
        a.put("b", "C");
        transitions.put("C", a);
        String initial = "A";
        String[] accepting = new String[]{"S", "C"};
        Automaton dfa = new DFAAutomaton(states, letters, transitions, initial, accepting);
        testWords(dfa);
    }

    @Test
    public void testDefinitionInteractive() {
        try (InputStream in = this.getClass().getClassLoader().getResource("testDFA1.txt").openStream()) {
            DFAAutomaton dfa = new DFAAutomaton(in);

            assertFalse(dfa.acceptsWord(new String[]{
                    "ambiente", "ambiente", "ambiente", "ambiente", "ambiente", "ambiente", "ambiente"
            }));
            assertTrue(dfa.acceptsWord(new String[]{
                    "bellethorne"
            }));
            assertFalse(dfa.acceptsWord(new String[]{
                    "bellethorne", "bellethorne", "bellethorne", "bellethorne", "bellethorne", "ambiente", "ambiente", "ambiente", "ambiente"
            }));
            assertTrue(dfa.acceptsWord(new String[]{
                    "callea", "callea", "ambiente", "bellethorne"
            }));
            assertTrue(dfa.acceptsWord(new String[]{
                    "bellethorne", "callea", "callea", "callea", "callea", "callea"
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEmptyAutomaton() {
        Automaton automaton = AutomatonSamples.DFASamples.emptyAutomaton();
        assertTrue(automaton.acceptsWord(""));
        assertFalse(automaton.acceptsWord("a"));
        assertFalse(automaton.acceptsWord("b"));
        assertFalse(automaton.acceptsWord("asdddsagtjwwe2r"));
        Automaton complement = automaton.getComplement();
        assertFalse(complement.acceptsWord(""));
    }

}
