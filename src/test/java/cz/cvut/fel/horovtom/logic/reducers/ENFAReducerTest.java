package cz.cvut.fel.horovtom.logic.reducers;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class ENFAReducerTest {
    @Test
    public void testSimple() {
        /*
            Automaton that accepts language L = {w | w is described by r = b*(eps + abb*+aa*)}
             , ,eps  ,a    ,b
            >,a,"b,e",d    ,a
             ,b,     ,"b,c",
            <,c,     ,     ,
             ,d,     ,     ,e
            <,e,     ,     ,e

            It is converted to NFA:
             ,   ,a  ,b
           <>,abe,bcd,abe
            <,bcd,bc ,e
            <,bc ,bc ,
            <,e  ,   ,e
         */
        String[] Q = new String[]{"a", "b", "c", "d", "e"};
        String[] sigma = new String[]{"eps", "a", "b"};
        HashMap<Integer, HashMap<Integer, int[]>> transitions = new HashMap<>();
        HashMap<Integer, int[]> curr;
        curr = new HashMap<>();
        curr.put(0, new int[]{1, 4});
        curr.put(1, new int[]{3});
        curr.put(2, new int[]{0});
        transitions.put(0, curr);
        curr = new HashMap<>();
        curr.put(0, new int[0]);
        curr.put(1, new int[]{1, 2});
        curr.put(2, new int[0]);
        transitions.put(1, curr);
        curr = new HashMap<>();
        curr.put(0, new int[0]);
        curr.put(1, new int[0]);
        curr.put(2, new int[0]);
        transitions.put(2, curr);
        curr = new HashMap<>();
        curr.put(0, new int[0]);
        curr.put(1, new int[0]);
        curr.put(2, new int[]{4});
        transitions.put(3, curr);
        curr = new HashMap<>();
        curr.put(0, new int[0]);
        curr.put(1, new int[0]);
        curr.put(2, new int[]{4});
        transitions.put(4, curr);
        int[] initialStates = new int[]{0};
        int[] acceptingStates = new int[]{2, 4};
        ENFAReducer reducer = new ENFAReducer(Q, sigma, transitions, initialStates, acceptingStates, 0);
        String[] reducedQ = reducer.getStateNames();
        String[] reducedSigma = reducer.getSigma();
        HashMap<Integer, HashMap<Integer, int[]>> reducedTransitions = reducer.getReducedTransitions();
        int[] reducedAccepting = reducer.getNewAccepting();
        int initialIndex = reducer.getInitialIndex();

        assertEquals("Initial index of reduced eps-nfa should be 0!", 0, initialIndex);
        assertEquals("There should be 4 accepting states as an output from reducer!", 4, reducedAccepting.length);
        assertEquals(0, reducedAccepting[0]);
        assertEquals(1, reducedAccepting[1]);
        assertEquals(2, reducedAccepting[2]);
        assertEquals(3, reducedAccepting[3]);
        assertEquals("There should be 4 states as an output from reducer", 4, reducedQ.length);
        assertEquals("a,b,e", reducedQ[0]);
        assertEquals("b,c,d", reducedQ[1]);
        assertEquals("b,c", reducedQ[2]);
        assertEquals("e", reducedQ[3]);
        assertEquals("Sigma should have no epsilon transition", 2, reducedSigma.length);
        assertEquals("a", reducedSigma[0]);
        assertEquals("b", reducedSigma[1]);

        assertEquals(1, reducedTransitions.get(0).get(0)[0]);
        assertEquals(0, reducedTransitions.get(0).get(1)[0]);
        assertEquals(2, reducedTransitions.get(1).get(0)[0]);
        assertEquals(3, reducedTransitions.get(1).get(1)[0]);
        assertEquals(2, reducedTransitions.get(2).get(0)[0]);
        assertEquals(0, reducedTransitions.get(2).get(1).length);
        assertEquals(0, reducedTransitions.get(3).get(0).length);
        assertEquals(3, reducedTransitions.get(3).get(1)[0]);
    }

    @Test
    public void testIntermediate() {
        //TODO: Test on some medium-sized enfa automaton
    }

    @Test
    public void testHeavillyConnected() {
        //TODO: Test on some medium-sized ENFA with a lot of eps-transitions
    }
}
