package cz.cvut.fel.horovtom.logic.automaton;

import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.logic.dfa.DFASamples;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RenamingTest {
    @Test
    public void testDFA() {
        DFAAutomaton dfa = DFASamples.getDFA3();
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
        //TODO: IMPL
    }

    @Test
    public void testENFA() {
        //TODO: IMPL
    }
}
