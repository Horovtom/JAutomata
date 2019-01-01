package cz.cvut.fel.horovtom.logic.automata.automaton;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.NFAAutomaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class EqualsTest {

    @Test
    public void testDFA1() {
        /*
               a b
            >0 1 1
            <1 2 0
             2 2 0
         */

        String[] Q = new String[]{"0", "1", "2"};
        String[] sigma = new String[]{"a", "b"};
        HashMap<String, HashMap<String, String>> map = new HashMap<>();
        HashMap<String, String> current = new HashMap<>();
        current.put("a", "1");
        current.put("b", "1");
        map.put("0", current);
        current = new HashMap<>();
        current.put("a", "2");
        current.put("b", "0");
        map.put("1", current);
        current = new HashMap<>();
        current.put("a", "2");
        current.put("b", "0");
        map.put("2", current);
        String initial = "0";
        String[] accepting = new String[]{"1"};
        DFAAutomaton dfa;
        DFAAutomaton dfa2;
        try {
            dfa = new DFAAutomaton(Q, sigma, map, initial, accepting);
            dfa2 = new DFAAutomaton(Q, sigma, map, initial, accepting);
        } catch (Automaton.InvalidAutomatonDefinitionException e) {
            fail();
            return;
        }

        dfa2.renameState("0", "3");
        dfa2.renameState("2", "abba");
        assertTrue("Two automata that differ only in naming of states were not equal!", dfa.equals(dfa2));
        dfa2.renameLetter("a", "c");
        assertFalse("Two automata that differ in alphabet cannot be equal", dfa.equals(dfa2));

    }

    @Test
    public void testDFAReduction() {
        DFAAutomaton dfa = AutomatonSamples.DFASamples.startEndSame();
        String[] Q = new String[]{"1", "2", "3", "4", "5"};
        String[] sigma = new String[]{"a", "b"};
        String initial = "1";
        String[] accepting = new String[]{"3", "4"};
        HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
        HashMap<String, String> curr = new HashMap<>();
        curr.put("a", "3");
        curr.put("b", "4");
        transitions.put("1", curr);
        curr = new HashMap<>();
        curr.put("a", "3");
        curr.put("b", "2");
        transitions.put("2", curr);
        curr = new HashMap<>();
        curr.put("a", "3");
        curr.put("b", "2");
        transitions.put("3", curr);
        curr = new HashMap<>();
        curr.put("a", "5");
        curr.put("b", "4");
        transitions.put("4", curr);
        curr = new HashMap<>();
        curr.put("a", "5");
        curr.put("b", "4");
        transitions.put("5", curr);
        DFAAutomaton dfa2 = null;
        try {
            dfa2 = new DFAAutomaton(Q, sigma, transitions, initial, accepting);
        } catch (Automaton.InvalidAutomatonDefinitionException e) {
            fail();
        }
        assertTrue("Reduced and non-reduced automaton should be considered equal!", dfa.equals(dfa2));
        DFAAutomaton reduced = dfa.reduce();
        assertTrue("These two automatons should be identical", reduced.equals(dfa2));
        assertTrue("Automaton and it's reduction should be equal", reduced.equals(dfa));
        assertTrue("Automaton and it's reduction should be equal", dfa.equals(reduced));
    }

    @Test
    public void testDFADifferent() {
        //This accepts \alpha*
        DFAAutomaton dfa = AutomatonSamples.DFASamples.alphaStar();
        String[] Q = new String[]{"0", "1"};
        String[] sigma = new String[]{"\\alpha", "\\beta"};
        HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
        HashMap<String, String> current = new HashMap<>();
        current.put("\\alpha", "1");
        current.put("\\beta", "0");
        transitions.put("0", current);
        current = new HashMap<>();
        current.put("\\alpha", "1");
        current.put("\\beta", "1");
        transitions.put("1", current);
        //This accepts \beta*
        DFAAutomaton dfa2 = null;
        try {
            dfa2 = new DFAAutomaton(Q, sigma, transitions, "0", new String[]{"0"});
        } catch (Automaton.InvalidAutomatonDefinitionException e) {
            fail();
        }
        assertFalse("Automatons accept different languages! They should not be equal!", dfa2.equals(dfa) || dfa.equals(dfa2));
    }

    @Test
    public void testNFASame() {
        /*
            Accepts language: b*(a+aa)

            Before reduction:
            , , a   ,b
           >,0,"1,2",0
            ,1, 3   ,
           <,2,     ,
           <,3,     ,

            After reduction:
            , ,a,b
           >,0,1,0
           <,1,2,3
           <,2,3,3
            ,3,3,3


         */

        String[] Q = new String[]{"0", "1", "2", "3"};
        String[] sigma = new String[]{"a", "b"};

        String[] initials = new String[]{"0"};
        String[] accepting = new String[]{"2", "3"};
        HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
        HashMap<String, String> curr = new HashMap<>();
        curr.put("a", "1,2");
        curr.put("b", "0");
        transitions.put("0", curr);
        curr = new HashMap<>();
        curr.put("a", "3");
        curr.put("b", "");
        transitions.put("1", curr);
        curr = new HashMap<>();
        curr.put("a", "");
        transitions.put("2", curr);
        curr = new HashMap<>();
        transitions.put("3", curr);
        NFAAutomaton nfa = null;
        try {
            nfa = new NFAAutomaton(Q, sigma, transitions, initials, accepting);
        } catch (Automaton.InvalidAutomatonDefinitionException e) {
            fail();
        }
        assertTrue(nfa.acceptsWord(new String[]{"b", "b", "b", "a", "a"}));
        assertTrue(nfa.acceptsWord(new String[]{"b", "b", "a"}));
        DFAAutomaton reducedNFA = nfa.reduce();
        assertTrue("Automaton and its reduced version should be equal!", reducedNFA.equals(nfa));
        assertTrue("Automaton should be equal with itself!", nfa.equals(nfa));
        assertTrue("Automaton and its copy should be equal!", nfa.equals(nfa.copy()));

        /*
            Creating equal automaton that accepts b*(a+aa), but with different naming

             , ,b    ,a
            <,a,     ,
            <,d,     ,
            >,g,g    ,"h,a"
             ,h,     ,d

             , ,b,a
            <,a,b,c
             ,b,b,b
            <,c,b,b
            >,d,d,a

         */
        transitions.clear();
        transitions.put("a", new HashMap<>());
        transitions.put("d", new HashMap<>());
        curr = new HashMap<>();
        curr.put("a", "h,a");
        curr.put("b", "g");
        transitions.put("g", curr);
        curr = new HashMap<>();
        curr.put("a", "d");
        transitions.put("h", curr);

        NFAAutomaton nfa2 = null;
        try {
            nfa2 = new NFAAutomaton(
                    new String[]{"a", "d", "g", "h"},
                    new String[]{"b", "a"},
                    transitions,
                    new String[]{"g"},
                    new String[]{"a", "d"});
        } catch (Automaton.InvalidAutomatonDefinitionException e) {
            fail();
        }

        assertTrue("Automatons different only in naming should be equal", nfa.equals(nfa2));
        assertTrue("Automatons different only in naming should be equal", reducedNFA.equals(nfa2));

        transitions.clear();
        curr = new HashMap<>();
        curr.put("b", "b");
        curr.put("a", "c");
        transitions.put("a", curr);
        curr = new HashMap<>();
        curr.put("b", "b");
        curr.put("a", "b");
        transitions.put("b", curr);
        curr = new HashMap<>();
        curr.put("b", "b");
        curr.put("a", "b");
        transitions.put("c", curr);
        curr = new HashMap<>();
        curr.put("b", "d");
        curr.put("a", "a");
        transitions.put("d", curr);
        DFAAutomaton reducedNFA2 = null;
        try {
            reducedNFA2 = new DFAAutomaton(
                    new String[]{"a", "b", "c", "d"},
                    new String[]{"b", "a"},
                    transitions,
                    "d",
                    new String[]{"a", "c"});
        } catch (Automaton.InvalidAutomatonDefinitionException e) {
            fail();
        }
        assertTrue(reducedNFA2.equals(nfa2));
        assertTrue(reducedNFA2.equals(nfa));
        assertTrue(reducedNFA2.equals(reducedNFA));
    }

    @Test
    public void testDFADifferent2() {
        /*
             , ,a,b
            >,0,1,2
            <,1,2,3
             ,2,3,0
             ,3,0,1

             is not the same as:

             , ,a,b
            >,0,1,2
            <,1,2,3
            <,2,3,0
             ,3,0,1

             is not the same as:

             , ,a,b
            >,0,1,2
            <,1,2,3
             ,2,3,0
            <,3,0,1

         */
        HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
        HashMap<String, String> curr = new HashMap<>();
        curr.put("a", "1");
        curr.put("b", "2");
        transitions.put("0", curr);
        curr = new HashMap<>();
        curr.put("a", "2");
        curr.put("b", "3");
        transitions.put("1", curr);
        curr = new HashMap<>();
        curr.put("a", "3");
        curr.put("b", "0");
        transitions.put("2", curr);
        curr = new HashMap<>();
        curr.put("a", "0");
        curr.put("b", "1");
        transitions.put("3", curr);

        DFAAutomaton dfa1 = null;
        try {
            dfa1 = new DFAAutomaton(
                    new String[]{"0", "1", "2", "3"},
                    new String[]{"a", "b"},
                    transitions,
                    "0",
                    new String[]{"1"}
            );
        } catch (Automaton.InvalidAutomatonDefinitionException e) {
            fail();
        }
        DFAAutomaton dfa2 = null;
        try {
            dfa2 = new DFAAutomaton(
                    new String[]{"0", "1", "2", "3"},
                    new String[]{"a", "b"},
                    transitions,
                    "0",
                    new String[]{"1", "2"}
            );
        } catch (Automaton.InvalidAutomatonDefinitionException e) {
            fail();
        }
        DFAAutomaton dfa3 = null;
        try {
            dfa3 = new DFAAutomaton(
                    new String[]{"0", "1", "2", "3"},
                    new String[]{"a", "b"},
                    transitions,
                    "0",
                    new String[]{"1", "3"}
            );
        } catch (Automaton.InvalidAutomatonDefinitionException e) {
            fail();
        }

        assertFalse("Automata with different accepting states count should not ever be equal", dfa1.equals(dfa2));
        assertFalse("Automata with different accepting states count should not ever be equal", dfa1.equals(dfa3));
        assertFalse("Automata with different accepting states after isomorphic snapping should not be equal", dfa2.equals(dfa3));
    }
}
