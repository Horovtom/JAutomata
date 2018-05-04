package cz.cvut.fel.horovtom.logic.automata.automaton.toString;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.NFAAutomaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ToStringTest {
    @Test
    public void testBordered() {
        Automaton nfa_troy = AutomatonSamples.NFASamples.troy();
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
        NFAAutomaton nfa3 = AutomatonSamples.NFASamples.regex1();
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
        NFAAutomaton nfa2 = AutomatonSamples.NFASamples.custom1();
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


    @Test
    public void testPlainText1() {
        DFAAutomaton dfa = AutomatonSamples.DFASamples.startEndSame();
        assertEquals("DFA1 did not create plain text output correctly!",
                "    a b \n" +
                        "  1 2 1 \n" +
                        "< 2 2 1 \n" +
                        "  3 7 5 \n" +
                        "< 4 7 4 \n" +
                        " >5 2 4 \n" +
                        "< 6 6 3 \n" +
                        "  7 7 4 ",
                dfa.exportToString().getPlainText());
    }

    @Test
    public void testRenaming() {
        DFAAutomaton dfa = AutomatonSamples.DFASamples.startEndSame();
        assertEquals("DFA1 did not create plain text output correctly!",
                "    a b \n" +
                        "  1 2 1 \n" +
                        "< 2 2 1 \n" +
                        "  3 7 5 \n" +
                        "< 4 7 4 \n" +
                        " >5 2 4 \n" +
                        "< 6 6 3 \n" +
                        "  7 7 4 ",
                dfa.exportToString().getPlainText());
        assertEquals("DFA1 did not output HTML code properly!",
                "<div id=\"scoped-content\">\n" +
                        "    <style type=\"text/css\" scoped>\n" +
                        "    \ttable {border-collapse: collapse;}\n" +
                        "\t\ttable, td, th {border: 1px solid black;}\n" +
                        "    </style>\n" +
                        "    \n" +
                        "    <table>\n" +
                        "        <tr><th colspan=\"2\"></th><th>a</th><th>b</th></tr>\n" +
                        "\t\t<tr><td></td><td>1</td><td>2</td><td>1</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>2</td><td>2</td><td>1</td></tr>\n" +
                        "\t\t<tr><td></td><td>3</td><td>7</td><td>5</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>4</td><td>7</td><td>4</td></tr>\n" +
                        "\t\t<tr><td>&rarr;</td><td>5</td><td>2</td><td>4</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>6</td><td>6</td><td>3</td></tr>\n" +
                        "\t\t<tr><td></td><td>7</td><td>7</td><td>4</td></tr>\n" +
                        "\t</table>\n" +
                        "</div>",
                dfa.exportToString().getHTML());
        assertEquals("DFA1 did not output TEX code properly!",
                "\\begin{tabular}{cc|c|c}\n" +
                        "\t & & $a$ & $b$ \\\\\\hline\n" +
                        "\t & $1$ & $2$ & $1$ \\\\\n" +
                        "\t$\\leftarrow$ & $2$ & $2$ & $1$ \\\\\n" +
                        "\t & $3$ & $7$ & $5$ \\\\\n" +
                        "\t$\\leftarrow$ & $4$ & $7$ & $4$ \\\\\n" +
                        "\t$\\rightarrow$ & $5$ & $2$ & $4$ \\\\\n" +
                        "\t$\\leftarrow$ & $6$ & $6$ & $3$ \\\\\n" +
                        "\t & $7$ & $7$ & $4$ \n" +
                        "\\end{tabular}",
                dfa.exportToString().getTEX());
        assertEquals("DFA1 did not output TIKZ code properly!",
                "\\begin{tikzpicture}[->,>=stealth',shorten >=1pt,auto,node distance=2.8cm,semithick]\n" +
                        "\t\\node[state] (0) {$1$};\n" +
                        "\t\\node[state,accepting] (1) [right of=0] {$2$};\n" +
                        "\t\\node[state] (2) [right of=1] {$3$};\n" +
                        "\t\\node[state,accepting] (3) [right of=2] {$4$};\n" +
                        "\t\\node[initial,state] (4) [right of=3] {$5$};\n" +
                        "\t\\node[state,accepting] (5) [right of=4] {$6$};\n" +
                        "\t\\node[state] (6) [right of=5] {$7$};\n" +
                        "\t\\path\n" +
                        "\t\t(0)\n" +
                        "\t\t\tedge [loop above] node {$b$} (0)\n" +
                        "\t\t\tedge [bend left] node {$a$} (1)\n" +
                        "\t\t(1)\n" +
                        "\t\t\tedge [bend left] node {$b$} (0)\n" +
                        "\t\t\tedge [loop above] node {$a$} (1)\n" +
                        "\t\t(2)\n" +
                        "\t\t\tedge node {$b$} (4)\n" +
                        "\t\t\tedge node {$a$} (6)\n" +
                        "\t\t(3)\n" +
                        "\t\t\tedge [loop above] node {$b$} (3)\n" +
                        "\t\t\tedge [bend left] node {$a$} (6)\n" +
                        "\t\t(4)\n" +
                        "\t\t\tedge node {$a$} (1)\n" +
                        "\t\t\tedge node {$b$} (3)\n" +
                        "\t\t(5)\n" +
                        "\t\t\tedge node {$b$} (2)\n" +
                        "\t\t\tedge [loop above] node {$a$} (5)\n" +
                        "\t\t(6)\n" +
                        "\t\t\tedge [bend left] node {$b$} (3)\n" +
                        "\t\t\tedge [loop above] node {$a$} (6);\n" +
                        "\\end{tikzpicture}",
                dfa.exportToString().getTIKZ());
        dfa.renameLetter("a", "ba");
        assertEquals("DFA1 did not create plain text output correctly after renaming!",
                "    ba b \n" +
                        "  1 2  1 \n" +
                        "< 2 2  1 \n" +
                        "  3 7  5 \n" +
                        "< 4 7  4 \n" +
                        " >5 2  4 \n" +
                        "< 6 6  3 \n" +
                        "  7 7  4 ",
                dfa.exportToString().getPlainText());
        assertEquals("DFA1 did not output HTML code properly!",
                "<div id=\"scoped-content\">\n" +
                        "    <style type=\"text/css\" scoped>\n" +
                        "    \ttable {border-collapse: collapse;}\n" +
                        "\t\ttable, td, th {border: 1px solid black;}\n" +
                        "    </style>\n" +
                        "    \n" +
                        "    <table>\n" +
                        "        <tr><th colspan=\"2\"></th><th>ba</th><th>b</th></tr>\n" +
                        "\t\t<tr><td></td><td>1</td><td>2</td><td>1</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>2</td><td>2</td><td>1</td></tr>\n" +
                        "\t\t<tr><td></td><td>3</td><td>7</td><td>5</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>4</td><td>7</td><td>4</td></tr>\n" +
                        "\t\t<tr><td>&rarr;</td><td>5</td><td>2</td><td>4</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>6</td><td>6</td><td>3</td></tr>\n" +
                        "\t\t<tr><td></td><td>7</td><td>7</td><td>4</td></tr>\n" +
                        "\t</table>\n" +
                        "</div>",
                dfa.exportToString().getHTML());
        assertEquals("DFA1 did not output TEX code properly!",
                "\\begin{tabular}{cc|c|c}\n" +
                        "\t & & $ba$ & $b$ \\\\\\hline\n" +
                        "\t & $1$ & $2$ & $1$ \\\\\n" +
                        "\t$\\leftarrow$ & $2$ & $2$ & $1$ \\\\\n" +
                        "\t & $3$ & $7$ & $5$ \\\\\n" +
                        "\t$\\leftarrow$ & $4$ & $7$ & $4$ \\\\\n" +
                        "\t$\\rightarrow$ & $5$ & $2$ & $4$ \\\\\n" +
                        "\t$\\leftarrow$ & $6$ & $6$ & $3$ \\\\\n" +
                        "\t & $7$ & $7$ & $4$ \n" +
                        "\\end{tabular}",
                dfa.exportToString().getTEX());
        assertEquals("DFA1 did not output TIKZ code properly!",
                "\\begin{tikzpicture}[->,>=stealth',shorten >=1pt,auto,node distance=2.8cm,semithick]\n" +
                        "\t\\node[state] (0) {$1$};\n" +
                        "\t\\node[state,accepting] (1) [right of=0] {$2$};\n" +
                        "\t\\node[state] (2) [right of=1] {$3$};\n" +
                        "\t\\node[state,accepting] (3) [right of=2] {$4$};\n" +
                        "\t\\node[initial,state] (4) [right of=3] {$5$};\n" +
                        "\t\\node[state,accepting] (5) [right of=4] {$6$};\n" +
                        "\t\\node[state] (6) [right of=5] {$7$};\n" +
                        "\t\\path\n" +
                        "\t\t(0)\n" +
                        "\t\t\tedge [loop above] node {$b$} (0)\n" +
                        "\t\t\tedge [bend left] node {$ba$} (1)\n" +
                        "\t\t(1)\n" +
                        "\t\t\tedge [bend left] node {$b$} (0)\n" +
                        "\t\t\tedge [loop above] node {$ba$} (1)\n" +
                        "\t\t(2)\n" +
                        "\t\t\tedge node {$b$} (4)\n" +
                        "\t\t\tedge node {$ba$} (6)\n" +
                        "\t\t(3)\n" +
                        "\t\t\tedge [loop above] node {$b$} (3)\n" +
                        "\t\t\tedge [bend left] node {$ba$} (6)\n" +
                        "\t\t(4)\n" +
                        "\t\t\tedge node {$ba$} (1)\n" +
                        "\t\t\tedge node {$b$} (3)\n" +
                        "\t\t(5)\n" +
                        "\t\t\tedge node {$b$} (2)\n" +
                        "\t\t\tedge [loop above] node {$ba$} (5)\n" +
                        "\t\t(6)\n" +
                        "\t\t\tedge [bend left] node {$b$} (3)\n" +
                        "\t\t\tedge [loop above] node {$ba$} (6);\n" +
                        "\\end{tikzpicture}",
                dfa.exportToString().getTIKZ());
        dfa.renameState("3", "S");
        assertEquals("DFA1 did not create plain text output correctly after renaming!",
                "    ba b \n" +
                        "  1 2  1 \n" +
                        "< 2 2  1 \n" +
                        "  S 7  5 \n" +
                        "< 4 7  4 \n" +
                        " >5 2  4 \n" +
                        "< 6 6  S \n" +
                        "  7 7  4 ",
                dfa.exportToString().getPlainText());
        assertEquals("DFA1 did not output HTML code properly!",
                "<div id=\"scoped-content\">\n" +
                        "    <style type=\"text/css\" scoped>\n" +
                        "    \ttable {border-collapse: collapse;}\n" +
                        "\t\ttable, td, th {border: 1px solid black;}\n" +
                        "    </style>\n" +
                        "    \n" +
                        "    <table>\n" +
                        "        <tr><th colspan=\"2\"></th><th>ba</th><th>b</th></tr>\n" +
                        "\t\t<tr><td></td><td>1</td><td>2</td><td>1</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>2</td><td>2</td><td>1</td></tr>\n" +
                        "\t\t<tr><td></td><td>S</td><td>7</td><td>5</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>4</td><td>7</td><td>4</td></tr>\n" +
                        "\t\t<tr><td>&rarr;</td><td>5</td><td>2</td><td>4</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>6</td><td>6</td><td>S</td></tr>\n" +
                        "\t\t<tr><td></td><td>7</td><td>7</td><td>4</td></tr>\n" +
                        "\t</table>\n" +
                        "</div>",
                dfa.exportToString().getHTML());
        assertEquals("DFA1 did not output TEX code properly!",
                "\\begin{tabular}{cc|c|c}\n" +
                        "\t & & $ba$ & $b$ \\\\\\hline\n" +
                        "\t & $1$ & $2$ & $1$ \\\\\n" +
                        "\t$\\leftarrow$ & $2$ & $2$ & $1$ \\\\\n" +
                        "\t & $S$ & $7$ & $5$ \\\\\n" +
                        "\t$\\leftarrow$ & $4$ & $7$ & $4$ \\\\\n" +
                        "\t$\\rightarrow$ & $5$ & $2$ & $4$ \\\\\n" +
                        "\t$\\leftarrow$ & $6$ & $6$ & $S$ \\\\\n" +
                        "\t & $7$ & $7$ & $4$ \n" +
                        "\\end{tabular}",
                dfa.exportToString().getTEX());
        assertEquals("DFA1 did not output TIKZ code properly!",
                "\\begin{tikzpicture}[->,>=stealth',shorten >=1pt,auto,node distance=2.8cm,semithick]\n" +
                        "\t\\node[state] (0) {$1$};\n" +
                        "\t\\node[state,accepting] (1) [right of=0] {$2$};\n" +
                        "\t\\node[state] (2) [right of=1] {$S$};\n" +
                        "\t\\node[state,accepting] (3) [right of=2] {$4$};\n" +
                        "\t\\node[initial,state] (4) [right of=3] {$5$};\n" +
                        "\t\\node[state,accepting] (5) [right of=4] {$6$};\n" +
                        "\t\\node[state] (6) [right of=5] {$7$};\n" +
                        "\t\\path\n" +
                        "\t\t(0)\n" +
                        "\t\t\tedge [loop above] node {$b$} (0)\n" +
                        "\t\t\tedge [bend left] node {$ba$} (1)\n" +
                        "\t\t(1)\n" +
                        "\t\t\tedge [bend left] node {$b$} (0)\n" +
                        "\t\t\tedge [loop above] node {$ba$} (1)\n" +
                        "\t\t(2)\n" +
                        "\t\t\tedge node {$b$} (4)\n" +
                        "\t\t\tedge node {$ba$} (6)\n" +
                        "\t\t(3)\n" +
                        "\t\t\tedge [loop above] node {$b$} (3)\n" +
                        "\t\t\tedge [bend left] node {$ba$} (6)\n" +
                        "\t\t(4)\n" +
                        "\t\t\tedge node {$ba$} (1)\n" +
                        "\t\t\tedge node {$b$} (3)\n" +
                        "\t\t(5)\n" +
                        "\t\t\tedge node {$b$} (2)\n" +
                        "\t\t\tedge [loop above] node {$ba$} (5)\n" +
                        "\t\t(6)\n" +
                        "\t\t\tedge [bend left] node {$b$} (3)\n" +
                        "\t\t\tedge [loop above] node {$ba$} (6);\n" +
                        "\\end{tikzpicture}",
                dfa.exportToString().getTIKZ());
    }

    @Test
    public void testPlainText2() {
        DFAAutomaton dfa = AutomatonSamples.DFASamples.lolipopNumbers();
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
                dfa.exportToString().getPlainText());
    }

    @Test
    public void testPlainText3() {
        DFAAutomaton dfa = AutomatonSamples.DFASamples.alphaStar();
        assertEquals("DFA2 did not create plain text output correctly!",
                "    \\alpha \\beta \n" +
                        "<>0 0      1     \n" +
                        "  1 1      1     ",
                dfa.exportToString().getPlainText());
    }

    @Test
    public void testHTML1() {
        DFAAutomaton dfa = AutomatonSamples.DFASamples.startEndSame();
        assertEquals("DFA1 did not output HTML code properly!",
                "<div id=\"scoped-content\">\n" +
                        "    <style type=\"text/css\" scoped>\n" +
                        "    \ttable {border-collapse: collapse;}\n" +
                        "\t\ttable, td, th {border: 1px solid black;}\n" +
                        "    </style>\n" +
                        "    \n" +
                        "    <table>\n" +
                        "        <tr><th colspan=\"2\"></th><th>a</th><th>b</th></tr>\n" +
                        "\t\t<tr><td></td><td>1</td><td>2</td><td>1</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>2</td><td>2</td><td>1</td></tr>\n" +
                        "\t\t<tr><td></td><td>3</td><td>7</td><td>5</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>4</td><td>7</td><td>4</td></tr>\n" +
                        "\t\t<tr><td>&rarr;</td><td>5</td><td>2</td><td>4</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>6</td><td>6</td><td>3</td></tr>\n" +
                        "\t\t<tr><td></td><td>7</td><td>7</td><td>4</td></tr>\n" +
                        "\t</table>\n" +
                        "</div>",
                dfa.exportToString().getHTML());

    }

    @Test
    public void testHTML2() {
        DFAAutomaton dfa = AutomatonSamples.DFASamples.lolipopNumbers();
        assertEquals("DFA2 did not output HTML code properly!",
                "<div id=\"scoped-content\">\n" +
                        "    <style type=\"text/css\" scoped>\n" +
                        "    \ttable {border-collapse: collapse;}\n" +
                        "\t\ttable, td, th {border: 1px solid black;}\n" +
                        "    </style>\n" +
                        "    \n" +
                        "    <table>\n" +
                        "        <tr><th colspan=\"2\"></th><th>0.12</th><th>-6.38</th><th>0</th><th>213.002</th></tr>\n" +
                        "\t\t<tr><td>&rarr;</td><td>0</td><td>1</td><td>0</td><td>0</td><td>0</td></tr>\n" +
                        "\t\t<tr><td></td><td>1</td><td>0</td><td>2</td><td>0</td><td>0</td></tr>\n" +
                        "\t\t<tr><td></td><td>2</td><td>3</td><td>0</td><td>0</td><td>0</td></tr>\n" +
                        "\t\t<tr><td></td><td>3</td><td>0</td><td>0</td><td>4</td><td>0</td></tr>\n" +
                        "\t\t<tr><td></td><td>4</td><td>0</td><td>0</td><td>0</td><td>5</td></tr>\n" +
                        "\t\t<tr><td></td><td>5</td><td>0</td><td>6</td><td>0</td><td>0</td></tr>\n" +
                        "\t\t<tr><td></td><td>6</td><td>0</td><td>0</td><td>0</td><td>7</td></tr>\n" +
                        "\t\t<tr><td>&larr;</td><td>7</td><td>7</td><td>7</td><td>7</td><td>7</td></tr>\n" +
                        "\t</table>\n" +
                        "</div>",
                dfa.exportToString().getHTML());
    }

    @Test
    public void testHTML3() {
        DFAAutomaton dfa = AutomatonSamples.DFASamples.alphaStar();
        assertEquals("DFA3 did not output HTML code properly!",
                "<div id=\"scoped-content\">\n" +
                        "    <style type=\"text/css\" scoped>\n" +
                        "    \ttable {border-collapse: collapse;}\n" +
                        "\t\ttable, td, th {border: 1px solid black;}\n" +
                        "    </style>\n" +
                        "    \n" +
                        "    <table>\n" +
                        "        <tr><th colspan=\"2\"></th><th>\\alpha</th><th>\\beta</th></tr>\n" +
                        "\t\t<tr><td>&harr;</td><td>0</td><td>0</td><td>1</td></tr>\n" +
                        "\t\t<tr><td></td><td>1</td><td>1</td><td>1</td></tr>\n" +
                        "\t</table>\n" +
                        "</div>",
                dfa.exportToString().getHTML());
    }

    @Test
    public void testTEX1() {
        DFAAutomaton dfa = AutomatonSamples.DFASamples.startEndSame();
        assertEquals("DFA1 did not output TEX code properly!",
                "\\begin{tabular}{cc|c|c}\n" +
                        "\t & & $a$ & $b$ \\\\\\hline\n" +
                        "\t & $1$ & $2$ & $1$ \\\\\n" +
                        "\t$\\leftarrow$ & $2$ & $2$ & $1$ \\\\\n" +
                        "\t & $3$ & $7$ & $5$ \\\\\n" +
                        "\t$\\leftarrow$ & $4$ & $7$ & $4$ \\\\\n" +
                        "\t$\\rightarrow$ & $5$ & $2$ & $4$ \\\\\n" +
                        "\t$\\leftarrow$ & $6$ & $6$ & $3$ \\\\\n" +
                        "\t & $7$ & $7$ & $4$ \n" +
                        "\\end{tabular}",
                dfa.exportToString().getTEX());
    }

    @Test
    public void testTEX2() {
        DFAAutomaton dfa = AutomatonSamples.DFASamples.lolipopNumbers();
        assertEquals("DFA2 did not output TEX code properly!",
                "\\begin{tabular}{cc|c|c|c|c}\n" +
                        "\t & & $0.12$ & $-6.38$ & $0$ & $213.002$ \\\\\\hline\n" +
                        "\t$\\rightarrow$ & $0$ & $1$ & $0$ & $0$ & $0$ \\\\\n" +
                        "\t & $1$ & $0$ & $2$ & $0$ & $0$ \\\\\n" +
                        "\t & $2$ & $3$ & $0$ & $0$ & $0$ \\\\\n" +
                        "\t & $3$ & $0$ & $0$ & $4$ & $0$ \\\\\n" +
                        "\t & $4$ & $0$ & $0$ & $0$ & $5$ \\\\\n" +
                        "\t & $5$ & $0$ & $6$ & $0$ & $0$ \\\\\n" +
                        "\t & $6$ & $0$ & $0$ & $0$ & $7$ \\\\\n" +
                        "\t$\\leftarrow$ & $7$ & $7$ & $7$ & $7$ & $7$ \n" +
                        "\\end{tabular}",
                dfa.exportToString().getTEX());
    }

    @Test
    public void testTEX3() {
        assertEquals("DFA3 did not output TEX code properly!",
                "\\begin{tabular}{cc|c|c}\n" +
                        "\t & & $\\alpha$ & $\\beta$ \\\\\\hline\n" +
                        "\t$\\leftrightarrow$ & $0$ & $0$ & $1$ \\\\\n" +
                        "\t & $1$ & $1$ & $1$ \n" +
                        "\\end{tabular}",
                AutomatonSamples.DFASamples.alphaStar().exportToString().getTEX());
    }

    @Test
    public void testTIKZ1() {
        assertEquals("DFA1 did not output TIKZ code properly!",
                "\\begin{tikzpicture}[->,>=stealth',shorten >=1pt,auto,node distance=2.8cm,semithick]\n" +
                        "\t\\node[state] (0) {$1$};\n" +
                        "\t\\node[state,accepting] (1) [right of=0] {$2$};\n" +
                        "\t\\node[state] (2) [right of=1] {$3$};\n" +
                        "\t\\node[state,accepting] (3) [right of=2] {$4$};\n" +
                        "\t\\node[initial,state] (4) [right of=3] {$5$};\n" +
                        "\t\\node[state,accepting] (5) [right of=4] {$6$};\n" +
                        "\t\\node[state] (6) [right of=5] {$7$};\n" +
                        "\t\\path\n" +
                        "\t\t(0)\n" +
                        "\t\t\tedge [loop above] node {$b$} (0)\n" +
                        "\t\t\tedge [bend left] node {$a$} (1)\n" +
                        "\t\t(1)\n" +
                        "\t\t\tedge [bend left] node {$b$} (0)\n" +
                        "\t\t\tedge [loop above] node {$a$} (1)\n" +
                        "\t\t(2)\n" +
                        "\t\t\tedge node {$b$} (4)\n" +
                        "\t\t\tedge node {$a$} (6)\n" +
                        "\t\t(3)\n" +
                        "\t\t\tedge [loop above] node {$b$} (3)\n" +
                        "\t\t\tedge [bend left] node {$a$} (6)\n" +
                        "\t\t(4)\n" +
                        "\t\t\tedge node {$a$} (1)\n" +
                        "\t\t\tedge node {$b$} (3)\n" +
                        "\t\t(5)\n" +
                        "\t\t\tedge node {$b$} (2)\n" +
                        "\t\t\tedge [loop above] node {$a$} (5)\n" +
                        "\t\t(6)\n" +
                        "\t\t\tedge [bend left] node {$b$} (3)\n" +
                        "\t\t\tedge [loop above] node {$a$} (6);\n" +
                        "\\end{tikzpicture}",
                AutomatonSamples.DFASamples.startEndSame().exportToString().getTIKZ());
    }

    @Test
    public void testTIKZ2() {
        assertEquals("DFA2 did not output TIKZ code properly!",
                "\\begin{tikzpicture}[->,>=stealth',shorten >=1pt,auto,node distance=2.8cm,semithick]\n" +
                        "\t\\node[initial,state] (0) {$0$};\n" +
                        "\t\\node[state] (1) [right of=0] {$1$};\n" +
                        "\t\\node[state] (2) [right of=1] {$2$};\n" +
                        "\t\\node[state] (3) [right of=2] {$3$};\n" +
                        "\t\\node[state] (4) [right of=3] {$4$};\n" +
                        "\t\\node[state] (5) [right of=4] {$5$};\n" +
                        "\t\\node[state] (6) [right of=5] {$6$};\n" +
                        "\t\\node[state,accepting] (7) [right of=6] {$7$};\n" +
                        "\t\\path\n" +
                        "\t\t(0)\n" +
                        "\t\t\tedge [loop above] node {$-6.38,0,213.002$} (0)\n" +
                        "\t\t\tedge [bend left] node {$0.12$} (1)\n" +
                        "\t\t(1)\n" +
                        "\t\t\tedge [bend left] node {$0.12,0,213.002$} (0)\n" +
                        "\t\t\tedge node {$-6.38$} (2)\n" +
                        "\t\t(2)\n" +
                        "\t\t\tedge node {$-6.38,0,213.002$} (0)\n" +
                        "\t\t\tedge node {$0.12$} (3)\n" +
                        "\t\t(3)\n" +
                        "\t\t\tedge node {$0.12,-6.38,213.002$} (0)\n" +
                        "\t\t\tedge node {$0$} (4)\n" +
                        "\t\t(4)\n" +
                        "\t\t\tedge node {$0.12,-6.38,0$} (0)\n" +
                        "\t\t\tedge node {$213.002$} (5)\n" +
                        "\t\t(5)\n" +
                        "\t\t\tedge node {$0.12,0,213.002$} (0)\n" +
                        "\t\t\tedge node {$-6.38$} (6)\n" +
                        "\t\t(6)\n" +
                        "\t\t\tedge node {$0.12,-6.38,0$} (0)\n" +
                        "\t\t\tedge node {$213.002$} (7)\n" +
                        "\t\t(7)\n" +
                        "\t\t\tedge [loop above] node {$0.12,-6.38,0,213.002$} (7);\n" +
                        "\\end{tikzpicture}",
                AutomatonSamples.DFASamples.lolipopNumbers().exportToString().getTIKZ());
    }

    @Test
    public void testTIKZ3() {
        assertEquals("DFA3 did not ouput TIKZ code properly!",
                "\\begin{tikzpicture}[->,>=stealth',shorten >=1pt,auto,node distance=2.8cm,semithick]\n" +
                        "\t\\node[initial,state,accepting] (0) {$0$};\n" +
                        "\t\\node[state] (1) [right of=0] {$1$};\n" +
                        "\t\\path\n" +
                        "\t\t(0)\n" +
                        "\t\t\tedge [loop above] node {$\\alpha$} (0)\n" +
                        "\t\t\tedge node {$\\beta$} (1)\n" +
                        "\t\t(1)\n" +
                        "\t\t\tedge [loop above] node {$\\alpha,\\beta$} (1);\n" +
                        "\\end{tikzpicture}",
                AutomatonSamples.DFASamples.alphaStar().exportToString().getTIKZ());
    }
}
