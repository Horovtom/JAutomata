package cz.cvut.fel.horovtom.logic.automaton;

import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.logic.samples.Samples;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CopyTest {
    @Test
    public void dfaCopyTest() {
        DFAAutomaton dfa = Samples.getDFA1();
        DFAAutomaton dfa2 = (DFAAutomaton) dfa.copy();
        dfa2.renameState("1", "h");
        assertEquals("DFA2 had wrong state name after renaming!", "h", dfa2.getQ()[0]);
        assertEquals("Copied automaton had wrong state name after renaming of the other!", "1", dfa.getQ()[0]);
        assertTrue(dfa.acceptsWord(new String[]{"a", "b", "a", "a", "b"}) == dfa2.acceptsWord(new String[]{"a", "b", "a", "a", "b"}));
    }

    @Test
    public void nfaCopyTest() {
        //TODO: IMPL

    }

    @Test
    public void enfaCopyTest() {
        //TODO: IMPL
    }

}
