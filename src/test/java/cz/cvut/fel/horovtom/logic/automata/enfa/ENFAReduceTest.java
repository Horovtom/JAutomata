package cz.cvut.fel.horovtom.logic.automata.enfa;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.ENFAAutomaton;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ENFAReduceTest {

    @Test
    public void testSimpleENFA() {
        Automaton automaton = Automaton.importFromCSV(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("samples/csv/enfa_01_regex.csv")).getFile()));
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
