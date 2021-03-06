package cz.cvut.fel.horovtom.logic.automata.samples;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.NFAAutomaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.*;

public class NFASamplesTest {
    @Test
    public void testListSyntaxCheck() throws FileNotFoundException, UnsupportedEncodingException {
        NFAAutomaton a = AutomatonSamples.NFASamples.listSyntaxCheck();
        assertTrue(a != null);
        assertTrue(a.acceptsWord(new String[]{"list ", "id", ";", "n", ";", "id", ";", "id", ";", "n", ";", "n", ";", "id", "#"}));
        assertFalse(a.acceptsWord(new String[]{"id", ";", "n", ";", "id", "#"}));
        assertFalse(a.acceptsWord(new String[]{"list ", "id", ";", "id", ";", "id", ";", "n", ";"}));
        assertFalse(a.acceptsWord(new String[]{"list ", "id", ";", "id", ";", "id", ";", "n", ";", "n"}));
        assertFalse(a.acceptsWord(new String[]{}));
        assertFalse(a.acceptsWord(new String[]{"list ", "id", "id", "#"}));
    }

    @Test
    public void testbAtEnd() throws FileNotFoundException, UnsupportedEncodingException {
        NFAAutomaton a = AutomatonSamples.NFASamples.bAtEnd();
        assertTrue(a != null);
        assertTrue(a.acceptsWord("baa"));
        assertTrue(a.acceptsWord("bbb"));
        assertTrue(a.acceptsWord("bab"));
        assertFalse(a.acceptsWord("aaa"));
        assertFalse(a.acceptsWord("ba"));
        assertFalse(a.acceptsWord("bb"));
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            StringBuilder sb = new StringBuilder();

            for (int i1 = 0; i1 < 100; i1++) {
                if (r.nextBoolean()) {
                    sb.append('a');
                } else {
                    sb.append('b');
                }
            }
            if (sb.charAt(sb.length() - 3) == 'b')
                assertTrue("Automaton should accept: " + sb.toString(), a.acceptsWord(sb.toString()));
            else
                assertFalse("Automaton should not accept: " + sb.toString(), a.acceptsWord(sb.toString()));

        }
    }

    @Test
    public void testRegex3() {
        NFAAutomaton a = AutomatonSamples.NFASamples.regex3();
        assertTrue(a != null);
        assertFalse(a.acceptsWord(""));
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            int cCount = 0;
            StringBuilder sb = new StringBuilder();
            int i1 = r.nextInt(100) + 1;
            boolean lastC = false;
            for (int i2 = 0; i2 < i1; i2++) {
                if (r.nextInt(40) == 1) {
                    sb.append('c');
                    cCount++;
                    lastC = true;
                } else if (r.nextBoolean()) {
                    sb.append('a');
                    lastC = false;
                } else {
                    sb.append('b');
                    lastC = false;
                }
            }
            if (cCount == 1 && r.nextBoolean()) {
                sb.append('c');
                assertTrue("Automaton should accept: " + sb.toString(), a.acceptsWord(sb.toString()));
            } else if (cCount == 2 && lastC)
                assertTrue("Automaton should accept: " + sb.toString(), a.acceptsWord(sb.toString()));
            else
                assertFalse("Automaton should not accept: " + sb.toString(), a.acceptsWord(sb.toString()));
        }
    }

    @Test
    public void regex4() {
        NFAAutomaton a = AutomatonSamples.NFASamples.regex4();
        assertTrue(a != null);
        String Q = "0,1,2,3";
        String sigma = "a,b,ε";
        HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
        HashMap<String, String> curr = new HashMap<>();
        curr.put("a", "0,1");
        transitions.put("0", curr);
        curr = new HashMap<>();
        curr.put("b", "2");
        transitions.put("1", curr);
        curr = new HashMap<>();
        curr.put("b", "3");
        curr.put("ε", "3");
        transitions.put("2", curr);
        String initial = "0";
        String accepting = "3";
        Automaton b = null;
        try {
            b = new ENFAAutomaton(Q, sigma, transitions, initial, accepting);
        } catch (Automaton.InvalidAutomatonDefinitionException e) {
            fail();
        }
        assertTrue(a.equals(b));
    }
}
