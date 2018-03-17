package cz.cvut.fel.horovtom.logic.abstracts;

import cz.cvut.fel.horovtom.logic.DFAAutomaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

public abstract class Automaton {
    private final static Logger LOGGER = Logger.getLogger(Automaton.class.getName());
    protected String[] Q, sigma;
    protected int[] initialStates;
    protected int[] acceptingStates;
    protected HashMap<Integer, HashMap<Integer, int[]>> transitions;
    protected DFAAutomaton reduced = null;
    private HashMap<String, Integer> sigmaMapping, stateMapping;

    /**
     * Constants to be used as indices for {@link #savedToString}
     */
    protected final int PLAIN_TEXT = 0, HTML = 1, TEX = 2, TIKZ = 3;
    protected final String[] savedToString = new String[4];

    protected Automaton() {
    }

    public Automaton(String[] Q, String[] sigma, HashMap<String, HashMap<String, String>> transitions, String[] initials, String[] accepting) {
        HashMap<String, Integer> stringIntStates = new HashMap<>();
        HashMap<Integer, HashMap<Integer, int[]>> trans = new HashMap<>();
        for (int i = 0; i < Q.length; i++) {
            String from = Q[i];
            stringIntStates.put(from, i);
            HashMap<Integer, int[]> curr = new HashMap<>();
            trans.put(i, curr);
            for (int l = 0; l < sigma.length; l++) {
                String by = sigma[l];
                String to = transitions.get(from).get(by);
                if (!stringIntStates.containsKey(to)) {
                    for (int i1 = 1; i1 < Q.length; i1++) {
                        if (Q[i1].equals(to)) {
                            stringIntStates.put(to, i1);
                            break;
                        }
                    }
                }
                curr.put(l, new int[]{stringIntStates.get(to)});
            }
        }

        int[] acc = new int[accepting.length];
        for (int i = 0; i < acc.length; i++) {
            acc[i] = stringIntStates.get(accepting[i]);
        }
        this.Q = Q;
        this.sigma = sigma;
        this.transitions = trans;
        int[] init = new int[initials.length];
        for (int i = 0; i < init.length; i++) {
            init[i] = stringIntStates.get(initials[i]);
        }

        this.initialStates = init;
        this.acceptingStates = acc;
    }

    public int getQSize() {
        return Q.length;
    }

    public int getSigmaSize() {
        return sigma.length;
    }

    /**
     * @return -1 if the letter is not in sigma
     */
    protected int getLetterIndex(String letterName) {
        if (sigmaMapping == null) {
            sigmaMapping = new HashMap<>(sigma.length);
            for (int i = 0; i < sigma.length; i++) {
                sigmaMapping.put(sigma[i], i);
            }
        }

        return sigmaMapping.getOrDefault(letterName, -1);
    }

    /**
     * @return -1 if stateName is not in Q
     */
    protected int getStateIndex(String stateName) {
        if (stateMapping == null) {
            stateMapping = new HashMap<>(Q.length);
            for (int i = 0; i < Q.length; i++) {
                stateMapping.put(Q[i], i);
            }
        }
        return stateMapping.getOrDefault(stateName, -1);
    }

    /**
     * @return byval copy of Q
     */
    public String[] getQ() {
        return Arrays.copyOf(Q, Q.length);
    }

    /**
     * @return byval copy of sigma
     */
    public String[] getSigma() {
        return Arrays.copyOf(sigma, sigma.length);
    }

    @Override
    public String toString() {
        return "Automaton" + getAutomatonTablePlainText();
    }

    /**
     * @return String containing formatted table as plain text
     */
    public String getAutomatonTablePlainText() {
        if (savedToString[PLAIN_TEXT] == null) {
            StringBuilder result = new StringBuilder();
            int[] columnLengths = this.getColumnLengths();

            //HEADER
            result.append(String.format("%1$-" + (columnLengths[0] + 1) + "s", ""));
            for (int i = 0; i < this.sigma.length; i++) {
                result.append(String.format("%1$-" + (columnLengths[i + 1] + 1) + "s", this.sigma[i]));
            }
            result.append("\n");

            //BODY
            for (int state = 0; state < this.Q.length; state++) {
                if (state != 0)
                    result.append("\n");
                //States column
                result.append(String.format("%1$-" + (columnLengths[0] + 1) + "s",
                        (this.isAcceptingState(state) ? "<" : " ") +
                                (this.isInitialState(state) ? ">" : " ") +
                                this.Q[state]));
                //Transitions
                for (int letter = 0; letter < this.sigma.length; letter++) {
                    StringBuilder cell = new StringBuilder();
                    int[] transitions = this.transitions.get(state).get(letter);
                    if (transitions.length == 0) {
                        LOGGER.fine("There is an empty cell in the table");
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
            savedToString[PLAIN_TEXT] = result.toString();
        }
        return savedToString[PLAIN_TEXT];
    }

    /**
     * @return true if the specified state belongs to initial states
     */
    private boolean isInitialState(int state) {
        for (int initialState : initialStates) {
            if (initialState == state) return true;
        }
        return false;
    }

    /**
     * @return String containing formatted table as html
     */
    public String getAutomatonTableHTML() {
        if (savedToString[HTML] == null) {
            StringBuilder res = new StringBuilder("<table>\n\t<tr><td></td><td></td>");
            for (String s : this.sigma) {
                res.append("<td>").append(s).append("</td>");
            }
            res.append("</tr>\n");
            for (int i = 0; i < this.Q.length; i++) {
                res.append("\t<tr><td>");
                if (this.isInitialState(i)) {
                    if (this.isAcceptingState(i)) {
                        res.append("&harr;");
                    } else {
                        res.append("&rarr;");
                    }
                } else if (this.isAcceptingState(i)) {
                    res.append("&larr;");
                }
                res.append("</td><td>").append(this.Q[i]).append("</td>");
                for (int letter = 0; letter < this.sigma.length; letter++) {
                    res.append("<td>");
                    int[] cell = this.transitions.get(i).get(letter);
                    StringBuilder cellString = new StringBuilder();
                    if (cell.length != 0)
                        cellString.append(this.Q[cell[0]]);

                    for (int item = 1; item < cell.length; item++) {
                        cellString.append(",").append(this.Q[cell[item]]);
                    }
                    res.append(cellString.toString()).append("</td>");
                }
                res.append("</tr>\n");
            }
            res.append("</table>");
            savedToString[HTML] = res.toString();
        }

        return savedToString[HTML];
    }

    /**
     * @return String containing formatted table as tex code
     */
    public String getAutomatonTableTEX() {
        if (savedToString[TEX] == null) {
            StringBuilder res = new StringBuilder("\\begin{tabular}{cc");
            String[] sigma1 = this.sigma;
            for (int i = 0; i < sigma1.length; i++) {
                res.append("|c");
            }
            res.append("}\n\t & ");
            for (String s : this.sigma) {
                res.append("& $").append(s).append("$ ");
            }
            res.append("\\\\\\hline\n");

            for (int state = 0; state < this.Q.length; state++) {
                res.append("\t");
                if (this.isInitialState(state)) {
                    if (this.isAcceptingState(state)) {
                        res.append("$\\leftrightarrow$");
                    } else {
                        res.append("$\\rightarrow$");
                    }
                } else if (this.isAcceptingState(state)) {
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
                    res.append("$ ");
                }
                if (state != this.Q.length - 1)
                    res.append("\\\\\n");
                else
                    res.append("\n");
            }

            res.append("\\end{tabular}");
            savedToString[TEX] = res.toString();
        }


        return savedToString[TEX];
    }

    /**
     * @return String containing code for TEX package TIKZ. This code draws a diagram of this automaton
     */
    public String getAutomatonTIKZ() {
        if (savedToString[TIKZ] == null) {
            StringBuilder res = new StringBuilder("\\begin{tikzpicture}[->,>=stealth',shorten >=1pt,auto,node distance=2.8cm,semithick]\n");
            for (int state = 0; state < this.Q.length; state++) {
                res.append("\t\\node[");
                //I/A
                if (this.isInitialState(state)) {
                    res.append("initial,");
                }
                res.append("state");
                if (this.isAcceptingState(state)) {
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
                HashMap<Integer, ArrayList<Integer>> edgesFromState = this.getEdgesFromState(state);

                for (int target = 0; target < this.Q.length; target++) {
                    if (!edgesFromState.containsKey(target)) continue;

                    res.append("\n\t\t\tedge ");
                    //Edge properties
                    if (state == target) {
                        //It is a loop
                        res.append("[loop above] ");
                    } else if (this.hasEdgeFromTo(target, state)) {
                        //It is a bend
                        res.append("[bend left] ");
                    }

                    res.append("node {$");
                    ArrayList<Integer> current = edgesFromState.get(target);
                    if (current.size() != 0)
                        res.append(this.sigma[current.get(0)]);
                    for (int letter = 1; letter < current.size(); letter++) {
                        res.append(",").append(this.sigma[current.get(letter)]);
                    }
                    res.append("$} ");
                    //Target
                    res.append("(").append(target).append(")");
                }
            }
            res.append(";\n\\end{tikzpicture}");
            savedToString[TIKZ] = res.toString();
        }
        return savedToString[TIKZ];
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

    public abstract DFAAutomaton reduce();

    /**
     * This function does not use the optimized reduced automaton to get answer!
     */
    public boolean acceptsWordVerbose(String[] word) {
        //TODO: VERBOSE Workflow?
        ArrayList<HashSet<Integer>> possibilities = new ArrayList<>(2);
        possibilities.add(new HashSet<>(this.Q.length));
        possibilities.add(new HashSet<>(this.Q.length));
        int c = 1, n;
        HashSet<Integer> current = possibilities.get(0);
        HashSet<Integer> next;
        //init
        for (int initialState : initialStates) {
            current.add(initialState);
        }

        //Progress
        for (String letter : word) {
            c = (c + 1) % 2;
            n = (c + 1) % 2;
            int letterIndex = getLetterIndex(letter);
            if (letterIndex == -1) {
                LOGGER.warning("Unknown letter passed: " + letter);
                System.err.println("Unknown letter: " + letter);
                return false;
            }
            current = possibilities.get(c);
            next = possibilities.get(n);
            for (Integer currentState : current) {
                int[] p = getPossibleTransitions(currentState, letterIndex);
                for (int toAddState : p) {
                    next.add(toAddState);
                }
            }
            if (next.size() == 0) return false;
        }

        //Final eval
        c = (c + 1) % 2;
        current = possibilities.get(c);
        for (Integer integer : current) {
            if (isAcceptingState(integer)) return true;
        }
        return false;
    }

    /**
     * This function uses reduced automaton to get answer faster
     *
     * @param word Array of letters from sigma
     */
    public boolean acceptsWord(String[] word) {
        if (this.reduced == null) {
            reduce();
        }

        return this.reduced.acceptsWord(word);
    }

    /**
     * @return true if the specified state belongs to accepting states
     */
    protected boolean isAcceptingState(int stateIndex) {
        for (int acceptingState : acceptingStates) {
            if (acceptingState == stateIndex) return true;
        }
        return false;
    }

    /**
     * This function will return an array of all states, that automaton can end up in after
     * transitioning from specified state by specified letter.
     * <p>
     * For DFA this will be array of only one element, for NFA it might be multiple elements
     */
    protected abstract int[] getPossibleTransitions(int state, int letter);

    /**
     * This will work out the maximum string length of cell columns and return them in array
     *
     * @return Array in which 0: column of states, 1: column of first letter and so on...
     */
    private int[] getColumnLengths() {
        //TODO: ADD SUPPORT FOR CACHING OF THE RESULT
        int[] ret = new int[this.sigma.length + 1];
        for (String s : this.Q) {
            ret[0] = Math.max(ret[0], s.length() + 2);
        }

        for (int letter = 0; letter < this.sigma.length; letter++) {
            int where = letter + 1;
            ret[where] = this.sigma[letter].length();
            for (int state = 0; state < this.Q.length; state++) {
                int curr = -1;
                for (int i : this.transitions.get(state).get(letter)) {
                    curr += this.Q[i].length() + 1;
                }
                if (curr < 0) {
                    LOGGER.warning("Somehow a maxSize of column " + (letter + 1) + "was < 0!");
                    curr = 0;
                }
                ret[where] = Math.max(ret[where], curr);
            }
        }
        return ret;
    }

    //region renaming

    /**
     * Renames originalName state to newName state. It does not rename if newName is already a state
     */
    public void renameState(String originalName, String newName) {
        renameState(originalName, newName, false);
    }

    /**
     * Renames originalName state to newName state.
     *
     * @param force If true, forces renaming even if there is a state with such name
     */
    public void renameState(String originalName, String newName, boolean force) {
        LOGGER.fine("Trying to rename state " + originalName + " to " + newName);
        int test = getStateIndex(newName);
        if (test != -1) {
            if (force) {
                LOGGER.info("State with name " + newName + " already exists, renaming by force");
            } else {
                LOGGER.warning("Cannot rename state " + originalName + " to " + newName + " because state with that name already exists");
                return;
            }
        }
        savedToString[0] = savedToString[1] = savedToString[2] = savedToString[3] = null;
        LOGGER.fine("Saved toString caches invalidated");
        int index = this.getStateIndex(originalName);
        if (index == -1) {
            LOGGER.info("Renaming failed, because state " + originalName + " does not exist");
            return;
        }
        Q[index] = newName;
    }

    /**
     * Renames originalName letter to newName letter. It does not rename if newName is already a letter
     */
    public void renameLetter(String originalName, String newName) {
        renameLetter(originalName, newName, false);
    }

    /**
     * Renames originalName letter to newName letter.
     *
     * @param force If true, forces renaming even if there is a letter with such name
     */
    public void renameLetter(String originalName, String newName, boolean force) {
        LOGGER.fine("Trying to rename letter " + originalName + " to " + newName);
        int test = getLetterIndex(newName);
        if (test != -1) {
            if (force) {
                LOGGER.info("Letter with name " + newName + " already exists, renaming by force");
            } else {
                LOGGER.warning("Cannot rename letter " + originalName + " to " + newName + " because letter with that name already exists");
                return;
            }
        }
        savedToString[0] = savedToString[1] = savedToString[2] = savedToString[3] = null;
        LOGGER.fine("Saved toString caches invalidated");
        int index = this.getLetterIndex(originalName);
        if (index == -1) {
            LOGGER.info("Renaming failed, because letter " + originalName + " does not exist");
            return;
        }
        sigma[index] = newName;
    }

    //endregion
}
