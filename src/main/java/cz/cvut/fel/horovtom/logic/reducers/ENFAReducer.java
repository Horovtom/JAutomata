package cz.cvut.fel.horovtom.logic.reducers;

import java.util.*;

public class ENFAReducer {
    private final HashMap<Integer, int[]> epsilonTransitions;
    private final int[] originalAccepting;
    private final HashMap<Integer, HashMap<Integer, int[]>> transitions;

    private Set<Integer> newAccepting = new HashSet<>();

    private int[] reducedAccepting;

    private ArrayList<int[]> states = new ArrayList<>();

    private ArrayList<String> stateNames = new ArrayList<>();

    private String[] sigma, oldQ;

    private HashMap<Integer, int[]> epsilonClosures = new HashMap<>();

    private HashMap<Integer, HashMap<Integer, int[]>> reducedTransitions;

    /**
     * @param epsilonColumn index of epsilon letter in transitions HashMap
     */
    public ENFAReducer(String[] Q, String[] sigma, HashMap<Integer, HashMap<Integer, int[]>> transitions, int[] initialStates, int[] acceptingStates, int epsilonColumn) {
        this.transitions = transitions;
        this.sigma = new String[sigma.length - 1];
        int currentLetter = 0;
        for (int i = 0; i < this.sigma.length; i++) {
            if (currentLetter == epsilonColumn) currentLetter++;
            this.sigma[i] = sigma[currentLetter++];
        }
        this.oldQ = Q;
        originalAccepting = acceptingStates;
        epsilonTransitions = new HashMap<>();

        for (int i = 0; i < Q.length; i++) {
            epsilonTransitions.put(i, transitions.get(i).get(epsilonColumn));
        }

        //Let us count:
        Set<Integer> initialState = new HashSet<>();
        for (int state : initialStates) {
            int[] curr = getEpsilonClosure(state);
            for (int i : curr) {
                initialState.add(i);
            }
        }
        int initialIndex = getStateIndex(initialState);
        reducedTransitions = new HashMap<>();
        Queue<Integer> q = new LinkedList<>();
        q.add(initialIndex);

        while (!q.isEmpty()) {
            int curr = q.poll();
            int stateCount = stateNames.size();
            HashMap<Integer, int[]> currentRow = new HashMap<>();
            reducedTransitions.put(curr, currentRow);
            currentLetter = 0;
            for (int i = 0; i < this.sigma.length; i++) {
                if (currentLetter == epsilonColumn) {
                    currentLetter++;
                }
                int stateFromTransitions = getStateFromTransitions(curr, currentLetter++);
                if (stateFromTransitions == -1) {
                    currentRow.put(i, new int[0]);
                } else {
                    currentRow.put(i, new int[]{stateFromTransitions});

                    if (stateCount < stateNames.size()) {
                        stateCount = stateNames.size();
                        q.add(stateFromTransitions);
                    }
                }
            }
        }
        reducedAccepting = newAccepting.stream().mapToInt(a -> a).toArray();
        Arrays.sort(reducedAccepting);
    }

    public int[] getAccepting() {
        return reducedAccepting;
    }

    public String[] getQ() {
        return stateNames.toArray(new String[0]);
    }

    public String[] getSigma() {
        return sigma;
    }

    public HashMap<Integer, HashMap<Integer, int[]>> getTransitions() {
        return reducedTransitions;
    }

    public int getInitial() {
        return 0;
    }

    private int getStateFromTransitions(int currState, int transition) {
        int[] oldStatesSet = states.get(currState);
        Set<Integer> s = new HashSet<>();
        for (int i : oldStatesSet) {
            int[] targs = transitions.get(i).get(transition);
            for (int targ : targs) {
                int[] closure = getEpsilonClosure(targ);
                for (int i1 : closure) {
                    s.add(i1);
                }
            }
        }

        return s.size() != 0 ? getStateIndex(s) : -1;
    }

    private int getStateIndex(Set<Integer> set) {
        int[] arr = set.stream().mapToInt(a -> a).toArray();
        Arrays.sort(arr);
        for (int i = 0; i < states.size(); i++) {
            if (Arrays.equals(states.get(i), arr)) {
                return i;
            }
        }
        outer:
        for (int accepting : originalAccepting) {
            for (int i = 0; i < arr.length && arr[i] <= accepting; i++) {
                if (arr[i] == accepting) {
                    newAccepting.add(states.size());
                    break outer;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length - 1; i++) {
            sb.append(oldQ[arr[i]]).append(",");
        }
        sb.append(oldQ[arr[arr.length - 1]]);
        stateNames.add(sb.toString());

        states.add(arr);
        return states.size() - 1;
    }

    private int[] getEpsilonClosure(int state) {
        //If it was already calculated, return it
        if (epsilonClosures.get(state) != null) return epsilonClosures.get(state);

        Set<Integer> current = new HashSet<>();
        Queue<Integer> q = new LinkedList<>();
        current.add(state);
        q.add(state);

        while (!q.isEmpty()) {
            int curr = q.poll();
            for (int i : epsilonTransitions.get(curr)) {
                if (current.add(i)) q.add(i);
            }
        }

        epsilonClosures.put(state, current.stream().mapToInt(a -> a).toArray());

        return epsilonClosures.get(state);
    }
}
