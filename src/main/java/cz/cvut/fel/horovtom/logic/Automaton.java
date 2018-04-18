package cz.cvut.fel.horovtom.logic;

import cz.cvut.fel.horovtom.tools.Utilities;
import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public abstract class Automaton {
    private final static Logger LOGGER = Logger.getLogger(Automaton.class.getName());
    /**
     * Ordered array of state names in Q
     */
    protected String[] Q;
    /**
     * Ordered array of letter names in Sigma
     * If automaton has epsilon transitions, epsilon will always be the first letter in this array, so its index is 0
     */
    protected String[] sigma;
    /**
     * Array containing indices of initial states in no particular order
     */
    protected int[] initialStates;
    /**
     * Array containing indices of accepting states in no particular order
     */
    protected int[] acceptingStates;
    /**
     * Representation of transitions table, where first key is the current state, second key is the current letter and
     * the result is the array of indices of all possible target states.
     * <p>
     * Transitions: &delta; : Q x &Sigma; &rarr; P(Q) <br>
     * </p>
     * <p>
     * We get &delta;(0, 1) = A &sube; Q <br>
     * as A = transitions.get(0).get(1) <br>
     * where 0 is the index of current state and 1 is the index of the letter letter
     * </p>
     */
    protected HashMap<Integer, HashMap<Integer, int[]>> transitions;

    //region CACHES
    /**
     * This contains the reduced automaton, has it been calculated yet. Else it contains null.
     */
    protected DFAAutomaton reduced = null;
    /**
     * These variables hold the cache for fast getting indices of states and letters from strings
     */
    private HashMap<String, Integer> sigmaMapping = null, stateMapping = null;
    /**
     * Constants to be used as indices for {@link #savedToString}
     */
    protected final int PLAIN_TEXT = 0, HTML = 1, TEX = 2, TIKZ = 3;
    /**
     * Array of names that are evaluated as epsilon letters
     */
    public static final String[] epsilonNames = new String[]{
            "\\epsilon", "ε", "eps"
    };

    /**
     * This variable holds the cache for string formats of this automaton
     * <p>E.G.: {@link #getAutomatonTableHTML()} or {@link #getAutomatonTIKZ()}</p>
     */
    protected final String[] savedToString = new String[4];
    /**
     * This variable holds the cache for column lengths, used mainly by {@link #getAutomatonTablePlainText()}
     */
    protected int[] savedColumnLengths;
    //endregion

    //region CONSTRUCTORS

    /**
     * Mandatory
     */
    protected Automaton() {
    }

    /**
     * Constructor used to initialize the variables. It is used by children of this abstract class.
     *
     * @param Q           State names of the automaton
     * @param sigma       Letter names of the automaton
     * @param transitions Table of transitions in text form. The value of this map can be empty, or state names separated by commas
     * @param initials    Initial state names of the automaton in text form.
     * @param accepting   Accepting state names of the automaton in text form
     */
    public Automaton(String[] Q, String[] sigma, HashMap<String, HashMap<String, String[]>> transitions, String[] initials, String[] accepting) {
        initializeQSigma(Q, sigma);
        initializeTransitions(transitions);
        initializeInitAcc(initials, accepting);
    }

    /**
     * Initializes initial and accepting states. Should be called after {@link #initializeQSigma(String[], String[])}
     */
    protected void initializeInitAcc(String[] initials, String[] accepting) {
        int[] acc = new int[accepting.length];
        for (int i = 0; i < acc.length; i++) {
            acc[i] = getStateIndex(accepting[i]);
        }
        int[] init = new int[initials.length];
        for (int i = 0; i < init.length; i++) {
            init[i] = getStateIndex(initials[i]);
        }

        this.initialStates = init;
        this.acceptingStates = acc;
    }

    /**
     * Initializes state names and letter names
     */
    protected void initializeQSigma(String[] Q, String[] sigma) {
        String[] newSigma = new String[sigma.length];

        int epsilonIndex = -1;
        //Search for epsilon letter
        for (int i = 0; i < sigma.length; i++) {
            for (String epsilonName : epsilonNames) {
                if (epsilonName.equals(sigma[i])) {
                    epsilonIndex = i;
                    break;
                }
            }
        }

        int current = 0, currNew = 0;
        if (epsilonIndex != -1) {
            newSigma[0] = sigma[epsilonIndex];
            currNew = 1;
        }
        while (current < sigma.length) {
            if (current == epsilonIndex) {
                current++;
                continue;
            }

            newSigma[currNew] = sigma[current];

            current++;
            currNew++;
        }

        this.Q = Q;
        this.sigma = newSigma;

    }

    /**
     * Initializes transition table of the automaton. Should be called after {@link #initializeQSigma(String[], String[])}
     */
    protected void initializeTransitions(HashMap<String, HashMap<String, String[]>> transitions) {
        HashMap<Integer, HashMap<Integer, int[]>> trans = new HashMap<>();
        for (int i = 0; i < Q.length; i++) {
            String from = Q[i];
            HashMap<Integer, int[]> curr = new HashMap<>();
            trans.put(i, curr);
            for (int l = 0; l < this.sigma.length; l++) {
                String by = this.sigma[l];
                String[] to = transitions.get(from).get(by);
                Set<Integer> targets = new HashSet<>();
                for (String aTo : to) {
                    targets.add(this.getStateIndex(aTo));
                }
                curr.put(l, targets.stream().mapToInt(a -> a).toArray());
            }
        }
        this.transitions = trans;
    }

    /**
     * Initializes transitions table of the automaton from the compact map.
     *
     * @param transitions Map where the values hold strings, containing comma-separated lists of target transitions
     */
    protected void initializeTransitionsCompact(HashMap<String, HashMap<String, String>> transitions) {
        HashMap<Integer, HashMap<Integer, int[]>> trans = new HashMap<>();
        for (int i = 0; i < Q.length; i++) {
            String from = Q[i];
            HashMap<String, String> transRow = transitions.get(from);


            HashMap<Integer, int[]> curr = new HashMap<>();
            trans.put(i, curr);

            //If entire row is missing, we assume that no connections are there
            if (transRow == null) {
                for (int l = 0; l < this.sigma.length; l++) {
                    curr.put(l, new int[0]);
                }
                continue;
            }

            for (int l = 0; l < this.sigma.length; l++) {
                String by = this.sigma[l];

                //If we got incomplete map, we assume that no connections are there
                if (!transRow.containsKey(by)) {
                    curr.put(l, new int[0]);
                    continue;
                }

                String to = transRow.get(by);
                Pair<Integer, String> ret;
                ArrayList<Integer> targets = new ArrayList<>();
                int currentIndex = 0;

                while (currentIndex >= 0) {
                    ret = Utilities.getNextToken(to, currentIndex, ',');
                    currentIndex = ret.getKey();
                    if (ret.getValue().length() != 0)
                        targets.add(getStateIndex(ret.getValue()));
                }

                curr.put(l, targets.stream().mapToInt(a -> a).toArray());
            }
        }
        this.transitions = trans;
    }

    //endregion

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
        return "Automaton:\n" + getAutomatonTablePlainText();
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
            result.append(String.format("%1$-" + (columnLengths[1] + 1) + "s", this.sigma[0].equals("\\epsilon") ? "ε" : this.sigma[0]));
            for (int i = 1; i < this.sigma.length; i++) {
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
            if (this.sigma[0].equals("\\epsilon")) {
                res.append("<td>ε</td>");
            } else {
                res.append("<td>").append(this.sigma[0]).append("</td>");
            }
            for (int i = 1; i < this.sigma.length; i++) {
                res.append("<td>").append(this.sigma[i]).append("</td>");
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
            for (String ignored : this.sigma) {
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

    /**
     * @return reduced version of this automaton
     */
    public DFAAutomaton getReduced() {
        if (reduced == null) {
            LOGGER.fine("Reducing automaton:\n" + this.toString());
            reduced = reduce();
        }
        return (DFAAutomaton) reduced.copy();
    }

    /**
     * This method is called if the automaton was not already reduced
     *
     * @return reduced version of this automaton
     */
    protected abstract DFAAutomaton reduce();

    /**
     * This function does not use the optimized reduced automaton to get answer!
     */
    public boolean acceptsWordUnified(String[] word) {
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
            next.clear();
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
            this.reduced = getReduced();
        }

        if (this.reduced != null) {
            return this.reduced.acceptsWord(word);
        } else {
            LOGGER.warning("Reduction of automaton failed for some reason!");
            return this.acceptsWordUnified(word);
        }
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
        if (this.savedColumnLengths != null) {
            return this.savedColumnLengths;
        } else {
            int[] ret = new int[this.sigma.length + 1];
            for (String s : this.Q) {
                ret[0] = Math.max(ret[0], s.length() + 2);
            }

            for (int letter = 0; letter < this.sigma.length; letter++) {
                int where = letter + 1;
                ret[where] = (letter == 0 && this.sigma[0].equals("\\epsilon")) ? 1 : this.sigma[letter].length();
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

            this.savedColumnLengths = ret;
            return ret;
        }
    }

    //region renaming

    /**
     * Renames originalName state to newName state. It does not rename if newName is already a state
     *
     * @return Whether the renaming was successful
     */
    public boolean renameState(String originalName, String newName) {
        if (newName.isEmpty()) {
            LOGGER.warning("Trying to rename state to empty string!");
            return false;
        }
        LOGGER.fine("Trying to rename state " + originalName + " to " + newName);
        int test = getStateIndex(newName);
        if (test != -1) {

            LOGGER.warning("Cannot rename state " + originalName + " to " + newName + " because state with that name already exists");
            return false;
        }

        //Invalidating caches
        invalidateCaches();
        int index = this.getStateIndex(originalName);
        if (index == -1) {
            LOGGER.info("Renaming failed, because state " + originalName + " does not exist");
            return false;
        }
        Q[index] = newName;
        return true;
    }

    /**
     * Renames originalName letter to newName letter. It does not rename if newName is already a letter
     *
     * @return Whether the renaming was successful
     */
    public boolean renameLetter(String originalName, String newName) {
        if (newName.isEmpty()) {
            LOGGER.warning("Trying to rename letter to empty string!");
            return false;
        }
        LOGGER.fine("Trying to rename letter " + originalName + " to " + newName);
        int test = getLetterIndex(newName);
        if (test != -1) {

            LOGGER.warning("Cannot rename letter " + originalName + " to " + newName + " because letter with that name already exists");
            return false;

        }

        invalidateCaches();
        LOGGER.fine("Saved toString caches invalidated");
        int index = this.getLetterIndex(originalName);
        if (index == -1) {
            LOGGER.info("Renaming failed, because letter " + originalName + " does not exist");
            return false;
        } else if (index == 0 && hasEpsilonTransitions()) {
            LOGGER.warning("Cannot rename epsilon state!");
            return false;
        }
        sigma[index] = newName;
        return true;
    }

    //endregion

    public void invalidateCaches() {
        savedToString[0] = savedToString[1] = savedToString[2] = savedToString[3] = null;
        savedColumnLengths = null;
        stateMapping = sigmaMapping = null;
        LOGGER.fine("Caches invalidated");
    }

    public void exportCSV(File file) {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            StringBuilder sb = new StringBuilder(",");

            for (String s : sigma) {
                sb.append(",\"").append(s).append("\"");
            }
            sb.append("\n");

            for (int i = 0; i < Q.length; i++) {
                if (isAcceptingState(i)) {
                    sb.append("<");
                }
                if (isInitialState(i)) {
                    sb.append(">");
                }
                sb.append(",\"").append(Q[i]).append("\"");

                int[] curr;
                for (int letter = 0; letter < sigma.length; letter++) {
                    curr = transitions.get(i).get(letter);
                    if (curr.length > 0) {
                        sb.append(",\"").append(Q[curr[0]]);
                        for (int c = 1; c < curr.length; c++) {
                            sb.append(",").append(Q[curr[c]]);
                        }
                        sb.append("\"");
                    } else sb.append(",");
                }
                sb.append("\n");
            }

            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Automaton importFromCSV(File fileToLoad) {
        if (fileToLoad == null) {
            LOGGER.warning("Cannot import from CSV file which is null!");
            return null;
        }
        try {
            Reader reader = new InputStreamReader(new FileInputStream(fileToLoad), "UTF-8");
            BufferedReader r = new BufferedReader(reader);
            String line = r.readLine();
            ArrayList<String> sigma = new ArrayList<>();
            ArrayList<String> Q = new ArrayList<>();
            Pair<Integer, String> ret;
            ret = Utilities.getNextToken(line, 0, ',');
            ret = Utilities.getNextToken(line, ret.getKey(), ',');
            int curr = ret.getKey();

            while (curr >= 0) {
                ret = Utilities.getNextToken(line, curr, ',');
                curr = ret.getKey();
                String t = ret.getValue();
                t = t.trim();

                if (t.charAt(0) == '\"')
                    sigma.add(t.substring(1, t.length() - 1));
                else
                    sigma.add(t);
            }

            HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
            ArrayList<Integer> initials = new ArrayList<>();
            ArrayList<Integer> accepting = new ArrayList<>();
            int counter = 0;
            while (r.ready()) {
                line = r.readLine();
                curr = 0;
                ret = Utilities.getNextToken(line, curr, ',');
                curr = ret.getKey();

                switch (ret.getValue()) {
                    case "<>":
                        initials.add(counter);
                    case "<":
                        accepting.add(counter);
                        break;
                    case ">":
                        initials.add(counter);
                        break;
                    case "":
                    case " ":
                        break;
                    default:
                        LOGGER.warning("Invalid CSV format, expected <>");
                        return null;
                }

                ret = Utilities.getNextToken(line, curr, ',');
                curr = ret.getKey();
                String state = ret.getValue();
                Q.add(state);
                HashMap<String, String> row = new HashMap<>();
                transitions.put(state, row);
                for (String letter : sigma) {
                    if (curr == -1) {
                        row.put(letter, "");
                        continue;
                    }

                    ret = Utilities.getNextToken(line, curr, ',');
                    curr = ret.getKey();

                    String t = ret.getValue();
                    t = t.trim();
                    if (t.length() == 0) {
                        row.put(letter, "");
                    } else {
                        if (t.charAt(0) == '\"')
                            row.put(letter, t.substring(1, t.length() - 1));
                        else
                            row.put(letter, t);
                    }
                }
                if (curr != -1) {
                    LOGGER.warning("Invalid CSV file, more tokens than expected on line");
                    return null;
                }
                counter++;
            }

            String[] initialString = new String[initials.size()];
            String[] acceptingString = new String[accepting.size()];
            for (int i = 0; i < initials.size(); i++) {
                initialString[i] = Q.get(initials.get(i));
            }
            for (int i = 0; i < accepting.size(); i++) {
                acceptingString[i] = Q.get(accepting.get(i));
            }
            if (reader.ready()) {
                LOGGER.warning("Invalid CSV file");
            }

            return new ENFAAutomaton(
                    Q.toArray(new String[0]),
                    sigma.toArray(new String[0]),
                    transitions,
                    initialString,
                    acceptingString);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning(e.getLocalizedMessage());
            return null;
        }
    }

    public abstract Automaton copy();

    /**
     * @return whether the automaton has any epsilon transitions
     */
    public abstract boolean hasEpsilonTransitions();

    /**
     * Array of states that are accessible from specified state by epsilon transitions.
     *
     * @return Null if state doesn't exist
     */
    public String[] getEpsilonClosure(String state) {
        int stateIndex = this.getStateIndex(state);
        if (stateIndex == -1) {
            LOGGER.info("Invalid state name passed: " + state);
            return null;
        }
        int[] ret = getEpsilonClosure(stateIndex);
        String[] returning = new String[ret.length];
        for (int i = 0; i < ret.length; i++) {
            returning[i] = this.sigma[ret[i]];
        }
        return returning;
    }

    /**
     * @return Array of state indices that are accessible from specified state by epsilon transitions
     */
    protected int[] getEpsilonClosure(int state) {
        if (!hasEpsilonTransitions()) {
            return new int[]{state};
        }

        LinkedList<Integer> toDo = new LinkedList<>();
        toDo.add(state);
        Set<Integer> closure = new HashSet<>();
        while (!toDo.isEmpty()) {
            int curr = toDo.poll();
            if (!closure.contains(curr)) {
                closure.add(curr);
                int[] targ = this.transitions.get(curr).get(0);
                for (int i : targ) {
                    if (!closure.contains(i)) {
                        toDo.add(i);
                    }
                }
            }
        }
        return closure.stream().mapToInt(a -> a).toArray();
    }

    /**
     * This function will return automaton that accepts language L3 = L1L2
     *
     * @param a Automaton accepting language L1
     * @param b Automaton accepting language L2
     * @return Automaton accepting language L1L2 (not reduced)
     */
    public static Automaton getConcatenation(Automaton a, Automaton b) {
        BinaryOperators operators = new BinaryOperators(a, b);
        return operators.getL1L2();
    }

    /**
     * This function will return automaton that accepts L*
     */
    public Automaton getKleeny() {
        //TODO: IMPLEMENT
        return null;
    }

    /**
     * This function will return automaton that is the result of Cartesian multiplication of states.
     *
     * @param a Automaton accepting language L1
     * @param b Automaton accepting language L2
     * @return Automaton accepting language L1+L2
     */
    public static Automaton cartMult(Automaton a, Automaton b) {
        //TODO: IMPLEMENT


        return null;
    }

    /**
     * Converts automaton to regular expression
     *
     * @return regular expression describing the language accepted by this automaton
     */
    public String toRegex() {
        //TODO: IMPLEMENT
        return "";
    }

    /**
     * Generates automaton accepting the same language as regular expression
     *
     * @param regex String containing the regular expression
     * @return Automaton that accepts the same language
     */
    public static ENFAAutomaton fromRegex(String regex) {
        //TODO: IMPLEMENT
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Automaton)) return false;
        if (super.equals(obj)) return true;

        Automaton other = (Automaton) obj;
        DFAAutomaton reducedOther = other.reduce();
        if (this.reduced == null) {
            this.reduced = this.reduce();
        }

        DFAAutomaton a = this.reduced;
        DFAAutomaton b = reducedOther;

        //Has the same number of letters?
        if (b.getSigmaSize() != a.getSigmaSize()) return false;
        //Has the same number of states?
        if (b.getQSize() != this.reduced.getQSize()) return false;
        //Has the same number of accepting states?
        if (b.acceptingStates.length != this.reduced.acceptingStates.length) return false;

        //Can sigma be equal?


        // Array, denoting reducedOther indices in relation to this.reduced
        int[] sigmaMapping = new int[this.reduced.getSigmaSize()];
        outer:
        for (int i = 0; i < this.reduced.sigma.length; i++) {
            for (int o = 0; o < b.sigma.length; o++) {
                if (this.reduced.sigma[i].equals(b.sigma[o])) {
                    sigmaMapping[i] = o;
                    continue outer;
                }
            }
            LOGGER.fine("Automaton is not equal, because it's alphabets are incompatible");
            return false;
        }

        int[] stateMapping = new int[this.reduced.getQSize()];
        Arrays.setAll(stateMapping, val -> -1);
        //We start with initial state:
        stateMapping[a.initialStates[0]] = b.initialStates[0];
        Queue<Integer> queue = new PriorityQueue<>();
        queue.add(a.initialStates[0]);
        while (!queue.isEmpty()) {
            int current = queue.poll();
            for (int i = 0; i < a.sigma.length; i++) {
                int currATarg = a.transitions.get(current).get(i)[0];
                int currBTarg = b.transitions.get(stateMapping[current]).get(sigmaMapping[i])[0];
                if (stateMapping[currATarg] == -1) {
                    stateMapping[currATarg] = currBTarg;
                    queue.add(currATarg);
                } else if (stateMapping[currATarg] != currBTarg) return false;
            }
        }

        for (int i = 0; i < stateMapping.length; i++) {
            if (stateMapping[i] == -1) return false;
            if (a.isAcceptingState(i) != b.isAcceptingState(stateMapping[i])) return false;
        }

        return true;
    }

    HashMap<Integer, HashMap<Integer, int[]>> getTransitions() {
        return Utilities.getCopyOfHashMap(this.transitions);
    }

    int[] getInitialStates() {
        return Arrays.copyOf(this.initialStates, this.initialStates.length);
    }

    int[] getAcceptingStates() {
        return Arrays.copyOf(this.acceptingStates, this.acceptingStates.length);
    }
}
