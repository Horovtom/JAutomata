package cz.cvut.fel.horovtom.automata.logic.reducers;

import java.util.*;

public class NFAReducer {
    private class Save {
        ArrayList<String> Q, sigma;
        ArrayList<Integer> initial, accepting;
        HashMap<Integer, HashMap<Integer, int[]>> transitions;
        HashMap<Integer, ArrayList<Integer>> savedIndices;

        Save(String[] q, String[] sigma, HashMap<Integer, HashMap<Integer, int[]>> transitions, int[] initialStates, int[] acceptingStates) {
            Q = new ArrayList<>(Arrays.asList(q));
            this.sigma = new ArrayList<>(Arrays.asList(sigma));
            this.transitions = new HashMap<>();
            for (int state = 0; state < q.length; state++) {
                HashMap<Integer, int[]> row = new HashMap<>();
                for (int letter = 0; letter < sigma.length; letter++) {
                    int[] orig = transitions.get(state).get(letter);
                    int[] trans = Arrays.copyOf(orig, orig.length);
                    Arrays.sort(trans);
                    row.put(letter, trans);
                }
                this.transitions.put(state, row);
            }
            this.initial = new ArrayList<>();
            for (int initialState : initialStates) {
                if (!this.initial.contains(initialState)) this.initial.add(initialState);
            }
            this.initial.sort(Comparator.comparingInt(o -> o));

            this.accepting = new ArrayList<>();
            for (int acceptingState : acceptingStates) {
                if (!this.accepting.contains(acceptingState)) this.accepting.add(acceptingState);
            }
            this.accepting.sort(Comparator.comparingInt(o -> o));

        }

        Save() {
            this.Q = new ArrayList<>();
            this.sigma = new ArrayList<>();
            this.initial = new ArrayList<>();
            this.accepting = new ArrayList<>();
            this.transitions = new HashMap<>();
            this.savedIndices = new HashMap<>();
        }

        String getAppendedStateNames(Iterable<Integer> indices) {
            StringBuilder sb = new StringBuilder();
            for (Integer index : indices) {
                sb.append(this.Q.get(index)).append(",");
            }
            if (sb.length() > 0) {
                sb.replace(sb.length() - 1, sb.length(), "");
                return sb.toString();
            } else
                return "ERROR";
        }

        /**
         * Finds existing state in the Save and returns its index, if such state does not exist,
         * it creates new state in the Save and returns its index.
         */
        int getState(String stateName, ArrayList<Integer> indices) {
            for (int i = 0; i < Q.size(); i++) {
                if (savedIndices.get(i).equals(indices)) return i;
            }

            this.Q.add(stateName);
            int i = this.Q.size() - 1;
            savedIndices.put(i, indices);
            return i;
        }

        ArrayList<Integer> getOriginalIndices(int i) {
            return this.savedIndices.get(i);
        }

        void setAccepting(ArrayList<Integer> originalAccepting) {
            for (int i = 0; i < this.Q.size(); i++) {
                ArrayList<Integer> integers = this.savedIndices.get(i);
                for (Integer integer : integers) {
                    if (originalAccepting.contains(integer)) {
                        this.accepting.add(i);
                        break;
                    }
                }
            }
        }

        /**
         * @return index of the next unfilled row index in the transitions table, -1 if there is no such row
         */
        int getNextUnfilled() {
            return this.Q.size() > this.transitions.size() ? this.transitions.size() : -1;
        }
    }

    private final Save original;
    private Save reduced = new Save();

    public NFAReducer(String[] q, String[] sigma, HashMap<Integer, HashMap<Integer, int[]>> transitions, int[] initialStates, int[] acceptingStates) {
        original = new Save(q, sigma, transitions, initialStates, acceptingStates);
        reduced.sigma = new ArrayList<>(Arrays.asList(sigma));
        reduce();
    }

    private void reduce() {
        int current = reduced.getState(original.getAppendedStateNames(original.initial), original.initial);
        reduced.initial.add(current);

        ArrayList<ArrayList<Integer>> pq = new ArrayList<>(reduced.sigma.size());

        for (int i = 0; i < reduced.sigma.size(); i++) {
            pq.add(new ArrayList<>());
        }

        while (current != -1) {
            ArrayList<Integer> origInd = reduced.getOriginalIndices(current);

            for (Integer state : origInd) {
                for (int letter = 0; letter < reduced.sigma.size(); letter++) {
                    int[] inds = original.transitions.get(state).get(letter);
                    ArrayList<Integer> row = pq.get(letter);
                    for (int ind : inds) {
                        if (!row.contains(ind)) row.add(ind);
                    }
                    row.sort(Comparator.comparingInt(a -> a));
                }
            }

            HashMap<Integer, int[]> currentRow = new HashMap<>();
            for (int i = 0; i < reduced.sigma.size(); i++) {
                int ind = reduced.getState(original.getAppendedStateNames(pq.get(i)), new ArrayList<>(pq.get(i)));
                pq.set(i, new ArrayList<>());
                currentRow.put(i, new int[]{ind});
            }
            reduced.transitions.put(current, currentRow);

            current = reduced.getNextUnfilled();
        }

        reduced.setAccepting(original.accepting);
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getReducedTransitions() {
        //TODO: CACHING
        HashMap<Integer, HashMap<Integer, Integer>> returning = new HashMap<>();
        HashMap<Integer, HashMap<Integer, int[]>> transitions = this.reduced.transitions;

        for (int i = 0; i < this.reduced.Q.size(); i++) {
            HashMap<Integer, int[]> row = transitions.get(i);
            HashMap<Integer, Integer> newRow = new HashMap<>();
            returning.put(i, newRow);
            for (int letter = 0; letter < this.reduced.sigma.size(); letter++) {
                newRow.put(letter, row.get(letter)[0]);
            }
        }

        return returning;
    }

    public String[] getReducedQ() {
        return reduced.Q.toArray(new String[0]);
    }

    public String[] getReducedSigma() {
        return reduced.sigma.toArray(new String[0]);
    }

    public int getReducedInitial() {
        return reduced.initial.get(0);
    }

    public int[] getReducedAccepting() {
        return reduced.accepting.stream().mapToInt(a -> a).toArray();
    }
}
