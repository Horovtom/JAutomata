package cz.cvut.fel.horovtom.jasl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}