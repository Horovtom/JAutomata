package cz.cvut.fel.horovtom.logic.automaton;

import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.logic.NFAAutomaton;
import cz.cvut.fel.horovtom.logic.samples.Samples;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RenamingTest {
    @Test
    public void testDFA() {
        DFAAutomaton dfa = Samples.getDFA3();
        dfa.renameLetter("aa", "bb");
        String[] sigma = dfa.getSigma();
        assertEquals("Renaming of non-existing letter changed sigma",
                2, sigma.length);
        assertEquals("Renaming of non-existing letter changed sigma",
                "\\alpha", sigma[0]);
        assertEquals("Renaming of non-existing letter changed sigma",
                "\\beta", sigma[1]);

        dfa.renameState("bb", "aa");
        String[] Q = dfa.getQ();
        assertEquals("Renaming of non-existing state changed Q",
                2, Q.length);
        assertEquals("Renaming of non-existing state changed Q",
                "0", Q[0]);
        assertEquals("Renaming of non-existing state changed Q",
                "1", Q[1]);

        dfa.renameLetter("\\alpha", "\\gamma");
        sigma = dfa.getSigma();
        assertEquals("Renaming letter changed the size of sigma", 2, sigma.length);
        assertEquals("Renaming letter did not change the letter", "\\gamma", sigma[0]);
        assertEquals("Renaming letter did change the other letter", "\\beta", sigma[1]);

        dfa.renameLetter("\\gamma", "\\beta");
        sigma = dfa.getSigma();
        assertEquals("Renaming letter changed the size of sigma", 2, sigma.length);
        assertEquals("Renaming letter to already existing name changed the letter", "\\gamma", sigma[0]);
        assertEquals("Renaming letter to already existing name changed the other letter", "\\beta", sigma[1]);

        dfa.renameState("3", "bye");
        Q = dfa.getQ();
        assertEquals("Renaming state changed the size of Q", 2, Q.length);
        assertEquals("Renaming non-existing state resulted in change of first state", "0", Q[0]);
        assertEquals("Renaming non-existing state resulted in change of second state", "1", Q[1]);

        dfa.renameState("0", "1");
        Q = dfa.getQ();
        assertEquals("Renaming state changed the size of Q", 2, Q.length);
        assertEquals("Renaming state to already existing name changed the first state", "0", Q[0]);
        assertEquals("Renaming state to already existing name changed the second state", "1", Q[1]);

        dfa.renameState("0", "2");
        Q = dfa.getQ();
        assertEquals("Renaming state changed the size of Q", 2, Q.length);
        assertEquals("Renaming state did not change the state name", "2", Q[0]);
        assertEquals("Renaming state did change the second state", "1", Q[1]);
    }

    @Test
    public void testNFA() {
        NFAAutomaton automaton = Samples.getNFA2();
        automaton.renameState("0", "");
        String[] Q = automaton.getQ();
        assertEquals("Changing state name to empty changed the size of Q!", 5, Q.length);
        assertEquals("Changing state name to empty changed the state name!", "0", Q[0]);
        automaton.renameLetter("b", "");
        String[] sigma = automaton.getSigma();
        assertEquals("Changing letter name to empty changed the size of sigma!", 2, sigma.length);
        assertEquals("Changing letter name to empty changed the letter name!", "b", sigma[1]);
        automaton.renameState("1", "a");
        Q = automaton.getQ();
        assertEquals("Changing state name did not actually change the name!", "a", Q[1]);
        assertEquals("Changing state name changed the automaton table in an unpredictable way!",
                "<table>\n" +
                        "\t<tr><td></td><td></td><td>a</td><td>b</td></tr>\n" +
                        "\t<tr><td>&harr;</td><td>0</td><td>a</td><td>a</td></tr>\n" +
                        "\t<tr><td></td><td>a</td><td>2</td><td></td></tr>\n" +
                        "\t<tr><td></td><td>2</td><td>2</td><td>2,3</td></tr>\n" +
                        "\t<tr><td></td><td>3</td><td>4</td><td>4</td></tr>\n" +
                        "\t<tr><td>&larr;</td><td>4</td><td></td><td></td></tr>\n" +
                        "</table>",
                automaton.getAutomatonTableHTML());
        assertTrue(automaton.renameLetter("a", "c"));
        assertEquals("Changing letter name changed the automaton table in an unpredictable way!",
                "<table>\n" +
                        "\t<tr><td></td><td></td><td>c</td><td>b</td></tr>\n" +
                        "\t<tr><td>&harr;</td><td>0</td><td>a</td><td>a</td></tr>\n" +
                        "\t<tr><td></td><td>a</td><td>2</td><td></td></tr>\n" +
                        "\t<tr><td></td><td>2</td><td>2</td><td>2,3</td></tr>\n" +
                        "\t<tr><td></td><td>3</td><td>4</td><td>4</td></tr>\n" +
                        "\t<tr><td>&larr;</td><td>4</td><td></td><td></td></tr>\n" +
                        "</table>",
                automaton.getAutomatonTableHTML());
    }

    @Test
    public void testENFA() {
        ENFAAutomaton automaton = Samples.getENFA1();
        assertFalse(automaton.renameLetter("ε", "d"));
        String[] sigma = automaton.getSigma();
        assertEquals("Changing epsilon letter to other name changed the size of sigma!", 3, sigma.length);
        assertEquals("Changing epsilon letter to other name, changed the letter!", "ε", sigma[0]);
    }
}
