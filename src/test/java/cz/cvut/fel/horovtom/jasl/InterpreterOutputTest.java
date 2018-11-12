package cz.cvut.fel.horovtom.jasl;

import cz.cvut.fel.horovtom.jasl.interpreter.Interpreter;
import cz.cvut.fel.horovtom.jasl.interpreter.Interpreter.InvalidSyntaxException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class InterpreterOutputTest {
    private File createTemp() {
        try {
            File tempFile = File.createTempFile("test-", "");
            tempFile.deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            System.err.println("Could not run test, because creation of temporary file failed");
            e.printStackTrace();
            assertFalse("Could not run test, because creation of temporary file failed", true);
            return null;
        }
    }

    //TODO: TEST FILE INTERPRETING

    @Test
    public void booleanExportTest() {
        Interpreter interpreter = new Interpreter();
        try {
            String res;

            res = interpreter.parseLine("$a = getExample1().accepts(abb)");
            assertEquals("This should output nothing.", "", res);

            res = interpreter.parseLine("$b = {a, b}");
            assertEquals("List initialization should not output anything.", "", res);

            res = interpreter.parseLine("getExample1().accepts($b)");
            assertEquals("This evaluation should output false, because the automaton does not accept word 'ab'.", "false", res);

            res = interpreter.parseLine("$a");
            assertEquals("This should output true, because the automaton does accept word 'aab'.", "true", res);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
            assertFalse("Failed to run test because of InvalidSyntaxException!", true);
        }
    }

    @Test
    public void dfaDefinition() {
        Interpreter interpreter = new Interpreter();

        try {
            String res;

            res = interpreter.parseLine("$a = DFA({{a,b},{>,0,2,1},{1,2,1},{<,2,2,1}})");
            assertEquals("Automaton definition and assignment should not generate any output!", "", res);

            res = interpreter.parseLine("$a.accepts(aab)");
            assertEquals("Automaton should not accept word 'aab'!", "false", res);

            res = interpreter.parseLine("$a.accepts(bbbba)");
            assertEquals("Automaton should accept word 'bbbba'!", "true", res);

            res = interpreter.parseLine("fromRegex((a+b)*a).reduce()");
            String res2 = interpreter.parseLine("$a.reduce()");
            assertTrue("Automatons should be equal! Yet they differ: \nres: " + res + "\nres2: " + res2, res2.equals(res));

            res = interpreter.parseLine("$c = {{a,b},{>,0,2,1},{1,2,1},{<,2,2,1}}");
            assertEquals("List initialization and assignment should not generate any output.", "", res);

            res = interpreter.parseLine("$b = DFA($c)");
            assertEquals("Automaton definition and assignment should not generate any output!", "", res);

            res = interpreter.parseLine("$a.equals($b)");
            assertEquals("Automatons A and B should be equal!", "true", res);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
            assertFalse("Failed to run test because of InvalidSyntaxException!", true);
        }
    }

    @Test
    public void nfaDefinition() {
        Interpreter interpreter = new Interpreter();

        try {
            String res;

            res = interpreter.parseLine("$a = NFA({{a,b}, {>,0,1,{}}, {<,1,{},1}, {<>,2,3,{}}, {<,3,3,{}}})");
            assertEquals("Automaton initialization and assignment should not generate any output.", "", res);

            res = interpreter.parseLine("$a.accepts(abbb)");
            assertEquals("Automaton should accept word 'abbb'.", "true", res);

            res = interpreter.parseLine("$a.accepts(aaaaa)");
            assertEquals("Automaton should accept word 'aaaaa'.", "true", res);

            res = interpreter.parseLine("$a.accepts(babbbbaaa)");
            assertEquals("Automaton should not accept word 'babbbbaaa'.", "false", res);

            res = interpreter.parseLine("$a.accepts(aaba)");
            assertEquals("Automaton should not accept word 'aaba'.", "false", res);

            interpreter.parseLine("$c = {{a,b}, {>,0,1,{}}, {<,1,{},1}, {<>,2,3,{}}, {<,3,3,{}}}");

            res = interpreter.parseLine("NFA($c).equals($a)");
            assertEquals("Automata should be equal!", "true", res);
            
            res = interpreter.parseLine("fromRegex(eps + a(a* + b*)).equals($a)");
            assertEquals("Automaton from this regex and automaton A should be the same.", "true", res);

            res = interpreter.parseLine("fromRegex((ba(b)*)*).equals($a)");
            assertEquals("Automaton from this regex and automaton A should not be the same.", "false", res);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
            assertFalse("Failed to run test because of InvalidSyntaxException!", true);
        }
    }
}
