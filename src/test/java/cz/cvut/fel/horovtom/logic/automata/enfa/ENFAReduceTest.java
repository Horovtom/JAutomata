package cz.cvut.fel.horovtom.logic.automata.enfa;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.ENFAAutomaton;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import static org.junit.Assert.*;


public class ENFAReduceTest {

    @Test
    public void testSimpleENFA() throws FileNotFoundException, UnsupportedEncodingException {
        Automaton automaton = null;
        try {
            automaton = Automaton.importFromCSV(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("samples/csv/enfa_01_regex.csv")).getFile()));
        } catch (Automaton.InvalidAutomatonDefinitionException e) {
            fail();
        }
        assertTrue("Automaton did not import correctly", automaton != null && automaton instanceof ENFAAutomaton);
        automaton = automaton.getReduced();
        assertTrue("Automaton did not reduce", automaton != null);
        assertTrue(automaton.acceptsWord(new String[]{"0", "1", "1", "1", "0", "1", "1", "0", "1", "0", "0", "1"}));
        assertFalse(automaton.acceptsWord(new String[]{"0", "1", "1", "1", "1", "1", "1", "1", "1", "1", "0", "0", "0"}));
        assertTrue(automaton.acceptsWord(new String[]{"1"}));
        assertFalse(automaton.acceptsWord(new String[0]));
        assertTrue(automaton.acceptsWord(new String[]{"1", "0", "1", "0", "1", "1", "1", "1"}));
    }

}
