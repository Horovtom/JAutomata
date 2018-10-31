package cz.cvut.fel.horovtom.logic.automata.automaton.regex;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.NFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.converters.FromRegexConverter;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FromRegexTest {
    @Test
    public void test1() throws FileNotFoundException, UnsupportedEncodingException {
        String r = "ab*+ba*";

        Automaton a = FromRegexConverter.getAutomaton(r);
        System.out.println(a);
        assertTrue(a != null);
        for (int i = 0; i < 100; i++) {
            assertTrue(a.acceptsWord("a" + new String(new char[i]).replace("\0", "b")));
            assertTrue(a.acceptsWord("b" + new String(new char[i]).replace("\0", "a")));
        }
        assertFalse(a.acceptsWord("aba"));
        assertFalse(a.acceptsWord("bab"));
        assertFalse(a.acceptsWord(""));
        assertTrue(AutomatonSamples.NFASamples.regex2().equals(a));
    }

    @Test
    public void testEmptyString() {
        String r = "";

        Automaton a = FromRegexConverter.getAutomaton(r);
        assertTrue(a != null);
        assertTrue(a.acceptsWord(""));
        for (int i = 1; i < 100; i++) {
            String word = new String(new char[i]).replace("\0", "a");
            assertFalse("Word should not be accepted: " + word, a.acceptsWord(word));
        }
    }


    @Test
    public void test3() {
        String r = "(ab)*b";

        Automaton a = FromRegexConverter.getAutomaton(r);
        assertTrue(a != null);
        for (int i = 0; i < 100; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < i; j++) {
                sb.append("ab");
            }
            sb.append("b");
            assertTrue("Automaton should accept word: " + sb.toString(), a.acceptsWord(sb.toString()));
        }


        assertFalse(a.acceptsWord("ababababababab"));
        assertFalse(a.acceptsWord("aaaaaaa"));
        assertFalse(a.acceptsWord(""));
    }

    @Test
    public void testSingleLetter() {
        String r = "a";

        Automaton a = FromRegexConverter.getAutomaton(r);
        assertTrue(a != null);
        assertTrue(a.acceptsWord("a"));
        assertFalse(a.acceptsWord(""));
        for (int i = 2; i < 100; i++) {
            String word = new String(new char[i]).replace("\0", "a");
            assertFalse("Word should not be accepted: " + word, a.acceptsWord(word));
        }
    }

    @Test
    public void test5() {
        String r = "(ba)*c*a(bc)*";

        Automaton a = FromRegexConverter.getAutomaton(r);
        assertTrue(a != null);
        assertTrue(a.acceptsWord("bababaccca"));
        assertFalse(a.acceptsWord("babababababa"));
        assertFalse(a.acceptsWord("bb"));
        assertFalse(a.acceptsWord("ab"));
        assertFalse(a.acceptsWord(""));

        NFAAutomaton nfaAutomaton = AutomatonSamples.NFASamples.regex1();
        assertTrue(a.equals(nfaAutomaton));
    }

    @Test
    public void testEpsilon() {
        String r = "((ba+ab+ε)b)*";

        Automaton a = FromRegexConverter.getAutomaton(r);
        assertTrue(a != null);
        assertTrue(a.acceptsWord(""));
        assertTrue(a.acceptsWord("bab"));
        assertTrue(a.acceptsWord("babbab"));
        assertTrue(a.acceptsWord("abb"));
        assertTrue(a.acceptsWord("abbabb"));
        assertTrue(a.acceptsWord("b"));
        assertTrue(a.acceptsWord("bb"));
        assertFalse(a.acceptsWord("ba"));
        assertTrue(a.acceptsWord("babb"));
        assertFalse(a.acceptsWord("aababbab"));
    }

    @Test
    public void test010w() throws FileNotFoundException, UnsupportedEncodingException {
        String r = "010(0+1)*";
        Automaton a = FromRegexConverter.getAutomaton(r);
        assertTrue(a != null);
        assertTrue(a.equals(AutomatonSamples.DFASamples.regex010w()));
    }

    @Test
    public void test101w() throws FileNotFoundException, UnsupportedEncodingException {
        String r = "101(0+1)*";
        Automaton a = FromRegexConverter.getAutomaton(r);
        assertTrue(a != null);
        assertTrue(a.equals(AutomatonSamples.DFASamples.regex101w()));

    }

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        String a = AutomatonSamples.DFASamples.regex101w().exportToString().getTIKZ();
        System.out.println(a);
    }
}
