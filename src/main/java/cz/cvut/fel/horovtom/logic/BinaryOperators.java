package cz.cvut.fel.horovtom.logic;

import java.util.*;
import java.util.logging.Logger;

public class BinaryOperators {
    private static final Logger LOGGER = Logger.getLogger(BinaryOperators.class.getName());
    private final Automaton a, b;
    private int aEps, bEps;
    private final HashMap<Integer, HashMap<Integer, int[]>> aTransitions;
    private final HashMap<Integer, HashMap<Integer, int[]>> bTransitions;

    private String[] commonQ;
    /**
     * Common sigma has united epsilon transition even if neither one has it
     */
    private String[] commonSigma;
    private HashMap<String, Integer> commonSigmaMap;
    /**
     * Keys: Indices in commonSigma
     * Vals: Indices in corresponding automaton sigmas
     */
    private HashMap<Integer, Integer> bSigmaMap, aSigmaMap;
    private Automaton L1L2;
    private Automaton union;
    private Automaton intersection;

    BinaryOperators(Automaton a, Automaton b) {
        this.a = a;
        this.b = b;

        aTransitions = a.getTransitions();
        bTransitions = b.getTransitions();

        createCommon();
    }

    private void createCommon() {
        //States
        String[] Q = new String[a.getQSize() + b.getQSize()];
        String[] aQ = a.getQ();
        String[] bQ = b.getQ();
        int currIndex = 0;
        for (String anAQ : aQ) {
            Q[currIndex++] = anAQ;
        }
        for (String aBQ : bQ) {
            Q[currIndex++] = aBQ;
        }

        //Sigma
        ArrayList<String> sigmaList = new ArrayList<>();
        HashMap<String, Integer> sigmaIndices = new HashMap<>();
        aSigmaMap = new HashMap<>();
        bSigmaMap = new HashMap<>();

        String[] aSigma = a.getSigma();
        int aEpsIndex = -1, bEpsIndex = -1;
        if (a.hasEpsilonTransitions()) {
            outer:
            for (int i = 0; i < aSigma.length; i++) {
                for (String epsilonName : Automaton.epsilonNames) {
                    if (epsilonName.equals(aSigma[i])) {
                        aEpsIndex = i;
                        break outer;
                    }
                }
            }
        }

        String[] bSigma = b.getSigma();
        if (b.hasEpsilonTransitions()) {
            outer:
            for (int i = 0; i < bSigma.length; i++) {
                for (String epsilonName : Automaton.epsilonNames) {
                    if (epsilonName.equals(bSigma[i])) {
                        bEpsIndex = i;
                        break outer;
                    }
                }
            }
        }

        //Unite epsilon Transitions:
        sigmaList.add("eps");

        if (aEpsIndex != -1) {
            this.aSigmaMap.put(0, aEpsIndex);
        }
        if (bEpsIndex != -1) {
            this.bSigmaMap.put(0, bEpsIndex);
        }

        for (int i = 0; i < aSigma.length; i++) {
            if (i == aEpsIndex) continue;
            int ta = sigmaList.size();
            sigmaIndices.put(aSigma[i], ta);
            sigmaList.add(aSigma[i]);
            this.aSigmaMap.put(ta, i);
        }
        for (int i = 0; i < bSigma.length; i++) {
            if (i == bEpsIndex) continue;
            if (!sigmaIndices.containsKey(bSigma[i])) {
                int ta = sigmaList.size();
                sigmaIndices.put(bSigma[i], ta);
                sigmaList.add(bSigma[i]);
                this.bSigmaMap.put(ta, i);
            } else {
                this.bSigmaMap.put(sigmaIndices.get(bSigma[i]), i);
            }
        }

        String[] sigma = sigmaList.toArray(new String[]{});
        this.commonQ = Q;
        this.commonSigma = sigma;
        this.commonSigmaMap = sigmaIndices;
        this.aEps = aEpsIndex;
        this.bEps = bEpsIndex;
    }

    public Automaton getL1L2() {
        if (L1L2 == null) createL1L2();
        return L1L2.copy();
    }

    private void createL1L2() {
        HashMap<Integer, HashMap<Integer, int[]>> transitions = new HashMap<>(this.commonQ.length);
        int[] aAccepting = a.getAcceptingStates();
        int[] bInitial = b.getInitialStates();

        int breakingPoint = a.getQSize();

        for (int state = 0; state < this.commonQ.length; state++) {
            HashMap<Integer, int[]> currRow = new HashMap<>();
            if (state < breakingPoint) {
                //It is an 'A' state
                boolean acceptingState = false;
                //Is it accepting state?
                for (int anAccepting : aAccepting) {
                    if (anAccepting == state) {
                        acceptingState = true;
                        break;
                    }
                }

                //Letters
                for (int letter = 0; letter < this.commonSigma.length; letter++) {
                    if (letter == 0) {
                        //Eps transition
                        ArrayList<Integer> targs = new ArrayList<>();

                        if (this.aEps != -1) {
                            for (int i : aTransitions.get(state).get(this.aSigmaMap.get(letter))) {
                                targs.add(i);
                            }
                        }

                        if (acceptingState) {
                            for (int i : bInitial) {
                                targs.add(breakingPoint + i);
                            }
                        }

                        currRow.put(0, targs.stream().mapToInt(a -> a).toArray());
                        continue;
                    }
                    if (this.aSigmaMap.containsKey(letter)) {
                        int[] ints = aTransitions.get(state).get(this.aSigmaMap.get(letter));
                        currRow.put(letter, Arrays.copyOf(ints, ints.length));
                    } else {
                        currRow.put(letter, new int[0]);
                    }
                }
            } else {
                //It is a 'B' state
                //Letters
                for (int letter = 0; letter < this.commonSigma.length; letter++) {
                    if (this.bSigmaMap.containsKey(letter)) {
                        int[] ints = bTransitions.get(state - breakingPoint).get(this.bSigmaMap.get(letter));
                        for (int i = 0; i < ints.length; i++) {
                            ints[i] += breakingPoint;
                        }
                        currRow.put(letter, Arrays.copyOf(ints, ints.length));
                    } else {
                        currRow.put(letter, new int[0]);
                    }
                }
            }

            transitions.put(state, currRow);
        }

        int[] initials = a.getInitialStates();
        int[] acceptingStates = b.getAcceptingStates();
        for (int i = 0; i < acceptingStates.length; i++) {
            acceptingStates[i] += breakingPoint;
        }

        this.L1L2 = new ENFAAutomaton(this.commonQ, this.commonSigma, transitions, initials, acceptingStates);
    }

    public Automaton getUnion() {
        if (union == null) createUnion();
        return union.copy();
    }

    private void createUnion() {
        String[] sigma = Arrays.copyOf(commonSigma, commonSigma.length);
        String[] Q = new String[a.getQSize() + b.getQSize() + 1];
        Q[0] = "START";
        int currToWrite = 1;
        String[] aQ = a.getQ();
        for (String s : aQ) {
            Q[currToWrite++] = s;
        }
        int bStart = currToWrite;
        String[] bQ = b.getQ();
        for (String s : bQ) {
            Q[currToWrite++] = s;
        }

        HashMap<Integer, HashMap<Integer, int[]>> transitions = new HashMap<>();
        HashMap<Integer, int[]> currRow = new HashMap<>();
        currRow.put(0, new int[]{1, bStart});
        transitions.put(0, currRow);
        //a states
        for (int state = 1; state < Q.length; state++) {
            currRow = new HashMap<>();
            //letters
            for (int letter = 0; letter < sigma.length; letter++) {
                if (state < bStart && aSigmaMap.containsKey(letter)) {
                    Integer aLetter = aSigmaMap.get(letter);
                    int[] ints = aTransitions.get(state - 1).get(aLetter);
                    int[] newInts = Arrays.copyOf(ints, ints.length);
                    for (int i = 0; i < newInts.length; i++) {
                        newInts[i]++;
                    }

                    currRow.put(letter, newInts);
                } else if (state >= bStart && bSigmaMap.containsKey(letter)) {
                    Integer bLetter = bSigmaMap.get(letter);
                    int[] ints = bTransitions.get(state - bStart).get(bLetter);
                    int[] newInts = Arrays.copyOf(ints, ints.length);
                    for (int i = 0; i < newInts.length; i++) {
                        newInts[i] += bStart;
                    }

                    currRow.put(letter, newInts);
                } else {
                    currRow.put(letter, new int[0]);
                }
            }

            transitions.put(state, currRow);
        }

        int[] initials = new int[]{0};
        int[] aAcc = a.getAcceptingStates();
        for (int i = 0; i < aAcc.length; i++) {
            aAcc[i]++;
        }
        int[] bAcc = b.getAcceptingStates();
        for (int i = 0; i < bAcc.length; i++) {
            bAcc[i] += bStart;
        }
        int[] accepting = new int[aAcc.length + bAcc.length];
        System.arraycopy(aAcc, 0, accepting, 0, aAcc.length);
        System.arraycopy(bAcc, 0, accepting, aAcc.length, bAcc.length);

        this.union = new ENFAAutomaton(Q, sigma, transitions, initials, accepting);
    }

    public Automaton getIntersection() {
        if (intersection == null) {
            createIntersection();
        }
        return intersection.copy();
    }

    private Automaton getEmptyAutomaton() {
        String[] Q = {"0"};
        String[] sigma = new String[0];
        HashMap<Integer, HashMap<Integer, int[]>> transitions = new HashMap<>();
        int[] initial = {0};
        int[] accepting = new int[0];
        return new ENFAAutomaton(Q, sigma, transitions, initial, accepting);
    }

    /**
     * Concatenates the two strings in the right way to create new composite state name
     *
     * @param a nullable string
     * @param b nullable string
     */
    private String getCompositeName(String a, String b) {
        if (a == null) {
            if (b == null) {
                return "∅";
            } else {
                return "∅," + b;
            }
        } else {
            if (b == null) {
                return a + ",∅";
            } else {
                return a + "," + b;
            }
        }
    }

    private void createIntersection() {
        DFAAutomaton a = this.a.getReduced();
        DFAAutomaton b = this.b.getReduced();
        HashMap<Integer, HashMap<Integer, int[]>> aTransitions = a.getTransitions();
        HashMap<Integer, HashMap<Integer, int[]>> bTransitions = b.getTransitions();
        String[] aSigma = a.getSigma();
        String[] bSigma = b.getSigma();
        String[] aQ = a.getQ();
        String[] bQ = b.getQ();

        //They can have different sigmas, but there has to be at least one letter that is the same,
        // otherwise it will be empty automaton

        //Create common sigma:
        //These maps hold indices of real letters in automatons a and b from commonSigma indices
        ArrayList<Integer> aSigmaMap = new ArrayList<>();
        ArrayList<Integer> bSigmaMap = new ArrayList<>();
        ArrayList<String> commonSigma = new ArrayList<>();

        for (int i = 0; i < bSigma.length; i++) {
            for (int i1 = 0; i1 < aSigma.length; i1++) {
                if (aSigma[i1].equals(bSigma[i])) {
                    commonSigma.add(aSigma[i1]);
                    aSigmaMap.add(i1);
                    bSigmaMap.add(i);
                    break;
                }
            }
        }

        if (commonSigma.size() == 0) {
            intersection = getEmptyAutomaton();
            return;
        } else if (commonSigma.size() != aSigma.length || aSigma.length != bSigma.length) {
            LOGGER.warning("Automata had different sigma's. Creating their intersection from common letters");
        }

        /*
          Array that holds indices of the new stateName + 1
         */
        int[][] statesIndices = new int[a.getQSize()][b.getQSize()];
        ArrayList<Integer> stateMapA = new ArrayList<>();
        ArrayList<Integer> stateMapB = new ArrayList<>();
        ArrayList<String> stateNames = new ArrayList<>();
        HashMap<Integer, HashMap<Integer, Integer>> transitions = new HashMap<>();

        Queue<Integer> toComplete = new LinkedList<>();

        //Initial state
        int initialA = a.getInitialStates()[0];
        int initialB = b.getInitialStates()[0];
        stateNames.add(getCompositeName(aQ[initialA], bQ[initialB]));
        stateMapA.add(initialA);
        stateMapB.add(initialB);
        statesIndices[initialA][initialB] = 1;
        toComplete.offer(0);

        //States
        while (!toComplete.isEmpty()) {
            int toDo = toComplete.poll();
            int aInd = stateMapA.get(toDo);
            int bInd = stateMapB.get(toDo);
            HashMap<Integer, Integer> curr = new HashMap<>();
            transitions.put(toDo, curr);

            for (int letter = 0; letter < commonSigma.size(); letter++) {
                int aLetInd = aSigmaMap.get(letter);
                int bLetInd = bSigmaMap.get(letter);
                int aTarg = -1, bTarg = -1;
                if (aInd != -1) {
                    aTarg = aTransitions.get(aInd).get(aLetInd)[0];
                }
                if (bInd != -1) {
                    bTarg = bTransitions.get(bInd).get(bLetInd)[0];
                }
                if (statesIndices[aTarg][bTarg] == 0) {
                    //Create new state
                    toComplete.offer(stateNames.size());
                    stateNames.add(getCompositeName(aQ[aTarg], bQ[bTarg]));
                    stateMapA.add(aTarg);
                    stateMapB.add(bTarg);
                    statesIndices[aTarg][bTarg] = stateNames.size();
                    curr.put(letter, statesIndices[aTarg][bTarg] - 1);
                } else {
                    //It already exists
                    curr.put(letter, statesIndices[aTarg][bTarg] - 1);
                }

            }
        }

        ArrayList<Integer> acceptingStates = new ArrayList<>();
        int[] aAcc = a.getAcceptingStates();
        int[] bAcc = b.getAcceptingStates();
        for (int aC : aAcc) {
            for (int bC : bAcc) {
                if (statesIndices[aC][bC] != 0) acceptingStates.add(statesIndices[aC][bC] - 1);
            }
        }

        intersection = new DFAAutomaton(stateNames.toArray(new String[]{}), commonSigma.toArray(new String[]{}), transitions, 0, acceptingStates.stream().mapToInt(c -> c).toArray());
        LOGGER.fine("Intersection created: \n" + intersection.toString());
    }
}
