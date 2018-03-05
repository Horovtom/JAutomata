package cz.cvut.fel.horovtom.logic.reductors;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DFAReductorTest {

    private static void putIn(HashMap<Integer, Integer> current, int[] toPutIn) {
        for (int i = 0; i < toPutIn.length; i++) {
            current.put(i, toPutIn[i]);
        }
    }

    @Test
    public void basicTest() {
        /*
                 a   b
             0   1   2
            >1   2   3
            <2   3   3
            <3   2   2
         */
        HashMap<Integer, HashMap<Integer, Integer>> transitions = new HashMap<>();
        HashMap<Integer, Integer> current = new HashMap<>();
        current.put(0, 1);
        current.put(1, 2);
        transitions.put(0, current);
        current = new HashMap<>();
        current.put(0, 2);
        current.put(1, 3);
        transitions.put(1, current);
        current = new HashMap<>();
        current.put(0, 3);
        current.put(1, 3);
        transitions.put(2, current);
        current = new HashMap<>();
        current.put(0, 2);
        current.put(1, 2);
        transitions.put(3, current);

        int[] accepting = new int[]{2, 3};
        DFAReductor dfaReductor = new DFAReductor(transitions, 1, accepting);
        HashMap<Integer, HashMap<Integer, Integer>> newTransitions = dfaReductor.getReducedTransitions();
        /*
            >0	1	1
            <1	1	1
         */
        assertEquals("Invalid initial state output from DFAReductor basic testcase",
                0,
                dfaReductor.getReducedInitial());
        assertEquals("Invalid accepting state output size from DFAReductor basic testcase",
                1,
                dfaReductor.getReducedAccepting().length);
        assertEquals("Invalid accepting state output form DFAReductor basic testcase",
                1,
                dfaReductor.getReducedAccepting()[0]);
        assertEquals("Invalid state count output from DFAReductor basic testcase",
                2,
                newTransitions.keySet().size());
        assertEquals("Invalid letter count output from DFAReductor basic testcase",
                2,
                newTransitions.get(0).keySet().size());
        //TransitionsCheck
        assertTrue(newTransitions.get(0).get(0) == 1);
        assertTrue(newTransitions.get(0).get(1) == 1);
        assertTrue(newTransitions.get(1).get(0) == 1);
        assertTrue(newTransitions.get(1).get(1) == 1);
    }

    @Test
    public void testDFA1() {
        /*
                a	b	c
           >0	1	2	3
            1	1	2	3
            2	7	5	4
           <3	9	7	5
            4	4	2	4
            5	7	7	7
            6	8	6	8
            7	8	9	9
           <8	3	0	1
           <9	2	4	6
         */

        int[] accepting = new int[]{3, 8, 9};
        int initial = 0;

        HashMap<Integer, HashMap<Integer, Integer>> transitions = new HashMap<>();
        HashMap<Integer, Integer> current;
        int curr = 0;
        current = new HashMap<>();
        putIn(current, new int[]{1, 2, 3});
        transitions.put(curr++, current);
        current = new HashMap<>();
        putIn(current, new int[]{1, 2, 3});
        transitions.put(curr++, current);
        current = new HashMap<>();
        putIn(current, new int[]{7, 5, 4});
        transitions.put(curr++, current);
        current = new HashMap<>();
        putIn(current, new int[]{9, 7, 5});
        transitions.put(curr++, current);
        current = new HashMap<>();
        putIn(current, new int[]{4, 2, 4});
        transitions.put(curr++, current);
        current = new HashMap<>();
        putIn(current, new int[]{7, 7, 7});
        transitions.put(curr++, current);
        current = new HashMap<>();
        putIn(current, new int[]{8, 6, 8});
        transitions.put(curr++, current);
        current = new HashMap<>();
        putIn(current, new int[]{8, 9, 9});
        transitions.put(curr++, current);
        current = new HashMap<>();
        putIn(current, new int[]{3, 0, 1});
        transitions.put(curr++, current);
        current = new HashMap<>();
        putIn(current, new int[]{2, 4, 6});
        transitions.put(curr, current);
        DFAReductor dfaReductor = new DFAReductor(transitions, initial, accepting);
        HashMap<Integer, HashMap<Integer, Integer>> newTransitions = dfaReductor.getReducedTransitions();
        assertEquals("Invalid initial state output from DFAReductor advanced testcase",
                0,
                dfaReductor.getReducedInitial());
        assertEquals("Invalid accepting state output size from DFAReductor advanced testcase",
                3,
                dfaReductor.getReducedAccepting().length);
        assertEquals("Invalid accepting state output form DFAReductor advanced testcase",
                2,
                dfaReductor.getReducedAccepting()[0]);
        assertEquals("Invalid accepting state output form DFAReductor advanced testcase",
                7,
                dfaReductor.getReducedAccepting()[1]);
        assertEquals("Invalid accepting state output form DFAReductor advanced testcase",
                8,
                dfaReductor.getReducedAccepting()[2]);
        assertEquals("Invalid state count output from DFAReductor advanced testcase",
                9,
                newTransitions.keySet().size());
        assertEquals("Invalid letter count output from DFAReductor advanced testcase",
                3,
                newTransitions.get(0).keySet().size());
        //TransitionsCheck
        assertTrue(checkRowTransitions(newTransitions.get(0), new int[]{0, 1, 2}));
        assertTrue(checkRowTransitions(newTransitions.get(1), new int[]{6, 4, 3}));
        assertTrue(checkRowTransitions(newTransitions.get(2), new int[]{8, 6, 4}));
        assertTrue(checkRowTransitions(newTransitions.get(3), new int[]{3, 1, 3}));
        assertTrue(checkRowTransitions(newTransitions.get(4), new int[]{6, 6, 6}));
        assertTrue(checkRowTransitions(newTransitions.get(5), new int[]{7, 5, 7}));
        assertTrue(checkRowTransitions(newTransitions.get(6), new int[]{7, 8, 8}));
        assertTrue(checkRowTransitions(newTransitions.get(7), new int[]{2, 0, 0}));
        assertTrue(checkRowTransitions(newTransitions.get(8), new int[]{1, 3, 5}));

    }

    private boolean checkRowTransitions(HashMap<Integer, Integer> integerIntegerHashMap, int[] expected) {
        for (int i = 0; i < expected.length; i++) {
            if (integerIntegerHashMap.get(i) != expected[i]) return false;
        }
        return true;
    }
}
