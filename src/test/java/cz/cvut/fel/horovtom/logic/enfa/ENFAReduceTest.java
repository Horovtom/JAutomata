package cz.cvut.fel.horovtom.logic.enfa;

import cz.cvut.fel.horovtom.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.logic.Automaton;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class ENFAReduceTest {

    @Test
    public void testSimpleENFA() {
        Automaton automaton = Automaton.importFromCSV(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("enfa_(01*+101)*0*1.csv")).getFile()));
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
