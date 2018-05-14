package cz.cvut.fel.horovtom.logic.automata.automaton.regex;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.converters.FromRegexConverter;
import cz.cvut.fel.horovtom.automata.logic.converters.ToRegexConverter;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ToRegexTest {
    @Test
    public void testSimple() {
        String regex = "a*bb*";
        Automaton a = FromRegexConverter.getAutomaton(regex);
        String r = ToRegexConverter.getRegex(a);
        Automaton b = FromRegexConverter.getAutomaton(r);
        assertTrue(a.equals(b));
        assertTrue(b.acceptsWord("aaab"));
        assertTrue(b.acceptsWord("b"));
        assertFalse(b.acceptsWord("aaaaaaa"));
        assertFalse(b.acceptsWord(""));
        assertTrue(b.acceptsWord("aaabbbb"));
    }

    @Test
    public void testIntermediate() {
        DFAAutomaton a = AutomatonSamples.DFASamples.not3kPlus1As();
        String r = ToRegexConverter.getRegex(a);
        Automaton b = FromRegexConverter.getAutomaton(r);
        assertTrue(a.equals(b));
        assertTrue(a.acceptsWord("aa"));
        assertTrue(b.acceptsWord(""));
        assertTrue(b.acceptsWord("bbbb"));
        assertTrue(b.acceptsWord("bbbabbbabbbbbbb"));
        assertTrue(b.acceptsWord("bbbabbaabbb"));
        assertFalse(b.acceptsWord("bbbbabb"));
        assertFalse(b.acceptsWord("a"));
    }

    @Test
    public void test2() {
        String r = "(ba)*b*a";
        DFAAutomaton a = FromRegexConverter.getAutomaton(r).getReduced();
        String s = ToRegexConverter.getRegex(a);
        Automaton b = FromRegexConverter.getAutomaton(s).getReduced();
        assertTrue(a.equals(b));
    }

    @Test
    public void testHard() {
        DFAAutomaton a = AutomatonSamples.NFASamples.regex1().getReduced();
        String r = ToRegexConverter.getRegex(a);
        Automaton b = FromRegexConverter.getAutomaton(r).getReduced();
        assertTrue(a.equals(b));
    }

    @Test
    public void testInsane() {
        String r = "(ε+((c+(ac)*)(ab))*)(ε+(((c*a)a)*+a)((a+((b+(b+a))+a))+(a+b)))+c((b+a)+a)";
        DFAAutomaton a = FromRegexConverter.getAutomaton(r).getReduced();
        String s = ToRegexConverter.getRegex(a);
        Automaton b = FromRegexConverter.getAutomaton(s).getReduced();
        assertTrue(a.equals(b));
    }
}
