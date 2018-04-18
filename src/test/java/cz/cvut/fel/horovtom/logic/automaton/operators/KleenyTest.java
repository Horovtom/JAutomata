package cz.cvut.fel.horovtom.logic.automaton.operators;

import cz.cvut.fel.horovtom.logic.Automaton;
import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.logic.samples.Samples;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KleenyTest {
    @Test
    public void kleeny1() {
        ENFAAutomaton enfa = Samples.getENFA1();
        Automaton kleeny = enfa.getKleeny();
        assertEquals("Kleeny automaton should have the same number of states as the original", 4, kleeny.getQSize());
        assertEquals("Kleeny should have the same number of letters with epsilon transition as the original", 3, kleeny.getQSize());

        //Original accepted {a, b}, kleeny accepts {a, b}+
        assertFalse("Automaton should not accept empty word ", kleeny.acceptsWord(new String[0]));
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            StringBuilder sb = new StringBuilder();
            int length = random.nextInt(1000) + 1;
            String[] word = new String[length];
            for (int i1 = 0; i1 < length; i1++) {
                word[i1] = random.nextBoolean() ? "a" : "b";
            }
            assertTrue("Automaton should accept word: " + sb.toString(), kleeny.acceptsWord(word));
        }
        DFAAutomaton reduced = kleeny.getReduced();
        assertEquals("Reduced automaton should have only 2 states", 2, reduced.getQSize());
    }

    @Test
    public void kleeny2() {
        ENFAAutomaton aab = Samples.getENFA_aab();
        Automaton kleeny = aab.getKleeny();
        assertFalse(kleeny.acceptsWord(new String[0]));
        assertTrue(kleeny.acceptsWord(new String[]{"a", "a", "b"}));
        assertTrue(kleeny.acceptsWord(new String[]{"a", "a", "b", "a", "a", "b", "a", "a", "b"}));
        assertFalse(kleeny.acceptsWord(new String[]{"a", "b", "a"}));
        assertFalse(kleeny.acceptsWord(new String[]{"a", "a", "b", "a"}));
    }

}
