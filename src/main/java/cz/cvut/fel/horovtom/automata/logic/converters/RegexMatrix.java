package cz.cvut.fel.horovtom.automata.logic.converters;

import java.util.HashMap;
import java.util.HashSet;

public class RegexMatrix {
    private final int qSize;
    private final Regex[][] matrix;


    RegexMatrix(int qSize, String[] sigma, HashMap<Integer, HashMap<Integer, Integer>> transitions, int initial) {
        this.qSize = qSize;
        matrix = new Regex[qSize][qSize + 1];

        //Initialize regexes
        matrix[initial][0] = new Regex("ε");

        for (int state = 0; state < qSize; state++) {
            for (int letterIndex = 0; letterIndex < sigma.length; letterIndex++) {
                int targ = transitions.get(state).get(letterIndex);
                if (matrix[targ][state + 1] != null) {
                    matrix[targ][state + 1] = Regex.or(matrix[targ][state + 1], new Regex(sigma[letterIndex]));
                } else {
                    matrix[targ][state + 1] = new Regex(sigma[letterIndex]);
                }
            }
        }
    }

    /**
     * This function will express an variable from matrix and fit it into the other columns.
     *
     * @param index Has to be in range
     */
    public void expressAndFit(int index) {
        express(index);
        fit(index);
    }

    /**
     * This will fit specified row into all states on index
     */
    private void fit(int index) {
        int[] references = innerGetReferences(index);
        for (int i = 0; i < qSize; i++) {
            if (index == i || matrix[i][index + 1] == null) continue;

            if (references.length > 0) {
                for (int reference : references) {
                    Regex indexCol = Regex.concat(matrix[index][reference], matrix[i][index + 1]);
                    matrix[i][reference] = Regex.or(matrix[i][reference], indexCol);
                }
                matrix[i][index + 1] = null;
            } else {
                Regex indexCol = Regex.concat(matrix[index][0], matrix[i][index + 1]);
                matrix[i][index + 1] = null;
                matrix[i][0] = indexCol;
            }
        }
    }

    private int[] innerGetReferences(int index) {
        HashSet<Integer> ref = new HashSet<>();
        for (int i = 0; i < qSize + 1; i++) {
            if (matrix[index][i] != null) ref.add(i);
        }
        return ref.stream().mapToInt(a -> a).toArray();
    }

    /**
     * This will express the specified state from table and return its regex
     */
    private void express(int index) {
        if (matrix[index][index + 1] == null)
            return;

        Regex toApp = Regex.kleene(matrix[index][index + 1]);
        for (int i = 0; i < qSize + 1; i++) {
            if (matrix[index][i] == null) continue;
            if (i == index + 1) {
                matrix[index][i] = null;
            } else {
                matrix[index][i] = Regex.concat(matrix[index][i], toApp);
            }
        }
    }


    /**
     * This will return all the indices, that are referenced by specified row
     */
    public int[] getReferences(int row) {
        HashSet<Integer> ref = new HashSet<>();
        for (int i = 0; i < qSize; i++) {
            if (matrix[row][i + 1] != null) ref.add(i);
        }
        return ref.stream().mapToInt(a -> a).toArray();
    }

    /**
     * Returns regex in epsilon column of specified state
     */
    public Regex getState(int state) {
        return matrix[state][0];
    }
}
