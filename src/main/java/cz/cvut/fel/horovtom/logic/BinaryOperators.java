package cz.cvut.fel.horovtom.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BinaryOperators {
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
                    currRow.put(letter, Arrays.copyOf(ints, ints.length));
                } else if (state >= bStart && bSigmaMap.containsKey(letter)) {
                    Integer bLetter = bSigmaMap.get(letter);
                    int[] ints = bTransitions.get(state - bStart).get(bLetter);
                    currRow.put(letter, Arrays.copyOf(ints, ints.length));
                } else {
                    currRow.put(letter, new int[0]);
                }
            }

            //FIXME: DEBUG PRINT
            System.out.print("Row: ");
            for (int i = 0; i < sigma.length; i++) {

                int[] ints = currRow.get(i);
                for (int anInt : ints) {
                    System.out.print(anInt + ",");
                }
                System.out.print(" ; ");
            }
            System.out.println("");

            transitions.put(state, currRow);
        }

        int[] initials = new int[]{0};
        int[] aAcc = a.getAcceptingStates();
        int[] bAcc = b.getAcceptingStates();
        int[] accepting = new int[aAcc.length + bAcc.length];
        System.arraycopy(aAcc, 0, accepting, 0, aAcc.length);
        System.arraycopy(bAcc, 0, accepting, aAcc.length, bAcc.length);


        this.union = new ENFAAutomaton(Q, sigma, transitions, initials, accepting);
    }
}
