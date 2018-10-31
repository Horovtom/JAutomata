package cz.cvut.fel.horovtom.automata.logic;

import cz.cvut.fel.horovtom.automata.logic.converters.ToRegexConverter;
import cz.cvut.fel.horovtom.automata.tools.Pair;
import cz.cvut.fel.horovtom.automata.tools.Utilities;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public abstract class Automaton {
    //region BASIC VARIABLES

    private final static Logger LOGGER = Logger.getLogger(Automaton.class.getName());
    /**
     * This holds the automaton description, language accepted and so on
     */
    protected String description;
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

    //endregion

    //region CACHES
    protected ToStringConverter toStringConverter;

    /**
     * This contains the reduced automaton, has it been calculated yet. Else it contains null.
     */
    protected DFAAutomaton reduced = null;
    /**
     * These variables hold the cache for fast getting indices of states and letters from strings
     */
    private HashMap<String, Integer> sigmaMapping = null, stateMapping = null;
    /**
     * Array of names that are evaluated as epsilon letters
     */
    public static final String[] epsilonNames = new String[]{
            "\\epsilon", "ε", "eps"
    };

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
        refactorTransitions();
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
     * Returns whether a specified string is considered as a mark for epsilon transition
     */
    public static boolean isEpsilonName(String s) {
        for (String epsilonName : epsilonNames) {
            if (s.equals(epsilonName)) return true;
        }
        return false;
    }

    /**
     * Initializes state names and letter names
     */
    protected void initializeQSigma(String[] Q, String[] sigma) {
        if (Q == null) {
            LOGGER.severe("Specified Q was null, could not continue!");
            System.exit(-1);
        } else if (sigma == null) {
            LOGGER.severe("Specified sigma was null, could not continue!");
            System.exit(-1);
        }
        String[] newSigma = new String[sigma.length];

        int epsilonIndex = -1;
        //Search for epsilon letter
        outer:
        for (int i = 0; i < sigma.length; i++) {
            for (String epsilonName : epsilonNames) {
                if (epsilonName.equals(sigma[i])) {
                    epsilonIndex = i;
                    break outer;
                }
            }
        }

        int current = 0, currNew = 0;
        if (epsilonIndex != -1) {
            newSigma[currNew++] = sigma[epsilonIndex];
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
                if (to != null) {
                    for (String aTo : to) {
                        int tar = this.getStateIndex(aTo);
                        if (tar >= 0)
                            targets.add(this.getStateIndex(aTo));
                    }
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

    /**
     * Attempts to refactor transitions so that every state has filled rows
     */
    protected void refactorTransitions() {
        HashMap<Integer, HashMap<Integer, int[]>> newTransitions = new HashMap<>();

        for (int state = 0; state < this.Q.length; state++) {
            HashMap<Integer, int[]> curr = new HashMap<>();
            newTransitions.put(state, curr);
            for (int letter = 0; letter < this.sigma.length; letter++) {
                int[] orig;
                if (this.transitions.containsKey(state)) {
                    if (this.transitions.get(state).containsKey(letter)) {
                        orig = this.transitions.get(state).get(letter);
                    } else {
                        orig = new int[0];
                    }
                } else {
                    orig = new int[0];
                }
                curr.put(letter, Arrays.copyOf(orig, orig.length));
            }
        }
        this.transitions = newTransitions;
    }

    //endregion

    //region GETTERS

    /**
     * This function will return ENFA automaton that accepts the same language as this automaton.
     * If this automaton is already ENFA, it will return this.
     * Otherwise it will convert to ENFA format.
     */
    public abstract ENFAAutomaton getENFA();

    /**
     * This function will return NFA automaton that accepts the same language as this automaton.
     * If this automaton is already NFA, it will return this.
     * If this automaton is ENFA, it will reduce it to NFA format.
     */
    public abstract NFAAutomaton getNFA();

    /**
     * This function will return DFA automaton that accepts the same language as this automaton.
     * If this automaton is already DFA, it will return this.
     * If this automaton is ENFA or NFA, it will reduce it to DFA format.
     */
    public abstract DFAAutomaton getDFA();

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
     * @return String containing description of the automaton set by the user
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return byval copy of sigma
     */
    public String[] getSigma() {
        return Arrays.copyOf(sigma, sigma.length);
    }

    @Override
    public String toString() {
        return "Automaton:\n" + (this.description != null ? (this.description + "\n") : "") +
                exportToString().getPlainText();
    }

    /**
     * @return whether the automaton has any epsilon transitions
     */
    public abstract boolean hasEpsilonTransitions();

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
     * This function parses string to individual characters as letters,
     * then calls {@link #acceptsWord(String[])}
     * <p>
     * Warning: Does not work on automatons with multiple character letters.k
     *
     * @return Whether this automaton accepts word represented by this string
     */
    public boolean acceptsWord(String word) {
        int a = word.length();
        String[] wordAr = new String[a];
        for (int w = 0; w < a; w++) {
            wordAr[w] = String.valueOf(word.charAt(w));
        }
        return acceptsWord(wordAr);
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

    public String getRegex() {
        return ToRegexConverter.getRegex(this);
    }

    /**
     * @return byval copy of initial states
     */
    public int[] getInitialStates() {
        return Arrays.copyOf(this.initialStates, this.initialStates.length);
    }

    /**
     * @return byval copy of accepting states
     */
    public int[] getAcceptingStates() {
        return Arrays.copyOf(this.acceptingStates, this.acceptingStates.length);
    }

    /**
     * @return deep values copy of transitions map
     */
    public HashMap<Integer, HashMap<Integer, int[]>> getTransitions() {
        return Utilities.getCopyOfHashMap(this.transitions);
    }

    //endregion

    //region SETTERS

    public void setDescription(String description) {
        this.description = description;
    }

    //endregion

    //region FOR CHILDREN

    /**
     * This method is called if the automaton was not already reduced
     *
     * @return reduced version of this automaton
     */
    protected abstract DFAAutomaton reduce();

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

    //endregion

    //region FUNCTIONALITY

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
                LOGGER.info("Unknown letter passed: " + letter);
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
     * This function will return boolean signifying whether this automaton accepts given word.
     * Word is passed as an ArrayList of strings that contains letters of sigma
     *
     * @param input ArrayList of strings that contains letters of sigma
     */
    public boolean acceptsWord(ArrayList<String> input) {
        return acceptsWord(input.toArray(new String[]{}));
    }

    public void invalidateCaches() {
        toStringConverter = null;
        stateMapping = sigmaMapping = null;
        LOGGER.fine("Caches invalidated");
    }

    public abstract Automaton copy();

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

    //endregion

    //region EXPORT

    /**
     * This function exports automaton to CSV file with a specified separator.
     * This will export CSV file in this format:
     * E.G.:
     * <pre>
     * [Description],,[letter1],[letter2],[letter3]
     * [IA],[state1],[trans11],[trans12],[trans13]
     * [IA],[state2],[trans21],[trans22],[trans23]
     * etc.
     * </pre>
     * Commas will be replaced by specified separator.
     * <p>
     * [IA] is initial and accepting description of the state. It is denoted by:
     * <pre>
     * initial - >
     * accepting - <
     * initial and accepting - <>
     * </pre>
     * <p>
     * If there are multiple states in [transxx], whole [transxx] will be encapsulated in "" and states will be separated by commas.
     */
    public void exportToCSV(File file, char separator) {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            if (description != null && description.length() != 0) {
                sb.append("\"").append(this.description).append("\"");
            }
            sb.append(separator);

            for (String s : sigma) {
                sb.append(separator).append("\"").append(s).append("\"");
            }
            sb.append("\n");

            for (int i = 0; i < Q.length; i++) {
                if (isAcceptingState(i)) {
                    sb.append("<");
                }
                if (isInitialState(i)) {
                    sb.append(">");
                }
                sb.append(separator).append("\"").append(Q[i]).append("\"");

                int[] curr;
                for (int letter = 0; letter < sigma.length; letter++) {
                    curr = transitions.get(i).get(letter);
                    if (curr.length > 0) {
                        sb.append(separator).append("\"").append(Q[curr[0]]);
                        for (int c = 1; c < curr.length; c++) {
                            sb.append(",").append(Q[curr[c]]);
                        }
                        sb.append("\"");
                    } else sb.append(separator);
                }
                sb.append("\n");
            }

            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calls {@link #exportToCSV(File, char)}
     * and uses default separator: ','
     */
    public void exportToCSV(File file) {
        exportToCSV(file, ',');
    }

    /**
     * This function will return an object containing all string representations of this automaton
     */
    public ToStringConverter exportToString() {
        if (toStringConverter == null) toStringConverter = new ToStringConverter(this);
        return toStringConverter;
    }

    //endregion

    //region IMPORT

    /**
     * Calls {@link #importFromCSV(Reader, char)}
     * and uses default separator: ','
     */
    public static Automaton importFromCSV(File fileToLoad) {
        return importFromCSV(fileToLoad, ',');
    }

    /**
     * This function imports automaton from CSV file with a specified separator.
     * This requires CSV file to be in this format:
     * E.G.:
     * <pre>
     * [Description],,[letter1],[letter2],[letter3]
     * [IA],[state1],[trans11],[trans12],[trans13]
     * [IA],[state2],[trans21],[trans22],[trans23]
     * etc.
     * </pre>
     * <p>
     * [IA] is initial and accepting description of the state. It is denoted by:
     * <pre>
     * initial - >
     * accepting - <
     * initial and accepting - <>
     * </pre>
     * <p>
     * If there are multiple states in [transxx], encapsulate whole [transxx] in "" and separate them by commas
     * <br>
     */
    public static Automaton importFromCSV(Reader reader, char separator) {
        try {
            BufferedReader r = new BufferedReader(reader);
            String line = r.readLine();
            ArrayList<String> sigma = new ArrayList<>();
            ArrayList<String> Q = new ArrayList<>();
            Pair<Integer, String> ret;
            ret = Utilities.getNextToken(line, 0, separator);
            String c = ret.getValue();
            if (c.length() == 0) {
                c = "";
            } else if (c.charAt(0) == '\"') {
                c = c.substring(1, c.length() - 1);
            }

            ret = Utilities.getNextToken(line, ret.getKey(), separator);
            int curr = ret.getKey();

            while (curr >= 0) {
                ret = Utilities.getNextToken(line, curr, separator);
                curr = ret.getKey();
                String t = ret.getValue();

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
                line = line.trim();
                curr = 0;
                ret = Utilities.getNextToken(line, curr, separator);
                curr = ret.getKey();
                String value = ret.getValue().trim();
                switch (value) {
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
                        LOGGER.warning("Invalid CSV format, expected <>, instead got: " + value);
                        return null;
                }

                ret = Utilities.getNextToken(line, curr, separator);
                curr = ret.getKey();
                String state = ret.getValue().trim();
                Q.add(state);
                HashMap<String, String> row = new HashMap<>();
                transitions.put(state, row);
                for (String letter : sigma) {
                    if (curr == -1) {
                        row.put(letter, "");
                        continue;
                    }

                    ret = Utilities.getNextToken(line, curr, separator);
                    curr = ret.getKey();

                    String t = ret.getValue().trim();
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

            ENFAAutomaton enfaAutomaton = new ENFAAutomaton(
                    Q.toArray(new String[0]),
                    sigma.toArray(new String[0]),
                    transitions,
                    initialString,
                    acceptingString);
            enfaAutomaton.setDescription(c);

            return enfaAutomaton;
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning(e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Calls {@link #importFromCSV(Reader, char)}
     * and uses default separator: ','
     */
    public static Automaton importFromCSV(File fileToLoad, char separator) {
        if (fileToLoad == null) {
            LOGGER.warning("Cannot import from CSV file which is null!");
            return null;
        }
        try {
            FileInputStream is = new FileInputStream(fileToLoad);
            Reader reader = new InputStreamReader(is, "UTF-8");
            return importFromCSV(reader, ',');
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning(e.getLocalizedMessage());
            return null;
        }
    }

    //endregion

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

        for (String epsilonName : epsilonNames) {
            if (epsilonName.equals(newName)) {
                LOGGER.warning("Cannot rename letter to: " + epsilonName + " because it is a mark of epsilon transition!");
                return false;
            }
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

    //region OPERATORS

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
    public Automaton getKleene() {
        UnaryOperators operators = new UnaryOperators(this);
        return operators.getKleene();
    }

    /**
     * This function will return automaton that is the result of Cartesian multiplication of states.
     *
     * @param a Automaton accepting language L1
     * @param b Automaton accepting language L2
     * @return Automaton accepting language that is intersection of L1 and L2
     */
    public static Automaton getIntersection(Automaton a, Automaton b) {
        BinaryOperators bo = new BinaryOperators(a, b);

        return bo.getIntersection();
    }

    /**
     * This function will return automaton that accepts union of languages accepted by a and b
     *
     * @param a Automaton accepting language L1
     * @param b Automaton accepting language L2
     * @return Automaton accepting language L3 = (L1+L2)
     */
    public static Automaton getUnion(Automaton a, Automaton b) {
        BinaryOperators bo = new BinaryOperators(a.getReduced(), b.getReduced());

        return bo.getUnion();
    }

    /**
     * This function returns an automaton that accepts complement of the language accepted by this automaton.
     *
     * @return automaton M, such that <b>L(M) = &Sigma;* &#8726; L(M1)</b>, where M1 is this automaton instance
     */
    public Automaton getComplement() {
        UnaryOperators uop = new UnaryOperators(this);
        return uop.getComplement();
    }

    //endregion
}
