package cz.cvut.fel.horovtom.jasl.graphviz;

import cz.cvut.fel.horovtom.automata.logic.Automaton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;


class ToDotConverter {
    private static final Logger LOGGER = Logger.getLogger(ToDotConverter.class.getName());
    /**
     * This function will return a string containing the dot code to display specified automaton.
     */
    static String convertToDot(Automaton automaton) {
        StringBuilder res = new StringBuilder("digraph automaton");
        res.append(" {\n\trankdir=LR;\n\tsize=\"8,3\"\n");

        // Initial points:
        res.append("\tnode [shape = none];\n");
        int[] initialStates = automaton.getInitialStates();
        for (int i = 0; i < initialStates.length; i++) {
            res.append("\t\tqS").append(i).append(" [label=\"\"]\n");
        }


        // Accepting states:
        int[] acceptingStatesIndices = automaton.getAcceptingStates();

        // Both acc and initial:
        ArrayList<Integer> bothStates = new ArrayList<>();
        for (int initialState : initialStates) {
            for (int acceptingState : acceptingStatesIndices) {
                if (initialState == acceptingState) {
                    bothStates.add(initialState);
                    break;
                }
            }
        }

        String[] q = automaton.getQ();
        res.append("\tnode [shape = doublecircle];\n");
        for (int acceptingState : acceptingStatesIndices) {
            if (bothStates.contains(acceptingState)) continue;
            res.append("\t\t\"").append(q[acceptingState]).append("\"\n");
        }

        // Initial states marking
        res.append("\tnode [shape = circle, color = \"red\"];\n");
        for (int initialState : initialStates) {
            if (bothStates.contains(initialState)) continue;
            res.append("\t\t\"").append(q[initialState]).append("\"\n");
        }

        // Both states marking
        if (bothStates.size() > 0) res.append("\tnode [shape = doublecircle, color = \"red\"];\n");
        for (Integer bothState : bothStates) {
            res.append("\t\t\"").append(q[bothState]).append("\"\n");
        }

        res.append("\tnode [shape = circle, color = \"black\"];\n");
        // Now draw edges:
        for (int i = 0; i < initialStates.length; i++) {
            res.append("\t\tqS").append(i).append(" -> \"").append(q[initialStates[i]]).append("\" [color=\"red:invis:red\"];\n");
        }

        String[] sigma = automaton.getSigma();
        HashMap<Integer, HashMap<Integer, int[]>> transitions = automaton.getTransitions();

        for (int state = 0; state < q.length; state++) {
            HashMap<Integer, ArrayList<Integer>> edgesFromState = getEdgesFromState(state, transitions, sigma.length);
            for (int target = 0; target < q.length; target++) {
                if (!edgesFromState.containsKey(target)) continue;
                res.append("\t\t\"").append(q[state]).append("\" -> \"").append(q[target]).append("\" [label = \"");
                ArrayList<Integer> current = edgesFromState.get(target);
                for (int i = 0; i < current.size(); i++) {
                    String character = sigma[current.get(i)];
                    if (Automaton.isEpsilonName(character)) character = "&epsilon;";
                    if (i == 0) res.append(character);
                    else res.append(",").append(character);
                }
                if (current.size() == 1 && (sigma[current.get(0)].equals("eps") || sigma[current.get(0)].equals("ε")))
                    res.append("\", style=dashed];\n");
                else
                    res.append("\"];\n");
            }
        }

        res.append("}");
        return res.toString();
    }

    private static HashMap<Integer, ArrayList<Integer>> getEdgesFromState(int state, HashMap<Integer, HashMap<Integer, int[]>> transitions, int sigmaLen) {
        // key: target, value: letters
        HashMap<Integer, ArrayList<Integer>> res = new HashMap<>();
        // key: letter, value: targets
        HashMap<Integer, int[]> stateTransitions = transitions.get(state);
        for (int letter = 0; letter < sigmaLen; letter++) {
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

}
