package cz.cvut.fel.horovtom.logic.reducers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

public class DFAReducer {
    private static Logger LOGGER = Logger.getLogger(DFAReducer.class.getName());

    private final HashMap<Integer, HashMap<Integer, Integer>> originalTransitions;
    private final int originalInitial;
    private final int[] originalAccepting;
    private final int QSize, sigmaSize;
    private HashMap<Integer, HashMap<Integer, Integer>> reducedTransitions;
    private int reducedInitial;
    private int[] reducedAccepting;
    private ArrayList<ArrayList<Integer>> reductionTable;
    private int reductionTableInitial;
    private ArrayList<Integer> reductionTableAccepting;
    private String reductionTableInString;
    private String[] reducedQ;

    public DFAReducer(HashMap<Integer, HashMap<Integer, Integer>> originalTransitions, int initial, int[] accepting) {
        this.originalTransitions = originalTransitions;
        this.originalInitial = initial;
        this.originalAccepting = accepting;
        this.QSize = this.originalTransitions.keySet().size();
        this.sigmaSize = this.originalTransitions.get(initial).keySet().size();
        reduce();
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getReducedTransitions() {
        return reducedTransitions;
    }

    public int[] getReducedAccepting() {
        return Arrays.copyOf(this.reducedAccepting, this.reducedAccepting.length);
    }

    public int getReducedInitial() {
        return this.reducedInitial;
    }

    private void reduce() {
        if (this.reducedTransitions != null) {
            LOGGER.finer("Trying to further reduce already reduced reducer");
            return;
        }
        //TODO: IMPLEMENT SAVING PROCESS

        initializeReductionTable();
        while (true) {
            if (nextColumn()) break;
        }

        int namesCol = this.reductionTable.get(0).size() - 2 - this.sigmaSize;
        this.reducedTransitions = new HashMap<>();

        ArrayList<Integer> newAccepting = new ArrayList<>();
        ArrayList<String> reducedStates = new ArrayList<>();


        for (int state = 0; state < this.reductionTable.size(); state++) {
            if (this.reducedTransitions.containsKey(this.reductionTable.get(state).get(namesCol))) continue;
            HashMap<Integer, Integer> curr = new HashMap<>();
            for (int letter = 0; letter < this.sigmaSize; letter++) {
                curr.put(letter, this.reductionTable.get(state).get(namesCol + letter + 1));
            }

            int currentStateKey = this.reductionTable.get(state).get(namesCol);
            this.reducedTransitions.put(currentStateKey, curr);
            reducedStates.add(String.valueOf(currentStateKey));
            if (this.reductionTableInitial == state) {
                reducedInitial = currentStateKey;
            }
            if (this.reductionTableAccepting.contains(state)) {
                newAccepting.add(currentStateKey);
            }
        }

        this.reducedQ = reducedStates.toArray(new String[0]);

        this.reducedAccepting = newAccepting.stream().mapToInt(a -> a).toArray();

        LOGGER.fine("Reduced automaton reduction table: \n" + getReductionTableInString());
    }

    private void initializeReductionTable() {
        this.reductionTable = new ArrayList<>();
        removeUnreachables();
        int states = this.reductionTable.size();
        for (int state = 0; state < states; state++) {
            if (this.reductionTableAccepting.contains(state)) this.reductionTable.get(state).add(1);
            else this.reductionTable.get(state).add(0);
        }
    }

    /**
     * Adds one column to the reduction table
     *
     * @return true if the reduction table is finished
     */
    private boolean nextColumn() {
        int namesColumn = this.reductionTable.get(0).size() - 1;
        int current = 0;
        HashMap<Integer, Integer> map = new HashMap<>();
        boolean ret = true;

        for (ArrayList<Integer> state : this.reductionTable) {
            int[] row = new int[this.sigmaSize + 1];
            row[0] = state.get(namesColumn);
            for (int letter = 0; letter < this.sigmaSize; letter++) {
                int to = state.get(letter);
                int toAdd = this.reductionTable.get(to).get(namesColumn);
                state.add(toAdd);
                row[letter + 1] = toAdd;
            }

            int hash = getRowHash(row);
            if (!map.containsKey(hash)) {
                map.put(hash, current++);
            }
            int toAdd = map.get(hash);
            state.add(toAdd);

            if (toAdd != state.get(namesColumn)) ret = false;
        }

        return ret;
    }

    private int getRowHash(int[] a) {
        return Arrays.hashCode(a);
    }

    /**
     * Removes unreachable states and saves the result to the reduction table
     */
    private void removeUnreachables() {
        boolean[] used = new boolean[QSize];
        used[originalInitial] = true;
        reachabilityCheck(originalInitial, used);

        int[] references = new int[used.length];
        int curr = 0;
        this.reductionTableAccepting = new ArrayList<>();
        for (int i = 0; i < used.length; i++) {
            if (used[i]) {
                references[i] = curr;
                if (this.originalInitial == i) {
                    this.reductionTableInitial = curr;
                }
                for (int i1 : this.originalAccepting) {
                    if (i == i1) {
                        reductionTableAccepting.add(curr);
                        break;
                    }
                }
                curr++;

            } else
                references[i] = -1;
        }


        for (int i = 0; i < used.length; i++) {
            if (references[i] != -1) {
                ArrayList<Integer> currRow = new ArrayList<>();
                this.reductionTable.add(currRow);
                for (int letter = 0; letter < this.sigmaSize; letter++) {
                    currRow.add(references[this.originalTransitions.get(i).get(letter)]);
                }
            }
        }
    }

    private void reachabilityCheck(int current, boolean[] used) {
        HashMap<Integer, Integer> currentRow = this.originalTransitions.get(current);
        for (int i = 0; i < this.sigmaSize; i++) {
            int child = currentRow.get(i);
            if (!used[child]) {
                used[child] = true;
                reachabilityCheck(child, used);
            }
        }
    }

    private String getReductionTableInString() {
        StringBuilder res = new StringBuilder();
        for (ArrayList<Integer> integers : this.reductionTable) {
            for (Integer integer : integers) {
                res.append(integer).append(" ");
            }
            res.append("\n");
        }
        return res.toString();
    }

    public boolean wasReduced() {
        return this.reducedQ.length == this.QSize;
    }

    public String[] getReducedQ() {
        return Arrays.copyOf(reducedQ, reducedQ.length);
    }
}
