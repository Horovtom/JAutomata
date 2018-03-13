package cz.cvut.fel.horovtom.logic.reducers;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class NFAReducerTest {

    /**
     * This function tests the NFAReducer on simple input:
     * <hr>
     * //TODO: IMPLE
     * <p>
     * <hr>
     * <pre>
     *
     *
     *
     * </pre>
     */
    @Test
    public void testSimple() {
        //TODO: IMPL

    }


    /**
     * This function tests the NFAReducer on intermediate input:
     * <hr>
     * <table><tr><th><br></th><th></th><th>a</th><th>b</th></tr><tr><td>→</td><td>0</td><td>∅</td><td>∅</td></tr><tr><td>→</td><td>1</td><td>1</td><td>3</td></tr><tr><td>←</td><td>2</td><td>∅</td><td>∅</td></tr><tr><td></td><td>3</td><td>2,1</td><td>1</td></tr></table>
     * <hr>
     * <pre>
     * +---+---+-----+---+
     * |   |   | a   | b |
     * +---+---+-----+---+
     * | → | 0 | ∅   | ∅ |
     * +---+---+-----+---+
     * | → | 1 | 1   | 3 |
     * +---+---+-----+---+
     * | ← | 2 | ∅   | ∅ |
     * +---+---+-----+---+
     * |   | 3 | 2,1 | 1 |
     * +---+---+-----+---+
     *
     *  </pre>
     * After conversion to DFA it is:
     * <p>
     * <hr>
     * <table><tr><th><br></th><th></th><th>a</th><th>b</th></tr><tr><td>→</td><td>0,1<br></td><td>1</td><td>3</td></tr><tr><td></td><td>1</td><td>1</td><td>3</td></tr><tr><td></td><td>3<br></td><td>1,2</td><td>1</td></tr><tr><td>←</td><td>1,2<br></td><td>1</td><td>3</td></tr></table>
     * <hr>
     * <pre>
     *  +---+-----+-----+---+
     *  |   |     | a   | b |
     *  +---+-----+-----+---+
     *  | → | 0,1 | 1   | 3 |
     *  +---+-----+-----+---+
     *  |   | 1   | 1   | 3 |
     *  +---+-----+-----+---+
     *  |   | 3   | 1,2 | 1 |
     *  +---+-----+-----+---+
     *  | ← | 1,2 | 1   | 3 |
     *  +---+-----+-----+---+
     *  </pre>
     */
    @Test
    public void testIntermediate() {
        String[] Q = new String[]{"0", "1", "2", "3"};
        String[] sigma = new String[]{"a", "b"};
        int[] initial = new int[]{0, 1, 1, 0};
        int[] accepting = new int[]{2, 2, 2};
        HashMap<Integer, HashMap<Integer, int[]>> transitions = new HashMap<>();
        HashMap<Integer, int[]> current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[0]);
        transitions.put(0, current);
        current = new HashMap<>();
        current.put(0, new int[]{1});
        current.put(1, new int[]{3});
        transitions.put(1, current);
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[0]);
        transitions.put(2, current);
        current = new HashMap<>();
        current.put(0, new int[]{2, 1, 2});
        current.put(1, new int[]{1});
        transitions.put(3, current);

        NFAReducer reducer = new NFAReducer(Q, sigma, transitions, initial, accepting);
        int[] reducedAccepting = reducer.getReducedAccepting();
        assertEquals("Invalid number of output accepting states!", 1, reducedAccepting.length);
        assertEquals("Invalid accepting state index!", 3, reducedAccepting[0]);
        int[] reducedInitial = reducer.getReducedInitial();
        assertEquals("Invalid number of output initial states!", 1, reducedInitial.length);
        assertEquals("Invalid initial state index!", 0, reducedInitial[0]);
        String[] reducedQ = reducer.getReducedQ();
        assertEquals("Invalid number of output states!", 4, reducedQ.length);
        assertEquals("Invalid state name!", "0,1", reducedQ[0]);
        assertEquals("Invalid state name!", "1", reducedQ[1]);
        assertEquals("Invalid state name!", "3", reducedQ[2]);
        assertEquals("Invalid state name!", "1,2", reducedQ[3]);
        String[] reducedSigma = reducer.getReducedSigma();
        assertEquals("Invalid number of output letters!", 2, reducedSigma.length);
        assertEquals("Invalid letter name!", "a", reducedSigma[0]);
        assertEquals("Invalid letter name!", "b", reducedSigma[1]);
        HashMap<Integer, HashMap<Integer, int[]>> reducedTransitions = reducer.getReducedTransitions();
        for (int i = 0; i < 4; i++) {
            for (int i1 = 0; i1 < 2; i1++) {
                assertEquals("Invalid output transitions size!", 1, reducedTransitions.get(i).get(i1).length);
            }
        }

        HashMap<Integer, int[]> reducedRow = reducedTransitions.get(0);
        assertEquals("Invalid transition index!", 1, reducedRow.get(0)[0]);
        assertEquals("Invalid transition index!", 2, reducedRow.get(1)[0]);
        reducedRow = reducedTransitions.get(1);
        assertEquals("Invalid transition index!", 1, reducedRow.get(0)[0]);
        assertEquals("Invalid transition index!", 2, reducedRow.get(1)[0]);
        reducedRow = reducedTransitions.get(2);
        assertEquals("Invalid transition index!", 3, reducedRow.get(0)[0]);
        assertEquals("Invalid transition index!", 1, reducedRow.get(1)[0]);
        reducedRow = reducedTransitions.get(3);
        assertEquals("Invalid transition index!", 1, reducedRow.get(0)[0]);
        assertEquals("Invalid transition index!", 2, reducedRow.get(1)[0]);
    }

    @Test
    public void testHard() {
        //TODO: IMPL
    }


}
