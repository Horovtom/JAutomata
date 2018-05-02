package cz.cvut.fel.horovtom.logic.automaton.regex;

import cz.cvut.fel.horovtom.logic.Automaton;
import cz.cvut.fel.horovtom.logic.converters.FromRegexConverter;
import cz.cvut.fel.horovtom.logic.samples.Samples;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FromRegexTest {
    @Test
    public void test1() {
        String r = "ab*+ba*";

        Automaton a = FromRegexConverter.getAutomaton(r);
        assertTrue(a != null);
        for (int i = 0; i < 100; i++) {
            assertTrue(a.acceptsWord("a" + new String(new char[i]).replace("\0", "b")));
            assertTrue(a.acceptsWord("b" + new String(new char[i]).replace("\0", "a")));
        }
        assertFalse(a.acceptsWord("aba"));
        assertFalse(a.acceptsWord("bab"));
        assertTrue(Samples.getNFA_01_regex().equals(a));
    }

    @Test
    public void test2() {
        String r = "";

        Automaton a = FromRegexConverter.getAutomaton(r);
        assertTrue(a != null);
        assertTrue(a.acceptsWord(""));
        for (int i = 1; i < 1000; i++) {
            String word = new String(new char[i]).replace("\0", "a");
            assertTrue("Word should be accepted: " + word, a.acceptsWord(word));
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
}
