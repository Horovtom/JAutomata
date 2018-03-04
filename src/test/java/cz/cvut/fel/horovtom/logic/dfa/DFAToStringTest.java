package cz.cvut.fel.horovtom.logic.dfa;

import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import org.junit.Test;

import static org.junit.Assert.*;

public class DFAToStringTest {
    @Test
    public void testPlainText1() {
        DFAAutomaton dfa = DFASamples.getDFA1();
        assertEquals("DFA1 did not create plain text output correctly!",
                "    a b \n" +
                        "  1 2 1 \n" +
                        "< 2 2 1 \n" +
                        "  3 7 5 \n" +
                        "< 4 7 4 \n" +
                        " >5 2 4 \n" +
                        "< 6 6 3 \n" +
                        "  7 7 4 ",
                dfa.getAutomatonTablePlainText());
    }

    @Test
    public void testPlainText2() {
        DFAAutomaton dfa = DFASamples.getDFA2();
        assertEquals("DFA2 did not create plain text output correctly!",
                "    0.12 -6.38 0 213.002 \n" +
                        " >0 1    0     0 0       \n" +
                        "  1 0    2     0 0       \n" +
                        "  2 3    0     0 0       \n" +
                        "  3 0    0     4 0       \n" +
                        "  4 0    0     0 5       \n" +
                        "  5 0    6     0 0       \n" +
                        "  6 0    0     0 7       \n" +
                        "< 7 7    7     7 7       ",
                dfa.getAutomatonTablePlainText());
    }

    @Test
    public void testPlainText3() {
        DFAAutomaton dfa = DFASamples.getDFA3();
        assertEquals("DFA2 did not create plain text output correctly!",
                "    \\alpha \\beta \n" +
                        "<>0 0      1     \n" +
                        "  1 1      1     ",
                dfa.getAutomatonTablePlainText());
    }

    @Test
    public void testHTML1() {
        DFAAutomaton dfa = DFASamples.getDFA1();
        assertEquals("DFA1 did not output HTML code properly!",
                "<table>\n" +
                        "\t<tr><td></td><td></td><td>a</td><td>b</td></tr>\n" +
                        "\t<tr><td></td><td>1</td><td>2</td><td>1</td></tr>\n" +
                        "\t<tr><td>&larr;</td><td>2</td><td>2</td><td>1</td></tr>\n" +
                        "\t<tr><td></td><td>3</td><td>7</td><td>5</td></tr>\n" +
                        "\t<tr><td>&larr;</td><td>4</td><td>7</td><td>4</td></tr>\n" +
                        "\t<tr><td>&rarr;</td><td>5</td><td>2</td><td>4</td></tr>\n" +
                        "\t<tr><td>&larr;</td><td>6</td><td>6</td><td>3</td></tr>\n" +
                        "\t<tr><td></td><td>7</td><td>7</td><td>4</td></tr>\n" +
                        "</table>",
                dfa.getAutomatonTableHTML());

    }

    @Test
    public void testHTML2() {
        DFAAutomaton dfa = DFASamples.getDFA2();
        assertEquals("DFA2 did not output HTML code properly!",
                "<table>\n" +
                        "\t<tr><td></td><td></td><td>0.12</td><td>-6.38</td><td>0</td><td>213.002</td></tr>\n" +
                        "\t<tr><td>&rarr;</td><td>0</td><td>1</td><td>0</td><td>0</td><td>0</td></tr>\n" +
                        "\t<tr><td></td><td>1</td><td>0</td><td>2</td><td>0</td><td>0</td></tr>\n" +
                        "\t<tr><td></td><td>2</td><td>3</td><td>0</td><td>0</td><td>0</td></tr>\n" +
                        "\t<tr><td></td><td>3</td><td>0</td><td>0</td><td>4</td><td>0</td></tr>\n" +
                        "\t<tr><td></td><td>4</td><td>0</td><td>0</td><td>0</td><td>5</td></tr>\n" +
                        "\t<tr><td></td><td>5</td><td>0</td><td>6</td><td>0</td><td>0</td></tr>\n" +
                        "\t<tr><td></td><td>6</td><td>0</td><td>0</td><td>0</td><td>7</td></tr>\n" +
                        "\t<tr><td>&larr;</td><td>7</td><td>7</td><td>7</td><td>7</td><td>7</td></tr>\n" +
                        "</table>",
                dfa.getAutomatonTableHTML());
    }

    @Test
    public void testHTML3() {
        DFAAutomaton dfa = DFASamples.getDFA3();
        assertEquals("DFA3 did not output HTML code properly!",
                "<table>\n" +
                        "\t<tr><td></td><td></td><td>\\alpha</td><td>\\beta</td></tr>\n" +
                        "\t<tr><td>&harr;</td><td>0</td><td>0</td><td>1</td></tr>\n" +
                        "\t<tr><td></td><td>1</td><td>1</td><td>1</td></tr>\n" +
                        "</table>",
                dfa.getAutomatonTableHTML());
    }
}
