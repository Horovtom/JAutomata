package cz.cvut.fel.horovtom.jasl.interpreter;

import cz.cvut.fel.horovtom.utilities.Utilities;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;

public class InterpreterOutputTest {


    //TODO: TEST FILE INTERPRETING

    @Test
    public void booleanExportTest() {
        Interpreter interpreter = new Interpreter();
        try {
            String res;

            res = interpreter.parseLine("$a = getExample().accepts(abb)");
            assertEquals("This should output nothing.", "", res);

            res = interpreter.parseLine("$b = {a, b}");
            assertEquals("List initialization should not output anything.", "", res);

            res = interpreter.parseLine("getExample().accepts($b)");
            assertEquals("This evaluation should output false, because the automaton does not accept word 'ab'.", "false", res);

            res = interpreter.parseLine("$a");
            assertEquals("This should output true, because the automaton does accept word 'aab'.", "true", res);
        } catch (SyntaxException e) {
            e.printStackTrace();
            fail("Failed to run test because of SyntaxException!");
        }
    }

    @Test
    public void dfaDefinition() {
        Interpreter interpreter = new Interpreter();

        try {
            String res;

            res = interpreter.parseLine("$a = Automaton({{a,b},{>,0,2,1},{1,2,1},{<,2,2,1}})");
            assertEquals("Automaton definition and assignment should not generate any output!", "", res);

            res = interpreter.parseLine("$a.accepts(aab)");
            assertEquals("Automaton should not accept word 'aab'!", "false", res);

            res = interpreter.parseLine("$a.accepts(bbbba)");
            assertEquals("Automaton should accept word 'bbbba'!", "true", res);

            res = interpreter.parseLine("fromRegex((a+b)*a).reduce()");
            String res2 = interpreter.parseLine("$a.reduce()");
            assertEquals("Automatons should be equal! Yet they differ: \nres: " + res + "\nres2: " + res2, res2, res);

            res = interpreter.parseLine("$c = {{a,b},{>,0,2,1},{1,2,1},{<,2,2,1}}");
            assertEquals("List initialization and assignment should not generate any output.", "", res);

            res = interpreter.parseLine("$b = Automaton($c)");
            assertEquals("Automaton definition and assignment should not generate any output!", "", res);

            res = interpreter.parseLine("$a.equals($b)");
            assertEquals("Automatons A and B should be equal!", "true", res);
        } catch (SyntaxException e) {
            e.printStackTrace();
            fail("Failed to run test because of SyntaxException!");
        }
    }

    @Test
    public void nfaDefinition() {
        Interpreter interpreter = new Interpreter();

        try {
            String res;

            res = interpreter.parseLine("$a = Automaton({{a,b}, {>,0,1,{}}, {<,1,{},1}, {<>,2,3,{}}, {<,3,3,{}}})");
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

            res = interpreter.parseLine("Automaton($c).equals($a)");
            assertEquals("Automata should be equal!", "true", res);

            res = interpreter.parseLine("fromRegex(eps + a(a* + b*)).equals($a)");
            assertEquals("Automaton from this regex and automaton A should be the same.", "true", res);

            res = interpreter.parseLine("fromRegex((ba(b)*)*).equals($a)");
            assertEquals("Automaton from this regex and automaton A should not be the same.", "false", res);
        } catch (SyntaxException e) {
            e.printStackTrace();
            fail("Failed to run test because of SyntaxException!");
        }
    }

    @Test
    public void enfaDefinition() {
        Interpreter interpreter = new Interpreter();

        try {
            String res;

            res = interpreter.parseLine(
                    "$a = Automaton({{eps,a,b},{>,0,{1,3},{},{}},{1,{},1,2},{<,2,{},{},{}},{3,{},{},{3,4}},{<,4,{},{},{}}})");
            assertEquals("Automaton initialization and assignment should not generate any output.", "", res);

            res = interpreter.parseLine("$a.accepts(aaab)");
            assertEquals("Automaton should accept word 'aaab'.", "true", res);

            res = interpreter.parseLine("$a.accepts(bbbbbb)");
            assertEquals("Automaton should accept word 'bbbbbb'.", "true", res);

            res = interpreter.parseLine("$a.accepts(b)");
            assertEquals("Automaton should accept word 'b'.", "true", res);

            res = interpreter.parseLine("$a.accepts(babbbbaaa)");
            assertEquals("Automaton should not accept word 'babbbbaaa'.", "false", res);

            res = interpreter.parseLine("$a.accepts(aaba)");
            assertEquals("Automaton should not accept word 'aaba'.", "false", res);

            res = interpreter.parseLine("$a.accepts()");
            assertEquals("Automaton should not accept word ''.", "false", res);

            interpreter.parseLine("$c = {{ε,a,b},{>,0,{1,3},{},{}},{1,{},1,2},{<,2,{},{},{}},{3,{},{},{3,4}},{<,4,{},{},{}}}");

            res = interpreter.parseLine("Automaton($c).equals($a)");
            assertEquals("Automata should be equal!", "true", res);

            res = interpreter.parseLine("fromRegex((a*+b*)b).equals($a)");
            assertEquals("Automaton from this regex and automaton A should be the same.", "true", res);

            res = interpreter.parseLine("fromRegex((ba(b)*)*).equals($a)");
            assertEquals("Automaton from this regex and automaton A should not be the same.", "false", res);
        } catch (SyntaxException e) {
            e.printStackTrace();
            fail("Failed to run test because of SyntaxException!");
        }
    }

    @Test
    public void exportPng() {
        Interpreter interpreter = new Interpreter();
        File tmp = Utilities.createTempFile();
        assert tmp != null;

        try {
            String res;

            res = interpreter.parseLine("$a = Automaton({{a,b},{>,0,2,1},{1,2,1},{<,2,2,1}})");
            assertEquals("Automaton definition and assignment should not generate any output!", "", res);

            String path = tmp.getAbsolutePath();
            res = interpreter.parseLine("$a.toPNG(" + path + ")");
            assertEquals("Exporting to PNG should not have any output.", "", res);

            File f = new File(path);
            assertEquals("The result file should be a png file.", "png", ImageIO.getImageReaders(ImageIO.createImageInputStream(f)).next().getFormatName());

        } catch (SyntaxException e) {
            e.printStackTrace();
            fail("Failed to run test because of SyntaxException!");
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void exportDot() {
        Interpreter interpreter = new Interpreter();
        try {
            String res;

            res = interpreter.parseLine("$a = Automaton({{a,b},{>,0,2,1},{1,2,1},{<,2,2,1}})");
            assertEquals("Automaton definition and assignment should not generate any output!", "", res);

            res = interpreter.parseLine("$a.toDot()");
            assertTrue("Result should at least start with: digraph automaton", res.startsWith("digraph automaton"));

            res = interpreter.parseLine("$a.toSimpleDot()");
            assertTrue("Result should at least start with: digraph automaton", res.startsWith("digraph automaton"));
        } catch (SyntaxException e) {
            e.printStackTrace();
            fail("Failed to run test because of SyntaxException!");
        }

    }

    @Test
    public void incompleteDefinition() {
        Interpreter interpreter = new Interpreter();
        try {
            String res;
            res = interpreter.parseLine("$a = Automaton({{a,b},{>,0,{1,2}},{1},{<,2,2,3},{3,3,2}})");
            assertEquals("Automaton definition and assignment should not generate any output!", "", res);

            res = interpreter.parseLine("$a");
            assertTrue(!res.equals(""));

            res = interpreter.parseLine("$a.accepts(abbab)");
            assertEquals("false", res);

            res = interpreter.parseLine("$a.accepts(abb)");
            assertEquals("true", res);

        } catch (SyntaxException e) {
            e.printStackTrace();
            fail("Failed to run test because of SyntaxException!");
        }
    }

    @Test
    public void chaining() {
        Interpreter interpreter = new Interpreter();

        try {
            String res;
            File tmp = Utilities.createTempFile();

            assertNotNull(tmp);
            res = interpreter.parseLine("getExample().reduce().equals(getExample()).save(" + tmp.getAbsolutePath() + ")");
            assertEquals("There should be no output from .save call", "", res);

            FileReader fr = new FileReader(tmp);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            assertEquals("The result of the line should be true...", "true", line);
            br.close();
            fr.close();
        } catch (SyntaxException e) {
            e.printStackTrace();
            fail("Failed to run test because of SyntaxException!");
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}
