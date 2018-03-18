package cz.cvut.fel.horovtom.logic.automaton;

import cz.cvut.fel.horovtom.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.logic.abstracts.Automaton;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImportExportTest {
    @Test
    public void testImportCSVDFA() {
        Automaton automaton = Automaton.importFromCSV(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("dfa.csv")).getFile()));
        assertTrue("Automaton did not import correctly", automaton != null && automaton instanceof ENFAAutomaton);
        assertEquals("Automaton had wrong number of states", 4, automaton.getQSize());
        assertEquals("Automaton had wrong number of letters", 3, automaton.getSigmaSize());
        assertEquals("Automaton import had wrong result",
                "            ambiente bellethorne callea  \n" +
                        " >andartes  andartes submarine   ERTEPLE \n" +
                        "< submarine GHETTO   submarine   ERTEPLE \n" +
                        "  ERTEPLE   ERTEPLE  submarine   GHETTO  \n" +
                        "< GHETTO    ERTEPLE  GHETTO      GHETTO  "
                , automaton.getAutomatonTablePlainText());

    }

    @Test
    public void testImportCSVNFA() {
        Automaton automaton = Automaton.importFromCSV(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("nfa.csv")).getFile()));
        assertTrue("Automaton did not import correctly", automaton != null && automaton instanceof ENFAAutomaton);
        assertEquals("Automaton had wrong number of states", 3, automaton.getQSize());
        assertEquals("Automaton had wrong number of letters", 2, automaton.getSigmaSize());
        assertEquals("Automaton import had wrong result",
                "    a     b   \n" +
                        "<>A A,B,C     \n" +
                        " >B A     C   \n" +
                        "  C A     B,A "
                , automaton.getAutomatonTablePlainText());

    }

    @Test
    public void testImportCSVENFA() {
        Automaton automaton = Automaton.importFromCSV(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("enfa.csv")).getFile()));
        assertTrue("Automaton did not import correctly", automaton != null && automaton instanceof ENFAAutomaton);
        assertEquals("Automaton had wrong number of states", 3, automaton.getQSize());
        assertEquals("Automaton had wrong number of letters", 3, automaton.getSigmaSize());
        assertEquals("Automaton import had wrong result",
                "    ε a     b   \n" +
                        "<>A C A,B,C     \n" +
                        " >B   A     C   \n" +
                        "  C   A     B,A "
                , automaton.getAutomatonTablePlainText());

    }
}
