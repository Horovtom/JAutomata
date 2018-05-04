package cz.cvut.fel.horovtom.logic.automata.automaton;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.NFAAutomaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import static org.junit.Assert.*;

public class CopyTest {
    @Test
    public void dfaCopyTest() {
        DFAAutomaton dfa = AutomatonSamples.DFASamples.startEndSame();
        DFAAutomaton dfa2 = (DFAAutomaton) dfa.copy();
        dfa2.renameState("1", "h");
        assertEquals("DFA2 had wrong state name after renaming!", "h", dfa2.getQ()[0]);
        assertEquals("Copied automaton had wrong state name after renaming of the other!", "1", dfa.getQ()[0]);
        assertTrue(dfa.acceptsWord(new String[]{"a", "b", "a", "a", "b"}) == dfa2.acceptsWord(new String[]{"a", "b", "a", "a", "b"}));
    }

    @Test
    public void nfaCopyTest() {
        NFAAutomaton nfa = AutomatonSamples.NFASamples.aWa();
        Automaton copy = nfa.copy();
        String description = copy.getDescription();
        assertEquals("Description was not copied correctly!", copy.getDescription(), nfa.getDescription());
        nfa.setDescription("Ahoj");
        assertEquals("Description was not dereferenced!", copy.getDescription(), description);
        copy.renameLetter("a", "c");
        assertFalse(copy.equals(nfa));
        assertFalse(copy.acceptsWord("ababaaba") == nfa.acceptsWord("ababaaba"));
    }
}
