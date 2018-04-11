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
        String[] reducedQ = reducer.getQ();
        String[] reducedSigma = reducer.getSigma();
        HashMap<Integer, HashMap<Integer, int[]>> reducedTransitions = reducer.getTransitions();
        int[] reducedAccepting = reducer.getAccepting();
        int initialIndex = reducer.getInitial();

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
        /*
            Automaton that accepts r = (01*+101)*0*1
             ,  ,eps    ,0,1
            >,S ,"1,3,7", ,
             ,1 ,       ,2,
             ,2 ,S      , ,2
             ,3 ,       , ,4
             ,4 ,       ,5,
             ,5 ,       , ,6
             ,6 ,S      , ,
             ,7 ,       ,7,8
            <,8 ,       , ,
             ,9 ,S      , ,
            >,10,9      , ,

            That gets converted to:
             ,                 ,0            ,1
            >,"S,1,3,7,9,10"   ,"S,1,2,3,7"  ,"4,8"
             ,"S,1,2,3,7"      ,"S,1,2,3,7"  ,"S,1,2,3,4,7,8"
            <,"4,8"            ,5            ,
            <,"S,1,2,3,4,7,8"  ,"S,1,2,3,5,7","S,1,2,3,4,7,8"
             ,5                ,             ,"S,1,3,6,7"
             ,"S,1,2,3,5,7"    ,"S,1,2,3,7"  ,"S,1,2,3,4,6,7,8"
             ,"S,1,3,6,7"      ,"S,1,2,3,7"  ,"4,8"
            <,"S,1,2,3,4,6,7,8","S,1,2,3,5,7","S,1,2,3,4,7,8"
         */

        String[] Q = new String[]{"S", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        String[] sigma = new String[]{"eps", "0", "1"};
        int[] initials = new int[]{0, 10};
        int[] accepting = new int[]{8};
        HashMap<Integer, HashMap<Integer, int[]>> transition = new HashMap<>();
        HashMap<Integer, int[]> current = new HashMap<>();
        current.put(0, new int[]{1, 3, 7});
        current.put(1, new int[0]);
        current.put(2, new int[0]);
        transition.put(0, current);
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[]{2});
        current.put(2, new int[0]);
        transition.put(1, current);
        current = new HashMap<>();
        current.put(0, new int[]{0});
        current.put(1, new int[0]);
        current.put(2, new int[]{2});
        transition.put(2, current);
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[0]);
        current.put(2, new int[]{4});
        transition.put(3, current);
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[]{5});
        current.put(2, new int[0]);
        transition.put(4, current);
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[0]);
        current.put(2, new int[]{6});
        transition.put(5, current);
        current = new HashMap<>();
        current.put(0, new int[]{0});
        current.put(1, new int[0]);
        current.put(2, new int[0]);
        transition.put(6, current);
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[]{7});
        current.put(2, new int[]{8});
        transition.put(7, current);
        current = new HashMap<>();
        current.put(0, new int[0]);
        current.put(1, new int[0]);
        current.put(2, new int[0]);
        transition.put(8, current);
        current = new HashMap<>();
        current.put(0, new int[]{0});
        current.put(1, new int[0]);
        current.put(2, new int[0]);
        transition.put(9, current);
        current = new HashMap<>();
        current.put(0, new int[]{9});
        current.put(1, new int[0]);
        current.put(2, new int[0]);
        transition.put(10, current);

        ENFAReducer reducer = new ENFAReducer(Q, sigma, transition, initials, accepting, 0);
        String[] newQ = reducer.getQ();
        String[] newSigma = reducer.getSigma();
        HashMap<Integer, HashMap<Integer, int[]>> newTransitions = reducer.getTransitions();
        int newInitial = reducer.getInitial();
        int[] newAccepting = reducer.getAccepting();

        assertEquals(0, newInitial);
        assertEquals(3, newAccepting.length);
        assertEquals(8, newQ.length);
        assertEquals(2, newSigma.length);
        assertEquals(8, newTransitions.size());

        //Check Q
        String[] shouldBe = new String[]{
                "S,1,3,7,9,10",
                "S,1,2,3,7",
                "4,8",
                "S,1,2,3,4,7,8",
                "5",
                "S,1,2,3,5,7",
                "S,1,3,6,7",
                "S,1,2,3,4,6,7,8"
        };
        for (int i = 0; i < shouldBe.length; i++) {
            assertEquals("Testing: " + shouldBe[i], shouldBe[i], newQ[i]);
        }

        //Check Sigma
        assertEquals("0", newSigma[0]);
        assertEquals("1", newSigma[1]);

        //Check Accepting
        assertEquals(2, newAccepting[0]);
        assertEquals(3, newAccepting[1]);
        assertEquals(7, newAccepting[2]);

        //Check transitions
        assertEquals(1, newTransitions.get(0).get(0)[0]);
        assertEquals(2, newTransitions.get(0).get(1)[0]);

        assertEquals(1, newTransitions.get(1).get(0)[0]);
        assertEquals(3, newTransitions.get(1).get(1)[0]);

        assertEquals(4, newTransitions.get(2).get(0)[0]);
        assertEquals(0, newTransitions.get(2).get(1).length);

        assertEquals(5, newTransitions.get(3).get(0)[0]);
        assertEquals(3, newTransitions.get(3).get(1)[0]);

        assertEquals(0, newTransitions.get(4).get(0).length);
        assertEquals(6, newTransitions.get(4).get(1)[0]);

        assertEquals(1, newTransitions.get(5).get(0)[0]);
        assertEquals(7, newTransitions.get(5).get(1)[0]);

        assertEquals(1, newTransitions.get(6).get(0)[0]);
        assertEquals(2, newTransitions.get(6).get(1)[0]);

        assertEquals(5, newTransitions.get(7).get(0)[0]);
        assertEquals(3, newTransitions.get(7).get(1)[0]);

        //Check sizes
        for (int i = 0; i < 7; i++) {
            for (int i1 = 0; i1 < 2; i1++) {
                if (i == 2 && i1 == 1) continue;
                if (i == 4 && i1 == 0) continue;
                assertEquals(1, newTransitions.get(i).get(i1).length);
            }
        }
    }

    @Test
    public void testHeavilyConnected() {
        /*
            This automaton accepts everything
             , ,a,b,eps
            >,0, , ,1
             ,1,2, ,"2,3"
             ,2, ,1,1
            <,3,4, ,
             ,4, , ,0

              ,           ,a          ,b
            <>,"0,1,2,3"  ,"0,1,2,3,4","1,2,3"
            < ,"0,1,2,3,4","0,1,2,3,4","1,2,3"
            < ,"1,2,3"    ,"0,1,2,3,4","1,2,3"
         */

        String[] Q = new String[]{"0", "1", "2", "3", "4"};
        String[] sigma = new String[]{"a", "b", "eps"};
        int[] initialStates = new int[]{0};
        int[] acceptingStates = new int[]{3};
        HashMap<Integer, HashMap<Integer, int[]>> transitions = new HashMap<>();
        HashMap<Integer, int[]> current = new HashMap<>();
        current.put(0, new int[]{});
        current.put(1, new int[]{});
        current.put(2, new int[]{1});
        transitions.put(0, current);
        current = new HashMap<>();
        current.put(0, new int[]{2});
        current.put(1, new int[]{});
        current.put(2, new int[]{2, 3});
        transitions.put(1, current);
        current = new HashMap<>();
        current.put(0, new int[]{});
        current.put(1, new int[]{1});
        current.put(2, new int[]{1});
        transitions.put(2, current);
        current = new HashMap<>();
        current.put(0, new int[]{4});
        current.put(1, new int[]{});
        current.put(2, new int[]{});
        transitions.put(3, current);
        current = new HashMap<>();
        current.put(0, new int[]{});
        current.put(1, new int[]{});
        current.put(2, new int[]{0});
        transitions.put(4, current);

        ENFAReducer reducer = new ENFAReducer(Q, sigma, transitions, initialStates, acceptingStates, 2);
        String[] newQ = reducer.getQ();
        String[] newSigma = reducer.getSigma();
        HashMap<Integer, HashMap<Integer, int[]>> newTransitions = reducer.getTransitions();
        int newInitial = reducer.getInitial();
        int[] newAccepting = reducer.getAccepting();

        assertEquals(3, newAccepting.length);
        assertEquals(3, newQ.length);
        assertEquals(2, newSigma.length);
        assertEquals(0, newInitial);
        assertEquals(3, newTransitions.size());
        assertEquals("0,1,2,3", newQ[0]);
        assertEquals("0,1,2,3,4", newQ[1]);
        assertEquals("1,2,3", newQ[2]);
        assertEquals("a", newSigma[0]);
        assertEquals("b", newSigma[1]);
        assertEquals(0, newAccepting[0]);
        assertEquals(1, newAccepting[1]);
        assertEquals(2, newAccepting[2]);

        //CheckTransitions
        assertEquals(1, newTransitions.get(0).get(0)[0]);
        assertEquals(2, newTransitions.get(0).get(1)[0]);
        assertEquals(1, newTransitions.get(1).get(0)[0]);
        assertEquals(2, newTransitions.get(1).get(1)[0]);
        assertEquals(1, newTransitions.get(2).get(0)[0]);
        assertEquals(2, newTransitions.get(2).get(1)[0]);
    }
}
