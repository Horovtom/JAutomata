package cz.cvut.fel.horovtom.jasl;

import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.jasl.console.ConsoleInterpreter;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ConsoleInterpreterTest {

    @Test
    public void getNextToken() {
        String[] res = ConsoleInterpreter.getNextToken("Hello, my little friend", ' ');
        assertEquals("The correct answer should be 'Hello,'", "Hello,", res[0]);
        assertEquals("The correct answer should be 'my little friend'", "my little friend", res[1]);

        res = ConsoleInterpreter.getNextToken("I never thought that !!eet would be so annoying", '!');
        assertEquals("The correct answer should be 'I never thought that '", "I never thought that ", res[0]);
        assertEquals("The correct answer should be '!eet would be so annoying'", "!eet would be so annoying", res[1]);

        res = ConsoleInterpreter.getNextToken(res[1], '!');
        assertEquals("The correct answer should be '', when the delimiter is the first in the string", "", res[0]);
        assertEquals("The correct answer should be 'eet would be so annoying'", "eet would be so annoying", res[1]);

        res = ConsoleInterpreter.getNextToken("There is no comma here...", ',');
        assertEquals("Function did not correctly tokenize when there was no delimiter in the string", "There is no comma here...", res[0]);
        assertEquals("Function did not output '', when there as no delimiter in the input.", "", res[1]);

        res = ConsoleInterpreter.getNextToken("", 'a');
        assertEquals("Function did not output correctly when the input was empty..", res[0], "");
        assertEquals(res[1], "");
    }

    @Test
    public void getNextTokenList() {
        Method method;
        try {
            method = ConsoleInterpreter.class.getDeclaredMethod("getNextTokenList", String.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            assertTrue("There is no such method in class ConsoleInterpreter", false);
            return;
        }

        ConsoleInterpreter interpreter = new ConsoleInterpreter();

        try {
            Object result = method.invoke(interpreter, "{2, 1, 3}, 2, 3}");
            assertTrue(result instanceof String[]);
            String[] res = (String[]) result;
            assertEquals("{2, 1, 3}", res[0]);
            assertEquals(" 2, 3}", res[1]);

            result = method.invoke(interpreter, "{1, 1, 2}}");
            assertTrue(result instanceof String[]);
            res = (String[]) result;
            assertEquals("{1, 1, 2}", res[0]);
            assertEquals("}", res[1]);

            result = method.invoke(interpreter, "{2, 3, {}, 1}, 2, 1}");
            assertTrue(result instanceof String[]);
            res = (String[]) result;
            assertEquals("{2, 3, {}, 1}", res[0]);
            assertEquals(" 2, 1}", res[1]);

            result = method.invoke(interpreter, "{}");
            assertTrue(result instanceof String[]);
            res = (String[]) result;
            assertEquals("{}", res[0]);
            assertEquals("", res[1]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        try {
            method.invoke(interpreter, "{2, {1, 3}");
            assertTrue("This input is invalid, the method should throw exception!", false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            assertTrue(e.getCause() instanceof ConsoleInterpreter.InvalidSyntaxException);
        }

        try {
            method.invoke(interpreter, "{2, 3, 1");
            assertTrue("This input is invalid, the method should throw exception!", false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            assertTrue(e.getCause() instanceof ConsoleInterpreter.InvalidSyntaxException);
        }
    }

    @Test
    public void parseList() {
        Method method;
        try {
            method = ConsoleInterpreter.class.getDeclaredMethod("parseList", String.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            assertTrue("There is no such method in class ConsoleInterpreter", false);
            return;
        }

        ConsoleInterpreter interpreter = new ConsoleInterpreter();

        try {
            Object result = method.invoke(interpreter, "{2, 3, 13}");
            assertTrue(result instanceof ArrayList);
            ArrayList<Object> res = (ArrayList<Object>) result;
            assertEquals(3, res.size());
            assertEquals("2", res.get(0));
            assertEquals("3", res.get(1));
            assertEquals("13", res.get(2));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        try {
            Object result = method.invoke(interpreter, "{a, b, 12, {}, {1, 2, 3, {}}}");
            assertTrue(result instanceof ArrayList);
            ArrayList<Object> res = (ArrayList<Object>) result;
            assertEquals(5, res.size());
            assertEquals("a", res.get(0));
            assertEquals("b", res.get(1));
            assertEquals("12", res.get(2));
            assertTrue(res.get(3) instanceof ArrayList);
            ArrayList<Object> inner = (ArrayList<Object>) res.get(3);
            assertTrue(inner.size() == 1);
            assertEquals("", inner.get(0));
            assertTrue(res.get(4) instanceof ArrayList);
            inner = (ArrayList<Object>) res.get(4);
            assertTrue(inner.size() == 4);
            assertEquals("1", inner.get(0));
            assertEquals("2", inner.get(1));
            assertEquals("3", inner.get(2));
            assertTrue(inner.get(3) instanceof ArrayList);
            inner = (ArrayList<Object>) inner.get(3);
            assertTrue(inner.size() == 1);
            assertEquals("", inner.get(0));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void extractFromBrackets() {
        Method method;
        try {
            method = ConsoleInterpreter.class.getDeclaredMethod("extractFromBrackets", String.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            assertTrue("There is no such method in class ConsoleInterpreter", false);
            return;
        }

        ConsoleInterpreter interpreter = new ConsoleInterpreter();

        String input = "aab(2,{3,4,1}, ss(12))";
        // Expected: {4, 4, 6, 12, 15, 20}
        try {
            int[] arr = (int[]) method.invoke(interpreter, input);
            assertEquals(4, arr[0]);
            assertEquals(4, arr[1]);
            assertEquals(6, arr[2]);
            assertEquals(12, arr[3]);
            assertEquals(14, arr[4]);
            assertEquals(20, arr[5]);

        } catch (IllegalAccessException | InvocationTargetException e) {
            assertTrue(false);
        }

        input = "(NFA(!#!#!#!#!)22$0)21313";
        try {
            int[] arr = (int[]) method.invoke(interpreter, input);
            assertEquals(1, arr[0]);
            assertEquals(18, arr[1]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            assertTrue(false);
        }

        input = "aas(al(1, 3, 1), 12)";
        try {
            int[] arr = (int[]) method.invoke(interpreter, input);
            assertEquals(4, arr[0]);
            assertEquals(14, arr[1]);
            assertEquals(16, arr[2]);
            assertEquals(18, arr[3]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            assertTrue(false);
        }

    }

    @Test
    public void reduceAutomaton() {
        Method method;
        try {
            method = ConsoleInterpreter.class.getDeclaredMethod("getExpressionResult", String.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            assertTrue("There is no such method in class ConsoleInterpreter", false);
            return;
        }

        ConsoleInterpreter interpreter = new ConsoleInterpreter();

        // Reducible automaton:
        //  , ,a,b
        // >,0,1,E
        //  ,1,2,3
        //  ,2,2,3
        // <,3,E,E
        //  ,E,E,E

        String table = "{{a,b},{>,0,1,E},{1,2,3},{2,2,3},{<,3,E,E},{E,E,E}}";
        try {
            Object result = method.invoke(interpreter, "ENFA(" + table + ").reduce()");
            assertTrue(result instanceof DFAAutomaton);
        } catch (IllegalAccessException | InvocationTargetException e) {
            assertFalse(true);
        }
    }


    @Test
    public void getDFAFromTable() {
        Method method;

        try {
            method = ConsoleInterpreter.class.getDeclaredMethod("getDFAFromTable", ArrayList.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            assertTrue("There is no such method in class ConsoleInterpreter", false);
            return;
        }

        ConsoleInterpreter interpreter = new ConsoleInterpreter();

        // input = "DFA({{a,b}, {<>, 1, 2, 1}, {2, 2, 2}})";
        ArrayList<ArrayList<String>> input = new ArrayList<ArrayList<String>>();
        ArrayList<String> elem = new ArrayList<>();
        elem.add("a");
        elem.add("b");
        input.add(elem);
        elem = new ArrayList<>();
        elem.add("<>");
        elem.add("1");
        elem.add("2");
        elem.add("1");
        input.add(elem);
        elem = new ArrayList<>();
        elem.add("2");
        elem.add("2");
        elem.add("2");
        input.add(elem);

        try {
            DFAAutomaton automaton = (DFAAutomaton) method.invoke(interpreter, input);
            assertTrue(automaton != null);
            assertEquals(2, automaton.getQSize());
            assertEquals(2, automaton.getSigmaSize());
            assertEquals(1, automaton.getAcceptingStates().length);
            assertTrue(automaton.acceptsWord(""));
            assertTrue(automaton.acceptsWord("bbb"));
            assertFalse(automaton.acceptsWord("a"));
            assertFalse(automaton.acceptsWord("aa"));
            String r = automaton.getRegex();
            assertEquals("b*", r);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            assertFalse(true);
        }
    }

}