package cz.cvut.fel.horovtom.logic.automata.samples;

import cz.cvut.fel.horovtom.automata.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ENFASamplesTest {

    @Test
    public void testFactors_aba() {
        ENFAAutomaton a = AutomatonSamples.ENFASamples.factors_aba();
        assertTrue(a != null);
        assertTrue(a.acceptsWord(""));
        assertTrue(a.acceptsWord("a"));
        assertTrue(a.acceptsWord("b"));
        assertTrue(a.acceptsWord("ab"));
        assertTrue(a.acceptsWord("aba"));
        assertTrue(a.acceptsWord("ba"));
        assertFalse(a.acceptsWord("aa"));
        assertFalse(a.acceptsWord("bb"));
        assertFalse(a.acceptsWord("abaa"));
        assertFalse(a.acceptsWord("baba"));
        assertFalse(a.acceptsWord("abab"));
    }
}
