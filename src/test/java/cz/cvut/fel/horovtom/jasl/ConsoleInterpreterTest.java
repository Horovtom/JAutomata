package cz.cvut.fel.horovtom.jasl;

import cz.cvut.fel.horovtom.jasl.console.ConsoleInterpreter;
import org.junit.Test;

import java.io.Console;
import java.lang.reflect.*;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
}