package cz.cvut.fel.horovtom.logic.reducers;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class NFAReducerTest {

    /**
     * This function tests the NFAReducer on simple input:
     * <hr>
     * <table><tr><th></th><th></th><th>a<br></th><th>b</th></tr><tr><td>↔</td><td>pear</td><td>pear,beer<br></td><td>apple</td></tr><tr><td>→</td><td>apple</td><td>∅</td><td>beer</td></tr><tr><td>←</td><td>beer</td><td>∅</td><td>∅</td></tr></table>
     * <hr>
     * <pre>
     * +---+-------+-----------+-------+
     * |   |       | a         | b     |
     * +---+-------+-----------+-------+
     * | ↔ | pear  | pear,beer | apple |
     * +---+-------+-----------+-------+
     * | → | apple | ∅         | beer  |
     * +---+-------+-----------+-------+
     * | ← | beer  | ∅         | ∅     |
     * +---+-------+-----------+-------+
     * </pre>
     * After conversion to DFA it is:
     * <hr>
     * <table><tr><th></th><th></th><th>a<br></th><th>b</th></tr><tr><td>↔</td><td>pear,apple<br></td><td>pear,beer<br></td><td>apple,beer</td></tr><tr><td>←</td><td>pear,beer</td><td>pear,beer</td><td>apple</td></tr><tr><td>←</td><td>apple,beer</td><td>∅</td><td>beer</td></tr><tr><td></td><td>apple</td><td>∅</td><td>beer</td></tr><tr><td></td><td>∅</td><td>∅</td><td>∅</td></tr><tr><td>←</td><td>beer</td><td>∅</td><td>∅</td></tr></table>
     * <hr>
     * <pre>
     * +---+------------+-----------+------------+
     * |   |            | a         | b          |
     * +---+------------+-----------+------------+
     * | ↔ | pear,apple | pear,beer | apple,beer |
     * +---+------------+-----------+------------+
     * | ← | pear,beer  | pear,beer | apple      |
     * +---+------------+-----------+------------+
     * | ← | apple,beer | ∅         | beer       |
     * +---+------------+-----------+------------+
     * |   | apple      | ∅         | beer       |
     * +---+------------+-----------+------------+
     * |   | ∅          | ∅         | ∅          |
     * +---+------------+-----------+------------+
     * | ← | beer       | ∅         | ∅          |
     * +---+------------+-----------+------------+
     * </pre>
     */
    @Test
    public void testSimple() {
        String[] Q = new String[]{"pear", "apple", "beer"};
        String[] sigma = new String[]{"a", "b"};
        int[] initial = new int[]{0, 1};
        int[] accepting = new int[]{0, 2};
        HashMap<Integer, HashMap<Integer, int[]>> transitions = new HashMap<>();
        HashMap<Integer, int[]> current = new HashMap<>();
        current.put(0, new int[]{0, 2});
        current.put(1, new int[]{1});
        transitions.put(0, current);
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[]{2});
        transitions.put(1, current);
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[0]);
        transitions.put(2, current);

        NFAReducer reducer = new NFAReducer(Q, sigma, transitions, initial, accepting);
        int[] reducedAccepting = reducer.getReducedAccepting();
        assertEquals("Invalid number of output accepting states!", 4, reducedAccepting.length);
        assertEquals("Invalid accepting state index!", 0, reducedAccepting[0]);
        assertEquals("Invalid accepting state index!", 1, reducedAccepting[1]);
        assertEquals("Invalid accepting state index!", 2, reducedAccepting[2]);
        assertEquals("Invalid accepting state index!", 5, reducedAccepting[3]);
        int reducedInitial = reducer.getReducedInitial();
        assertEquals("Invalid initial state index!", 0, reducedInitial);
        String[] reducedQ = reducer.getReducedQ();
        assertEquals("Invalid number of output states!", 6, reducedQ.length);
        assertEquals("Invalid state name!", "pear,apple", reducedQ[0]);
        assertEquals("Invalid state name!", "pear,beer", reducedQ[1]);
        assertEquals("Invalid state name!", "apple,beer", reducedQ[2]);
        assertEquals("Invalid state name!", "apple", reducedQ[3]);
        assertEquals("Invalid state name!", "ERROR", reducedQ[4]);
        assertEquals("Invalid state name!", "beer", reducedQ[5]);
        String[] reducedSigma = reducer.getReducedSigma();
        assertEquals("Invalid number of output letters!", 2, reducedSigma.length);
        assertEquals("Invalid letter name!", "a", reducedSigma[0]);
        assertEquals("Invalid letter name!", "b", reducedSigma[1]);
        HashMap<Integer, HashMap<Integer, Integer>> reducedTransitions = reducer.getReducedTransitions();

        HashMap<Integer, Integer> reducedRow = reducedTransitions.get(0);
        assertEquals("Invalid transition index!", new Integer(1), reducedRow.get(0));
        assertEquals("Invalid transition index!", new Integer(2), reducedRow.get(1));
        reducedRow = reducedTransitions.get(1);
        assertEquals("Invalid transition index!", new Integer(1), reducedRow.get(0));
        assertEquals("Invalid transition index!", new Integer(3), reducedRow.get(1));
        reducedRow = reducedTransitions.get(2);
        assertEquals("Invalid transition index!", new Integer(4), reducedRow.get(0));
        assertEquals("Invalid transition index!", new Integer(5), reducedRow.get(1));
        reducedRow = reducedTransitions.get(3);
        assertEquals("Invalid transition index!", new Integer(4), reducedRow.get(0));
        assertEquals("Invalid transition index!", new Integer(5), reducedRow.get(1));
        reducedRow = reducedTransitions.get(4);
        assertEquals("Invalid transition index!", new Integer(4), reducedRow.get(0));
        assertEquals("Invalid transition index!", new Integer(4), reducedRow.get(1));
        reducedRow = reducedTransitions.get(5);
        assertEquals("Invalid transition index!", new Integer(4), reducedRow.get(0));
        assertEquals("Invalid transition index!", new Integer(4), reducedRow.get(1));
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
        int reducedInitial = reducer.getReducedInitial();
        assertEquals("Invalid initial state index!", 0, reducedInitial);
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
        HashMap<Integer, HashMap<Integer, Integer>> reducedTransitions = reducer.getReducedTransitions();


        HashMap<Integer, Integer> reducedRow = reducedTransitions.get(0);
        assertEquals("Invalid transition index!", new Integer(1), reducedRow.get(0));
        assertEquals("Invalid transition index!", new Integer(2), reducedRow.get(1));
        reducedRow = reducedTransitions.get(1);
        assertEquals("Invalid transition index!", new Integer(1), reducedRow.get(0));
        assertEquals("Invalid transition index!", new Integer(2), reducedRow.get(1));
        reducedRow = reducedTransitions.get(2);
        assertEquals("Invalid transition index!", new Integer(3), reducedRow.get(0));
        assertEquals("Invalid transition index!", new Integer(1), reducedRow.get(1));
        reducedRow = reducedTransitions.get(3);
        assertEquals("Invalid transition index!", new Integer(1), reducedRow.get(0));
        assertEquals("Invalid transition index!", new Integer(2), reducedRow.get(1));
    }

    /**
     * This function tests NFAReducer on hard input
     */
    @Test
    public void testHard() {
        String[] Q = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};
        String[] sigma = new String[]{"a", "b", "c"};
        int[] initial = new int[]{0, 5, 7, 10};
        int[] accepting = new int[]{2, 3, 7, 11, 12, 13};
        HashMap<Integer, HashMap<Integer, int[]>> transitions = new HashMap<>();
        HashMap<Integer, int[]> current = new HashMap<>();
        //A
        current.put(0, new int[]{2, 3});
        current.put(1, new int[]{0, 6, 7, 8});
        current.put(2, new int[]{9, 0});
        transitions.put(0, current);
        //B
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[0]);
        current.put(2, new int[0]);
        transitions.put(1, current);
        //C
        current = new HashMap<>();
        current.put(0, new int[]{1});
        current.put(1, new int[0]);
        current.put(2, new int[0]);
        transitions.put(2, current);
        //D
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[]{4});
        current.put(2, new int[0]);
        transitions.put(3, current);
        //E
        current = new HashMap<>();
        current.put(0, new int[]{5, 6});
        current.put(1, new int[0]);
        current.put(2, new int[0]);
        transitions.put(4, current);
        //F
        current = new HashMap<>();
        current.put(0, new int[]{7, 13});
        current.put(1, new int[]{0, 2});
        current.put(2, new int[]{7, 9});
        transitions.put(5, current);
        //G
        current = new HashMap<>();
        current.put(0, new int[]{10, 13});
        current.put(1, new int[]{0, 1, 2, 3});
        current.put(2, new int[0]);
        transitions.put(6, current);
        //H
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[0]);
        current.put(2, new int[0]);
        transitions.put(7, current);
        //I
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[]{8});
        current.put(2, new int[]{9, 12});
        transitions.put(8, current);
        //J
        current = new HashMap<>();
        current.put(0, new int[]{12});
        current.put(1, new int[]{12});
        current.put(2, new int[]{12});
        transitions.put(9, current);
        //K
        current = new HashMap<>();
        current.put(0, new int[]{0});
        current.put(1, new int[]{0});
        current.put(2, new int[]{1});
        transitions.put(10, current);
        //L
        current = new HashMap<>();
        current.put(0, new int[]{0});
        current.put(1, new int[]{2});
        current.put(2, new int[]{0});
        transitions.put(11, current);
        //M
        current = new HashMap<>();
        current.put(0, new int[]{2});
        current.put(1, new int[]{3});
        current.put(2, new int[]{7});
        transitions.put(12, current);
        //N
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[]{0});
        current.put(2, new int[0]);
        transitions.put(13, current);


        NFAReducer reducer = new NFAReducer(Q, sigma, transitions, initial, accepting);

        int[] reducedAccepting = reducer.getReducedAccepting();
        assertEquals("Invalid number of output accepting states!", 35, reducedAccepting.length);
        int reducedInitial = reducer.getReducedInitial();
        String[] reducedQ = reducer.getReducedQ();
        assertEquals("Invalid number of output states!", 44, reducedQ.length);
        String[] reducedSigma = reducer.getReducedSigma();
        assertEquals("Invalid number of output letters!", 3, reducedSigma.length);
        assertEquals("Invalid letter name!", "a", reducedSigma[0]);
        assertEquals("Invalid letter name!", "b", reducedSigma[1]);
        assertEquals("Invalid letter name!", "c", reducedSigma[2]);
    }


}
