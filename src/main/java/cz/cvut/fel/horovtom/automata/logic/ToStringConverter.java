package cz.cvut.fel.horovtom.automata.logic;

import java.util.ArrayList;
import java.util.HashMap;

//FIXME: TEST THIS

/**
 * This class is used to convert Automaton to wanted string format.
 */
public class ToStringConverter {
    private final String[] sigma;
    private final String[] Q;
    private final HashMap<Integer, HashMap<Integer, int[]>> transitions;
    private final int[] initials;
    private final int[] accepting;
    private final String description;
    private final int ioColumn;

    //region CACHES
    private String cachedBorderedPlainText = null;
    private String cachedPlainText = null;
    private String cachedHTML = null;
    private String cachedTEX = null;
    private String cachedTIKZ = null;

    /**
     * This is the output column length array. It holds the maximum number of characters in each of the columns.
     * In this array: 0: column of states, 1: column of first letter and so on...
     */
    private final int[] columnLengths;
    //endregion

    /**
     * This will work out the maximum string length of cell columns and return them in array
     *
     * @return Array in which 0: column of states, 1: column of first letter and so on...
     */
    private int[] calculateColumnLengths() {
        int[] ret = new int[this.sigma.length + 1];
        for (String s : this.Q) {
            ret[0] = Math.max(ret[0], s.length() + 2);
        }

        for (int letter = 0; letter < this.sigma.length; letter++) {
            int where = letter + 1;
            boolean epsFound = false;
            if (letter == 0) {
                for (String epsilonName : Automaton.epsilonNames) {
                    if (this.sigma[0].equals(epsilonName)) {
                        epsFound = true;
                        break;
                    }
                }
            }
            ret[where] = epsFound ? 1 : this.sigma[letter].length();
            for (int state = 0; state < this.Q.length; state++) {
                int curr = -1;
                for (int i : this.transitions.get(state).get(letter)) {
                    curr += this.Q[i].length() + 1;
                }
                if (curr < 0) {
                    curr = 0;
                }
                ret[where] = Math.max(ret[where], curr);
            }
        }

        return ret;
    }

    public ToStringConverter(Automaton a) {
        this.sigma = a.getSigma();
        this.Q = a.getQ();
        this.transitions = a.getTransitions();
        this.initials = a.getInitialStates();
        this.accepting = a.getAcceptingStates();
        this.description = a.getDescription();

        this.columnLengths = calculateColumnLengths();

        int ioColumn = 1;
        outer:
        for (int initial : initials) {
            for (int i : accepting) {
                if (initial == i) {
                    ioColumn += 1;
                    break outer;
                }
            }
        }
        this.ioColumn = ioColumn;
    }

    /**
     * @return Automaton table in plain text separated by borders
     */
    public String getBorderedPlainText() {
        if (cachedBorderedPlainText == null) {
            createBorderedPlainText();
        }
        return cachedBorderedPlainText;
    }

    private void appendLineSeparator(StringBuilder sb) {
        sb.append("+");
        for (int i = 0; i < ioColumn + 2; i++) {
            sb.append("-");
        }
        sb.append("+");
        for (int i1 = 0; i1 < columnLengths.length; i1++) {

            int savedColumnLength = columnLengths[i1];
            if (i1 != 0) savedColumnLength += 2;
            for (int i = 0; i < savedColumnLength; i++) {
                sb.append("-");
            }
            sb.append("+");
        }
        sb.append("\n");
    }

    private void createBorderedPlainText() {
        StringBuilder sb = new StringBuilder();
        appendLineSeparator(sb);
        //Header
        //IO
        sb.append("| ");
        for (int i = 0; i < this.ioColumn; i++) {
            sb.append(" ");
        }
        sb.append(" |");
        for (int i = 0; i < columnLengths[0]; i++) {
            sb.append(" ");
        }
        sb.append("|");
        //Letter names
        for (int i = 0; i < sigma.length; i++) {
            boolean epsFound = false;
            if (i == 0) {
                for (String epsilonName : Automaton.epsilonNames) {
                    if (sigma[i].equals(epsilonName)) {
                        epsFound = true;
                        break;
                    }
                }
            }
            sb.append(" ");
            sb.append(String.format("%1$-" + (columnLengths[i + 1]) + "s", epsFound ? "ε" : sigma[i]));
            sb.append(" |");
        }
        sb.append("\n");
        appendLineSeparator(sb);

        //States
        for (int state = 0; state < Q.length; state++) {
            String io;
            if (ioColumn == 2) {
                if (isAccepting(state)) {
                    if (isInitial(state)) {
                        io = "<>";
                    } else {
                        io = "< ";
                    }
                } else {
                    if (isInitial(state)) {
                        io = " >";
                    } else {
                        io = "  ";
                    }
                }
            } else {
                if (isAccepting(state)) {
                    io = "<";
                } else if (isInitial(state)) {
                    io = ">";
                } else {
                    io = " ";
                }
            }
            //IO
            sb.append("| ").append(io).append(" |").append(String.format("%1$-" + (columnLengths[0]) + "s", Q[state])).append("|");
            //Letters
            for (int letter = 0; letter < sigma.length; letter++) {
                int[] ints = transitions.get(state).get(letter);
                sb.append(" ");
                StringBuilder a = new StringBuilder();
                if (ints.length > 0) {
                    a.append(Q[ints[0]]);
                } else {
                    a.append("∅");
                }
                for (int i = 1; i < ints.length; i++) {
                    a.append(",").append(Q[ints[i]]);
                }
                sb.append(String.format("%1$-" + (columnLengths[letter + 1]) + "s", a.toString())).append(" |");
            }
            sb.append("\n");
            appendLineSeparator(sb);
        }
        this.cachedBorderedPlainText = sb.toString();
    }

    private void createPlainText() {
        StringBuilder result = new StringBuilder();

        //HEADER
        result.append(String.format("%1$-" + (columnLengths[0] + 1) + "s", ""));
        boolean isEps = false;
        if (this.sigma.length > 0) {
            for (String epsilonName : Automaton.epsilonNames) {
                if (this.sigma[0].equals(epsilonName)) {
                    isEps = true;
                    break;
                }
            }

            result.append(String.format("%1$-" + (columnLengths[1] + 1) + "s", isEps ? "ε" : this.sigma[0]));
            for (int i = 1; i < this.sigma.length; i++) {
                result.append(String.format("%1$-" + (columnLengths[i + 1] + 1) + "s", this.sigma[i]));
            }
        }
        result.append("\n");

        //BODY
        for (int state = 0; state < this.Q.length; state++) {
            if (state != 0)
                result.append("\n");
            //States column
            result.append(String.format("%1$-" + (columnLengths[0] + 1) + "s",
                    (isAccepting(state) ? "<" : " ") +
                            (isInitial(state) ? ">" : " ") +
                            this.Q[state]));
            //Transitions
            for (int letter = 0; letter < this.sigma.length; letter++) {
                StringBuilder cell = new StringBuilder();
                int[] transitions = this.transitions.get(state).get(letter);
                if (transitions.length == 0) {
                    result.append(String.format("%1$-" + (columnLengths[letter + 1] + 1) + "s", ""));
                    continue;
                }
                cell.append(this.Q[transitions[0]]);
                for (int i = 1; i < transitions.length; i++) {
                    cell.append(",").append(this.Q[transitions[i]]);
                }
                result.append(String.format("%1$-" + (columnLengths[letter + 1] + 1) + "s", cell.toString()));
            }
        }
        this.cachedPlainText = result.toString();
    }

    /**
     * @return String containing formatted table as html
     */
    public String getHTML() {
        if (cachedHTML == null) createHTML();

        return cachedHTML;
    }

    private void createHTML() {
        StringBuilder res = new StringBuilder("<div id=\"scoped-content\">\n" +
                "    <style type=\"text/css\" scoped>\n" +
                "    \ttable {border-collapse: collapse;}\n" +
                "\t\ttable, td, th {border: 1px solid black;}\n" +
                "    </style>\n" +
                "    \n" +
                "    <table>\n" +
                "        <tr><th colspan=\"2\"></th>");
        if (this.sigma[0].equals("\\epsilon")) {
            res.append("<th>ε</th>");
        } else {
            res.append("<th>").append(this.sigma[0]).append("</th>");
        }
        for (int i = 1; i < this.sigma.length; i++) {
            res.append("<th>").append(this.sigma[i]).append("</th>");
        }
        res.append("</tr>\n");
        for (int i = 0; i < this.Q.length; i++) {
            res.append("\t\t<tr><td>");
            if (isInitial(i)) {
                if (isAccepting(i)) {
                    res.append("&harr;");
                } else {
                    res.append("&rarr;");
                }
            } else if (isAccepting(i)) {
                res.append("&larr;");
            }
            res.append("</td><td>").append(this.Q[i]).append("</td>");
            for (int letter = 0; letter < this.sigma.length; letter++) {
                res.append("<td>");
                int[] cell = this.transitions.get(i).get(letter);
                StringBuilder cellString = new StringBuilder();
                if (cell.length != 0)
                    cellString.append(this.Q[cell[0]]);
                else
                    cellString.append("&empty;");

                for (int item = 1; item < cell.length; item++) {
                    cellString.append(",").append(this.Q[cell[item]]);
                }
                res.append(cellString.toString()).append("</td>");
            }
            res.append("</tr>\n");
        }
        res.append("\t</table>\n</div>");
        cachedHTML = res.toString();
    }

    /**
     * @return String containing formatted table as plain text
     */
    public String getPlainText() {
        if (this.cachedPlainText == null) createPlainText();
        return this.cachedPlainText;
    }


    /**
     * @return String containing formatted table as tex code
     */
    public String getTEX() {
        if (cachedTEX == null) createTEX();
        return cachedTEX;
    }

    private void createTEX() {
        StringBuilder res = new StringBuilder("\\begin{tabular}{cc");
        for (String ignored : this.sigma) {
            res.append("|c");
        }
        res.append("}\n\t & ");

        outer:
        for (String s : this.sigma) {
            for (String epsilonName : Automaton.epsilonNames) {
                if (s.equals(epsilonName)) {
                    res.append("& $\\varepsilon$ ");
                    continue outer;
                }
            }
            res.append("& $").append(s).append("$ ");
        }
        res.append("\\\\\\hline\n");

        for (int state = 0; state < this.Q.length; state++) {
            res.append("\t");
            if (isInitial(state)) {
                if (isAccepting(state)) {
                    res.append("$\\leftrightarrow$");
                } else {
                    res.append("$\\rightarrow$");
                }
            } else if (isAccepting(state)) {
                res.append("$\\leftarrow$");
            }

            //StateName
            res.append(" & $").append(this.Q[state]).append("$ ");
            //Transitions
            for (int letter = 0; letter < this.sigma.length; letter++) {
                res.append("& ");
                int[] current = this.transitions.get(state).get(letter);
                if (current.length != 0)
                    res.append("$").append(this.Q[current[0]]);
                for (int i = 1; i < current.length; i++) {
                    res.append(",").append(this.Q[current[i]]);
                }
                if (current.length != 0)
                    res.append("$ ");
            }
            if (state != this.Q.length - 1)
                res.append("\\\\\n");
            else
                res.append("\n");
        }

        res.append("\\end{tabular}");
        cachedTEX = res.toString();
    }

    /**
     * @return String containing code for TEX package TIKZ. This code draws a diagram of this automaton
     */
    public String getTIKZ() {
        if (cachedTIKZ == null) createTIKZ();
        return cachedTIKZ;
    }

    private void createTIKZ() {
        StringBuilder res = new StringBuilder("\\begin{tikzpicture}[->,>=stealth',shorten >=1pt,auto,node distance=2.8cm,semithick]\n");
        for (int state = 0; state < this.Q.length; state++) {
            res.append("\t\\node[");
            //I/A
            if (isInitial(state)) {
                res.append("initial,");
            }
            res.append("state");
            if (isAccepting(state)) {
                res.append(",accepting");
            }
            res.append("] (").append(state).append(") ");
            //Adjacency
            if (state != 0) {
                res.append("[right of=").append(state - 1).append("] ");
            }
            //Name
            res.append("{$").append(this.Q[state]).append("$};\n");
        }
        res.append("\t\\path");

        for (int state = 0; state < this.Q.length; state++) {
            res.append("\n\t\t(").append(state).append(")");

            // key: target, value: letters
            HashMap<Integer, ArrayList<Integer>> edgesFromState = getEdgesFromState(state);

            for (int target = 0; target < this.Q.length; target++) {
                if (!edgesFromState.containsKey(target)) continue;

                res.append("\n\t\t\tedge ");
                //Edge properties
                if (state == target) {
                    //It is a loop
                    res.append("[loop above] ");
                } else if (hasEdgeFromTo(target, state)) {
                    //It is a bend
                    res.append("[bend left] ");
                }

                res.append("node {$");
                ArrayList<Integer> current = edgesFromState.get(target);
                if (current.size() != 0) {
                    boolean found = false;
                    for (String epsilonName : Automaton.epsilonNames) {
                        if (this.sigma[current.get(0)].equals(epsilonName)) {
                            res.append("\\varepsilon");
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        res.append(this.sigma[current.get(0)]);
                }
                for (int letter = 1; letter < current.size(); letter++) {
                    res.append(",").append(this.sigma[current.get(letter)]);
                }
                res.append("$} ");
                //Target
                res.append("(").append(target).append(")");
            }
        }
        res.append(";\n\\end{tikzpicture}");
        cachedTIKZ = res.toString();
    }

    /**
     * @return true if there is an edge from state to target
     */
    private boolean hasEdgeFromTo(int state, int target) {
        HashMap<Integer, int[]> states = this.transitions.get(state);
        for (int letter = 0; letter < this.sigma.length; letter++) {
            for (int i : states.get(letter)) {
                if (i == target) return true;
            }
        }
        return false;
    }

    /**
     * @return HashMap that has target state as key and all letters as values
     */
    private HashMap<Integer, ArrayList<Integer>> getEdgesFromState(int state) {
        // key: target, value: letters
        HashMap<Integer, ArrayList<Integer>> res = new HashMap<>();
        // key: letter, value: targets
        HashMap<Integer, int[]> stateTransitions = this.transitions.get(state);
        for (int letter = 0; letter < this.sigma.length; letter++) {
            int[] targs = stateTransitions.get(letter);
            for (int targ : targs) {
                if (!res.containsKey(targ)) {
                    res.put(targ, new ArrayList<>());
                }
                res.get(targ).add(letter);
            }
        }
        return res;
    }

    private boolean isAccepting(int state) {
        for (int i : accepting) {
            if (state == i) return true;
        }
        return false;
    }

    private boolean isInitial(int state) {
        for (int initial : initials) {
            if (state == initial) return true;
        }
        return false;
    }
}
