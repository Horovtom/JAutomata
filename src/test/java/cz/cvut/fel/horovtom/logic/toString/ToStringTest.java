package cz.cvut.fel.horovtom.logic.toString;

import cz.cvut.fel.horovtom.logic.Automaton;
import cz.cvut.fel.horovtom.logic.NFAAutomaton;
import cz.cvut.fel.horovtom.logic.samples.Samples;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ToStringTest {
    @Test
    public void testBordered() {
        Automaton nfa_troy = Samples.getNFA_troy();
        System.out.println(nfa_troy.exportToString().getBorderedPlainText());
        assertEquals("Automaton did not output bordered version correctly!", "+---+---+-----+---+---+---+\n" +
                "|   |   | t   | r | o | y |\n" +
                "+---+---+-----+---+---+---+\n" +
                "| > |0  | 0,1 | 0 | 0 | 0 |\n" +
                "+---+---+-----+---+---+---+\n" +
                "|   |1  | ∅   | 2 | ∅ | ∅ |\n" +
                "+---+---+-----+---+---+---+\n" +
                "|   |2  | ∅   | ∅ | 3 | ∅ |\n" +
                "+---+---+-----+---+---+---+\n" +
                "|   |3  | ∅   | ∅ | ∅ | 4 |\n" +
                "+---+---+-----+---+---+---+\n" +
                "| < |4  | 4   | 4 | 4 | 4 |\n" +
                "+---+---+-----+---+---+---+\n", nfa_troy.exportToString().getBorderedPlainText());
    }

    @Test
    public void testBordered2() {
        NFAAutomaton nfa3 = Samples.getNFA3();
        assertEquals("Automaton did not output boredered version correctly!", "+---+---+---+---+---+\n" +
                "|   |   | a | b | c |\n" +
                "+---+---+---+---+---+\n" +
                "| > |0  | 4 | 1 | 3 |\n" +
                "+---+---+---+---+---+\n" +
                "|   |1  | 2 | ∅ | ∅ |\n" +
                "+---+---+---+---+---+\n" +
                "|   |2  | 4 | 1 | 3 |\n" +
                "+---+---+---+---+---+\n" +
                "|   |3  | 4 | ∅ | 3 |\n" +
                "+---+---+---+---+---+\n" +
                "| < |4  | ∅ | 5 | ∅ |\n" +
                "+---+---+---+---+---+\n" +
                "|   |5  | ∅ | ∅ | 6 |\n" +
                "+---+---+---+---+---+\n" +
                "| < |6  | ∅ | 5 | ∅ |\n" +
                "+---+---+---+---+---+\n", nfa3.exportToString().getBorderedPlainText());
    }

    @Test
    public void testBordered3() {
        NFAAutomaton nfa2 = Samples.getNFA2();
        assertEquals("Automaton did not output bordered version correctly!", "+----+---+---+-----+\n" +
                "|    |   | a | b   |\n" +
                "+----+---+---+-----+\n" +
                "| <> |0  | 1 | 1   |\n" +
                "+----+---+---+-----+\n" +
                "|    |1  | 2 | ∅   |\n" +
                "+----+---+---+-----+\n" +
                "|    |2  | 2 | 2,3 |\n" +
                "+----+---+---+-----+\n" +
                "|    |3  | 4 | 4   |\n" +
                "+----+---+---+-----+\n" +
                "| <  |4  | ∅ | ∅   |\n" +
                "+----+---+---+-----+\n", nfa2.exportToString().getBorderedPlainText());
    }
}
