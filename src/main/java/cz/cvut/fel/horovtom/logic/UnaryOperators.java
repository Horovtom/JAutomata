package cz.cvut.fel.horovtom.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

public class UnaryOperators {
    private static final Logger LOGGER = Logger.getLogger(UnaryOperators.class.getName());
    private final Automaton a;
    private final int aEps;

    private Automaton kleeny;

    public UnaryOperators(Automaton a) {
        this.a = a;
        int foundEps = -1;

        //Search for epsTransitions
        if (a.hasEpsilonTransitions()) {
            String[] sigma = a.getSigma();
            outer:
            for (int i = 0; i < sigma.length; i++) {
                for (String epsilonName : Automaton.epsilonNames) {
                    if (sigma[i].equals(epsilonName)) {
                        foundEps = i;
                        break outer;
                    }
                }
            }
        }
        aEps = foundEps;

    }

    private void createKleeny() {
        HashMap<Integer, HashMap<Integer, int[]>> transitions = a.getTransitions();
        String[] q = a.getQ();
        String[] sigma = a.getSigma();
        int[] acceptingStates = a.getAcceptingStates();
        int[] initialStates = a.getInitialStates();
        HashMap<Integer, HashMap<Integer, int[]>> newTransitions = new HashMap<>();

        int eps = aEps;

        if (aEps == -1) {
            //Add eps transitions
            String[] newSigma = new String[sigma.length + 1];
            newSigma[0] = "eps";

            System.arraycopy(sigma, 0, newSigma, 1, sigma.length);

            for (int i = 0; i < q.length; i++) {
                //Letters
                HashMap<Integer, int[]> row = new HashMap<>();
                row.put(0, new int[0]);
                for (int i1 = 0; i1 < sigma.length; i1++) {
                    row.put(i1 + 1, transitions.get(i).get(i1));
                }
                newTransitions.put(i, row);
            }
            transitions = newTransitions;
            sigma = newSigma;
            eps = 0;
        }


        for (int acceptingState : acceptingStates) {
            int[] targs = transitions.get(acceptingState).get(eps);
            HashSet<Integer> newTargs = new HashSet<>();
            for (int initialState : initialStates) {
                newTargs.add(initialState);
            }
            for (int targ : targs) {
                newTargs.add(targ);
            }
            int[] newTargsArr = newTargs.stream().mapToInt(a -> a).toArray();
            Arrays.sort(newTargsArr);
            transitions.get(acceptingState).remove(eps);
            transitions.get(acceptingState).put(eps, newTargsArr);
        }

        kleeny = new ENFAAutomaton(q, sigma, transitions, initialStates, acceptingStates);
    }

    public Automaton getKleeny() {
        if (kleeny == null) {
            LOGGER.fine("Creating kleeny automaton for " + a);
            createKleeny();
        }
        return kleeny.copy();
    }


}
