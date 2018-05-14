package cz.cvut.fel.horovtom.automata.logic.converters;

import cz.cvut.fel.horovtom.automata.logic.Automaton;

import java.util.HashMap;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * This class is used for converting automaton to regular expression
 */
public class ToRegexConverter {
    private static final Logger LOGGER = Logger.getLogger(ToRegexConverter.class.getName());

    private static HashMap<Integer, HashMap<Integer, Integer>> transitions;
    private static int qSize, sigmaSize;
    private static int initial;
    private static int[] acceptings;
    private static String[] sigma;
    private static RegexMatrix matrix;


    public static String getRegex(Automaton a) {
        a = a.getReduced();
        HashMap<Integer, HashMap<Integer, int[]>> itTransitions = a.getTransitions();
        qSize = a.getQSize();
        sigmaSize = a.getSigmaSize();
        initial = a.getInitialStates()[0];
        acceptings = a.getAcceptingStates();
        sigma = a.getSigma();
        transitions = new HashMap<>();

        for (int i = 0; i < qSize; i++) {
            HashMap<Integer, Integer> current = new HashMap<>();
            transitions.put(i, current);
            for (int i1 = 0; i1 < sigmaSize; i1++) {
                current.put(i1, itTransitions.get(i).get(i1)[0]);
            }
        }


        matrix = new RegexMatrix(qSize, sigma, transitions, initial);
        String convert = convert();
        return convert;
    }

    private static String convert() {
        if (acceptings.length == 0)
            return "";

        int[] ordering = getOrdering();
        for (int i : ordering) {
            LOGGER.fine("Expressing state number: " + i);
            matrix.expressAndFit(i);
        }


        Regex res = matrix.getState(acceptings[0]);
        for (int i = 1; i < acceptings.length; i++) {
            res = Regex.or(res, matrix.getState(acceptings[i]));
        }

        res.simplify();
        return res.getString();
    }

    /**
     * This function will figure out the right ordering of expressing variables from the matrix.
     * <p>
     * Algorithm:
     * push all final states onto the stack
     * while stack not empty:
     * a = stack.pop()
     * stack2.push(a)
     * for symb in a:
     * if not (stack.contains(symb) or stack2.contains(symb):
     * stack.push(symb)
     * <p>
     * stack2 is the right ordering of states
     */
    private static int[] getOrdering() {
        Stack<Integer> stack = new Stack<>();
        Stack<Integer> stack2 = new Stack<>();

        for (int accepting : acceptings) {
            stack.push(accepting);
            stack2.push(accepting);
        }

        while (!stack.empty()) {
            int a = stack.pop();
            if (!stack2.contains(a))
                stack2.push(a);
            int[] ref = matrix.getReferences(a);
            for (int i : ref) {
                if (!stack.contains(i) && !stack2.contains(i))
                    stack.push(i);
            }
        }

        int[] arr = new int[stack2.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = stack2.pop();
        }
        return arr;
    }


}
