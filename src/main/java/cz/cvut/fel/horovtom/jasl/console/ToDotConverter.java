package cz.cvut.fel.horovtom.jasl.console;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;

import java.util.ArrayList;
import java.util.HashMap;

public class ToDotConverter {

    /**
     * This function will return a string containing the dot code to display specified automaton.
     */
    public static String convertToDot(Automaton automaton) {
        StringBuilder res = new StringBuilder("digraph automaton");
        res.append(" {\n\trankdir=LR;\n\tsize=\"8,5\"\n");

        // Initial points:
        res.append("\tnode [shape = none];\n");
        int[] initialStates = automaton.getInitialStates();
        for (int i = 0; i < initialStates.length; i++) {
            res.append("\t\tqS").append(i).append(" [label=\"\"]\n");
        }

        // Accepting states:
        int[] acceptingStatesIndices = automaton.getAcceptingStates();
        String[] q = automaton.getQ();
        res.append("\tnode [shape = doublecircle];\n");
        for (int acceptingStatesIndex : acceptingStatesIndices) {
            res.append("\t\t").append(q[acceptingStatesIndex]).append("\n");
        }

        res.append("\tnode [shape = circle];\n");
        // Now draw edges:
        for (int i = 0; i < initialStates.length; i++) {
            res.append("\t\tqS").append(i).append(" -> ").append(q[initialStates[i]]).append(" [color=\"red:invis:red\"];\n");
        }

        String[] sigma = automaton.getSigma();
        HashMap<Integer, HashMap<Integer, int[]>> transitions = automaton.getTransitions();

        for (int state = 0; state < q.length; state++) {
            HashMap<Integer, ArrayList<Integer>> edgesFromState = getEdgesFromState(state, transitions, sigma.length);
            for (int target = 0; target < q.length; target++) {
                if (!edgesFromState.containsKey(target)) continue;
                res.append("\t\t").append(q[state]).append(" -> ").append(q[target]).append(" [label = \"");
                ArrayList<Integer> current = edgesFromState.get(target);
                for (int i = 0; i < current.size(); i++) {
                    String character = sigma[current.get(i)];
                    if (i == 0) res.append(character);
                    else res.append(",").append(character);
                }
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

    public static void main(String[] args) {
        //thisIsUgly();

//        try {
        DFAAutomaton dfaAutomaton = AutomatonSamples.DFASamples.startEndSame();
        System.out.println(convertToDot(dfaAutomaton));
//        } catch (FileNotFoundException | UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

    }

    private static void thisIsUgly() {
        DFAAutomaton dfaAutomaton = AutomatonSamples.DFASamples.lolipop();
        System.out.println(convertToDot(dfaAutomaton));
    }

}
