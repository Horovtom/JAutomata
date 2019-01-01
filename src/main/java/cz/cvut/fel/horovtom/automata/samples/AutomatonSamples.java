package cz.cvut.fel.horovtom.automata.samples;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.NFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.converters.FromRegexConverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Logger;

public class AutomatonSamples {
    private static Logger LOGGER = Logger.getLogger(AutomatonSamples.class.getName());
    // ************************
    //          DFA
    // ************************

    public static class DFASamples {

        /**
         * Image: samples/images/not3kPlus1As.png
         * <p>
         * This automaton accepts this language: L = {w ∈ {a, b}* | |w|_a != 3k + 1}<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>a</th><th>b</th></tr>
         * <tr><td>&harr;</td><td>0</td><td>1</td><td>0</td></tr>
         * <tr><td></td><td>1</td><td>2</td><td>1</td></tr>
         * <tr><td>&larr;</td><td>2</td><td>0</td><td>2</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +----+---+---+---+
         * |    |   | a | b |
         * +----+---+---+---+
         * | <> |0  | 1 | 0 |
         * +----+---+---+---+
         * |    |1  | 2 | 1 |
         * +----+---+---+---+
         * | <  |2  | 0 | 2 |
         * +----+---+---+---+
         * </pre>
         */
        public static DFAAutomaton not3kPlus1As() {
            String[] Q = new String[]{"0", "1", "2"};
            String[] sigma = new String[]{"a", "b"};
            String initial = "0";
            String acceptings = "0,2";
            HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
            HashMap<String, String> curr = new HashMap<>();
            transitions.put("0", curr);
            curr.put("a", "1");
            curr.put("b", "0");
            curr = new HashMap<>();
            transitions.put("1", curr);
            curr.put("a", "2");
            curr.put("b", "1");
            curr = new HashMap<>();
            transitions.put("2", curr);
            curr.put("a", "0");
            curr.put("b", "2");
            DFAAutomaton dfa = null;
            try {
                dfa = new DFAAutomaton(Q, sigma, transitions, initial, acceptings);
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                return null;
            }
            dfa.setDescription("This automaton accepts this language: L = {w ∈ {a, b}* | |w|_a != 3k + 1}");
            return dfa;
        }

        /**
         * Image is similar to: samples/images/lolipop.png
         * <p>
         * This automaton is <b>reduced</b> and it accepts this language: <br>
         * <b> &Sigma; = {0.12, -6.38, 0, 213.002}, L = {w | w &isin;&Sigma;<sup>*</sup>, w contains "0.12 -6.38 0.12 0 213.002 -6.38 213.002" as a substring}</b>
         * <hr>
         * <table><tr><th colspan="2">Q<br></th><th>0.12</th><th>-6.38<br></th><th>0</th><th>213.002</th></tr><tr><td>→</td><td>0</td><td>1</td><td>0<br></td><td>0</td><td>0</td></tr><tr><td></td><td>1</td><td>0</td><td>2</td><td>0</td><td>0</td></tr><tr><td><br></td><td>2</td><td>3</td><td>0</td><td>0</td><td>0</td></tr><tr><td></td><td>3</td><td>0</td><td>0</td><td>4</td><td>0</td></tr><tr><td></td><td>4</td><td>0</td><td>0</td><td>0</td><td>5</td></tr><tr><td><br></td><td>5</td><td>0</td><td>6</td><td>0</td><td>0</td></tr><tr><td></td><td>6</td><td>0</td><td>0</td><td>0</td><td>7</td></tr><tr><td>←</td><td>7</td><td>7</td><td>7</td><td>7</td><td>7</td></tr></table>
         * <hr>
         * <pre>
         * +-------+------+-------+---+-----------------+ <br>
         * | Q     | 0.12 | -6.38 | 0 | 213.002         | <br>
         * +-------+------+-------+---+-----------------+ <br>
         * | → | 0 | 1    | 0     | 0 | 0               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * |   | 1 | 0    | 2     | 0 | 0               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * |   | 2 | 3    | 0     | 0 | 0               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * |   | 3 | 0    | 0     | 4 | 0               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * |   | 4 | 0    | 0     | 0 | 5               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * |   | 5 | 0    | 6     | 0 | 0               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * |   | 6 | 0    | 0     | 0 | 7               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * | ← | 7 | 7    | 7     | 7 | 7               | <br>
         * +---+---+------+-------+---+-----------------+
         * </pre>
         */
        public static DFAAutomaton lolipopNumbers() {
            String[] Q = new String[]{
                    "0", "1", "2", "3", "4", "5", "6", "7"
            };
            String[] sigma = new String[]{
                    "0.12", "-6.38", "0", "213.002"
            };
            HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
            HashMap<String, String> current = new HashMap<>();
            current.put("0.12", "1");
            current.put("-6.38", "0");
            current.put("0", "0");
            current.put("213.002", "0");
            transitions.put("0", current);
            current = new HashMap<>();
            current.put("0.12", "0");
            current.put("-6.38", "2");
            current.put("0", "0");
            current.put("213.002", "0");
            transitions.put("1", current);
            current = new HashMap<>();
            current.put("0.12", "3");
            current.put("-6.38", "0");
            current.put("0", "0");
            current.put("213.002", "0");
            transitions.put("2", current);
            current = new HashMap<>();
            current.put("0.12", "0");
            current.put("-6.38", "0");
            current.put("0", "4");
            current.put("213.002", "0");
            transitions.put("3", current);
            current = new HashMap<>();
            current.put("0.12", "0");
            current.put("-6.38", "0");
            current.put("0", "0");
            current.put("213.002", "5");
            transitions.put("4", current);
            current = new HashMap<>();
            current.put("0.12", "0");
            current.put("-6.38", "6");
            current.put("0", "0");
            current.put("213.002", "0");
            transitions.put("5", current);
            current = new HashMap<>();
            current.put("0.12", "0");
            current.put("-6.38", "0");
            current.put("0", "0");
            current.put("213.002", "7");
            transitions.put("6", current);
            current = new HashMap<>();
            current.put("0.12", "7");
            current.put("-6.38", "7");
            current.put("0", "7");
            current.put("213.002", "7");
            transitions.put("7", current);
            try {
                return new DFAAutomaton(Q, sigma, transitions, "0", new String[]{"7"});
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                return null;
            }
        }

        /**
         * Image samples/images/lolipop.png
         * <p>
         * This automaton is <b>reduced</b> and it accepts this language: <br>
         * <b> &Sigma; = {l, o, i, p}, L = {w | w &isin;&Sigma;<sup>*</sup>, w contains "lolipop" as a substring}</b>
         * <hr>
         * <table><tr><th colspan="2">Q<br></th><th>l</th><th>o<br></th><th>i</th><th>p</th></tr>
         * <tr><td>→</td><td>0</td><td>1</td><td>0<br></td><td>0</td><td>0</td></tr>
         * <tr><td></td><td>1</td><td>1</td><td>2</td><td>0</td><td>0</td></tr>
         * <tr><td><br></td><td>2</td><td>3</td><td>0</td><td>0</td><td>0</td></tr>
         * <tr><td></td><td>3</td><td>1</td><td>0</td><td>4</td><td>0</td></tr>
         * <tr><td></td><td>4</td><td>1</td><td>0</td><td>0</td><td>5</td></tr>
         * <tr><td><br></td><td>5</td><td>1</td><td>6</td><td>0</td><td>0</td></tr>
         * <tr><td></td><td>6</td><td>1</td><td>0</td><td>0</td><td>7</td></tr>
         * <tr><td>←</td><td>7</td><td>7</td><td>7</td><td>7</td><td>7</td></tr></table>
         * <hr>
         * <pre>
         * +-------+------+-------+---+-----------------+ <br>
         * | Q     | l    | o     | i | p               | <br>
         * +-------+------+-------+---+-----------------+ <br>
         * | → | 0 | 1    | 0     | 0 | 0               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * |   | 1 | 1    | 2     | 0 | 0               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * |   | 2 | 3    | 0     | 0 | 0               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * |   | 3 | 1    | 0     | 4 | 0               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * |   | 4 | 1    | 0     | 0 | 5               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * |   | 5 | 1    | 6     | 0 | 0               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * |   | 6 | 1    | 0     | 0 | 7               | <br>
         * +---+---+------+-------+---+-----------------+ <br>
         * | ← | 7 | 7    | 7     | 7 | 7               | <br>
         * +---+---+------+-------+---+-----------------+
         * </pre>
         */
        public static DFAAutomaton lolipop() {
            String[] Q = new String[]{
                    "0", "1", "2", "3", "4", "5", "6", "7"
            };
            String[] sigma = new String[]{
                    "l", "o", "i", "p"
            };
            HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
            HashMap<String, String> current = new HashMap<>();
            current.put("l", "1");
            current.put("o", "0");
            current.put("i", "0");
            current.put("p", "0");
            transitions.put("0", current);
            current = new HashMap<>();
            current.put("l", "1");
            current.put("o", "2");
            current.put("i", "0");
            current.put("p", "0");
            transitions.put("1", current);
            current = new HashMap<>();
            current.put("l", "3");
            current.put("o", "0");
            current.put("i", "0");
            current.put("p", "0");
            transitions.put("2", current);
            current = new HashMap<>();
            current.put("l", "1");
            current.put("o", "0");
            current.put("i", "4");
            current.put("p", "0");
            transitions.put("3", current);
            current = new HashMap<>();
            current.put("l", "1");
            current.put("o", "0");
            current.put("i", "0");
            current.put("p", "5");
            transitions.put("4", current);
            current = new HashMap<>();
            current.put("l", "1");
            current.put("o", "6");
            current.put("i", "0");
            current.put("p", "0");
            transitions.put("5", current);
            current = new HashMap<>();
            current.put("l", "1");
            current.put("o", "0");
            current.put("i", "0");
            current.put("p", "7");
            transitions.put("6", current);
            current = new HashMap<>();
            current.put("l", "7");
            current.put("o", "7");
            current.put("i", "7");
            current.put("p", "7");
            transitions.put("7", current);
            try {
            return new DFAAutomaton(Q, sigma, transitions, "0", new String[]{"7"});
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                return null;
            }
        }

        /**
         * Ex. 3.2.3. in script. Image: samples/images/startEndSame.png
         * <p>
         * This automaton is <b>not</b> reduced and accepts this language:
         * <br><b>L = { w | w &isin; &Sigma;<sup>*</sup>, w starts and ends with the same character }, &Sigma; = {a,b}</b>
         * <hr>
         * <table><tr><th colspan="2">Q</th><th>a<br></th><th>b</th></tr><tr><td></td><td>1</td><td>2</td><td>1</td></tr><tr><td>&lt;</td><td>2</td><td>2</td><td>1</td></tr><tr><td></td><td>3</td><td>7</td><td>5</td></tr><tr><td>&lt;</td><td>4</td><td>7</td><td>4</td></tr><tr><td>&gt;</td><td>5</td><td>2</td><td>4</td></tr><tr><td>&lt;</td><td>6</td><td>6</td><td>3</td></tr><tr><td></td><td>7</td><td>7</td><td>4</td></tr></table>
         * <hr>
         * <pre>
         * +-------+---+---+ <br>
         * | Q     | a | b | <br>
         * +-------+---+---+ <br>
         * |   | 1 | 2 | 1 | <br>
         * +---+---+---+---+ <br>
         * | ← | 2 | 2 | 1 | <br>
         * +---+---+---+---+ <br>
         * |   | 3 | 7 | 5 | <br>
         * +---+---+---+---+ <br>
         * | ← | 4 | 7 | 4 | <br>
         * +---+---+---+---+ <br>
         * | → | 5 | 2 | 4 | <br>
         * +---+---+---+---+ <br>
         * | ← | 6 | 6 | 3 | <br>
         * +---+---+---+---+ <br>
         * |   | 7 | 7 | 4 | <br>
         * +---+---+---+---+
         * </pre>
         * <p>
         * After reduction (removal of unreachable states) it looks like this:
         * <hr>
         * <table><tr><th colspan="2">Q'<br></th><th>a</th><th>b</th></tr><tr><td>→</td><td>1<br></td><td>3<br></td><td>4<br></td></tr><tr><td><br></td><td>2</td><td>3<br></td><td>2<br></td></tr><tr><td>←</td><td>3</td><td>3<br></td><td>2<br></td></tr><tr><td>←</td><td>4</td><td>5<br></td><td>4</td></tr><tr><td></td><td>5</td><td>5</td><td>4</td></tr></table>
         * <hr>
         * <pre>
         * +-------+---+---+ <br>
         * | Q'    | a | b | <br>
         * +-------+---+---+ <br>
         * | → | 1 | 3 | 4 | <br>
         * +---+---+---+---+ <br>
         * |   | 2 | 3 | 2 | <br>
         * +---+---+---+---+ <br>
         * | ← | 3 | 3 | 2 | <br>
         * +---+---+---+---+ <br>
         * | ← | 4 | 5 | 4 | <br>
         * +---+---+---+---+ <br>
         * |   | 5 | 5 | 4 | <br>
         * +---+---+---+---+
         * </pre>
         */
        public static DFAAutomaton startEndSame() {
            String[] sigma = new String[]{
                    "a", "b"
            };
            String[] Q = new String[]{
                    "1", "2", "3", "4", "5", "6", "7"
            };
            HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
            HashMap<String, String> current = new HashMap<>();
            current.put("a", "2");
            current.put("b", "1");
            transitions.put("1", current);
            current = new HashMap<>();
            current.put("a", "2");
            current.put("b", "1");
            transitions.put("2", current);
            current = new HashMap<>();
            current.put("a", "7");
            current.put("b", "5");
            transitions.put("3", current);
            current = new HashMap<>();
            current.put("a", "7");
            current.put("b", "4");
            transitions.put("4", current);
            current = new HashMap<>();
            current.put("a", "2");
            current.put("b", "4");
            transitions.put("5", current);
            current = new HashMap<>();
            current.put("a", "6");
            current.put("b", "3");
            transitions.put("6", current);
            current = new HashMap<>();
            current.put("a", "7");
            current.put("b", "4");
            transitions.put("7", current);
            try {
                return new DFAAutomaton(Q, sigma, transitions, "5", new String[]{"2", "4", "6"});
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                return null;
            }
        }

        /**
         * Image samples/images/alphaStar.png
         * <p>
         * This automaton is <b>reduced</b> and accepts this language: <br>
         * <b>&Sigma; = {α, β}, L = {w | w &isin; &Sigma;<sup>*</sup>, w = α<sup>*</sup>} </b>
         * <p>
         * <hr>
         * <table><tr><th colspan="2">Q<br></th><th>α</th><th>β</th></tr><tr><td>↔</td><td>0</td><td>0</td><td>1</td></tr><tr><td></td><td>1</td><td>1</td><td>1</td></tr></table>
         * <hr>
         * <pre>
         * +-------+---+---+ <br>
         * | Q     | α | β | <br>
         * +-------+---+---+ <br>
         * | ↔ | 0 | 0 | 1 | <br>
         * +---+---+---+---+ <br>
         * |   | 1 | 1 | 1 | <br>
         * +---+---+---+---+
         * </pre>
         */
        public static DFAAutomaton alphaStar() {
            String[] Q = new String[]{
                    "0", "1"
            };
            String[] sigma = new String[]{
                    "\\alpha", "\\beta"
            };
            HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
            HashMap<String, String> current = new HashMap<>();
            current.put("\\alpha", "0");
            current.put("\\beta", "1");
            transitions.put("0", current);
            current = new HashMap<>();
            current.put("\\alpha", "1");
            current.put("\\beta", "1");
            transitions.put("1", current);
            try {
                return new DFAAutomaton(Q, sigma, transitions, "0", new String[]{"0"});
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                return null;
            }
        }

        /**
         * Image: samples/images/dfaRegex1.png
         * <p>
         * Accepts regex: (a*b)+(b*a)<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>a</th><th>b</th></tr>
         * <tr><td>&rarr;</td><td>0</td><td>1</td><td>2</td></tr>
         * <tr><td>&larr;</td><td>1</td><td>3</td><td>4</td></tr>
         * <tr><td>&larr;</td><td>2</td><td>4</td><td>5</td></tr>
         * <tr><td></td><td>3</td><td>3</td><td>4</td></tr>
         * <tr><td>&larr;</td><td>4</td><td>6</td><td>6</td></tr>
         * <tr><td></td><td>5</td><td>4</td><td>5</td></tr>
         * <tr><td></td><td>6</td><td>6</td><td>6</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +---+---+---+---+
         * |   |   | a | b |
         * +---+---+---+---+
         * | > |0  | 1 | 2 |
         * +---+---+---+---+
         * | < |1  | 3 | 4 |
         * +---+---+---+---+
         * | < |2  | 4 | 5 |
         * +---+---+---+---+
         * |   |3  | 3 | 4 |
         * +---+---+---+---+
         * | < |4  | 6 | 6 |
         * +---+---+---+---+
         * |   |5  | 4 | 5 |
         * +---+---+---+---+
         * |   |6  | 6 | 6 |
         * +---+---+---+---+
         * </pre>
         */
        public static DFAAutomaton regex1() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return Automaton.importFromCSV(
                        new File(Objects.requireNonNull(AutomatonSamples.class.getClassLoader().getResource("samples/csv/dfa_01_regex.csv")).getFile())).getDFA();
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }

        /**
         * Image: samples/images/dfaRegex2.png
         * <p>
         * Accepts regex: a*b*<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>b</th><th>a</th></tr>
         * <tr><td>&harr;</td><td>0</td><td>1</td><td>0</td></tr>
         * <tr><td>&larr;</td><td>1</td><td>1</td><td>2</td></tr>
         * <tr><td></td><td>2</td><td>2</td><td>2</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +----+---+---+---+
         * |    |   | b | a |
         * +----+---+---+---+
         * | <> |0  | 1 | 0 |
         * +----+---+---+---+
         * | <  |1  | 1 | 2 |
         * +----+---+---+---+
         * |    |2  | 2 | 2 |
         * +----+---+---+---+
         * </pre>
         */
        public static DFAAutomaton regex2() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return Automaton.importFromCSV(
                        new File(Objects.requireNonNull(AutomatonSamples.class.getClassLoader()
                                .getResource("samples/csv/dfa_02_regex.csv")).getFile())).getDFA();
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }

        /**
         * Image: samples/images/regex010w.png
         * <p>
         * This automaton accepts language L = {w : w ∈ {0,1}*, w = 010v, v ∈ {0,1}*}<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>0</th><th>1</th></tr>
         * <tr><td>&rarr;</td><td>0</td><td>1</td><td>E</td></tr>
         * <tr><td></td><td>1</td><td>E</td><td>2</td></tr>
         * <tr><td></td><td>E</td><td>E</td><td>E</td></tr>
         * <tr><td></td><td>2</td><td>3</td><td>E</td></tr>
         * <tr><td>&larr;</td><td>3</td><td>3</td><td>3</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +---+---+---+---+
         * |   |   | 0 | 1 |
         * +---+---+---+---+
         * | > |0  | 1 | E |
         * +---+---+---+---+
         * |   |1  | E | 2 |
         * +---+---+---+---+
         * |   |E  | E | E |
         * +---+---+---+---+
         * |   |2  | 3 | E |
         * +---+---+---+---+
         * | < |3  | 3 | 3 |
         * +---+---+---+---+
         * </pre>
         */
        public static DFAAutomaton regex010w() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return Automaton.importFromCSV(
                        new File(Objects.requireNonNull(AutomatonSamples.class.getClassLoader().getResource("samples/csv/regex010w.csv")).getFile())
                ).getDFA();
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }

        /**
         * Image: samples/images/regex101w.png
         * <p>
         * This automaton accepts language L = {w : w ∈ {0,1}*, w = 101v, v ∈ {0,1}*}<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>0</th><th>1</th></tr>
         * <tr><td>&rarr;</td><td>0</td><td>E</td><td>1</td></tr>
         * <tr><td></td><td>E</td><td>E</td><td>E</td></tr>
         * <tr><td></td><td>1</td><td>2</td><td>E</td></tr>
         * <tr><td></td><td>2</td><td>E</td><td>3</td></tr>
         * <tr><td>&larr;</td><td>3</td><td>3</td><td>3</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +---+---+---+---+
         * |   |   | 0 | 1 |
         * +---+---+---+---+
         * | > |0  | E | 1 |
         * +---+---+---+---+
         * |   |E  | E | E |
         * +---+---+---+---+
         * |   |1  | 2 | E |
         * +---+---+---+---+
         * |   |2  | E | 3 |
         * +---+---+---+---+
         * | < |3  | 3 | 3 |
         * +---+---+---+---+
         * </pre>
         */
        public static DFAAutomaton regex101w() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return Automaton.importFromCSV(
                        new File(Objects.requireNonNull(AutomatonSamples.class.getClassLoader().getResource("samples/csv/regex101w.csv")).getFile())
                ).getDFA();
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }

        /**
         * Image: samples/images/atLeastThreeAs.png
         * <p>
         * This accepts language L = {x ∈ {a,b}* | x = b*ab*ab*a(a+b)*}<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>a</th><th>b</th></tr>
         * <tr><td>&rarr;</td><td>0</td><td>1</td><td>0</td></tr>
         * <tr><td></td><td>1</td><td>2</td><td>1</td></tr>
         * <tr><td></td><td>2</td><td>3</td><td>2</td></tr>
         * <tr><td>&larr;</td><td>3</td><td>3</td><td>3</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +---+---+---+---+
         * |   |   | a | b |
         * +---+---+---+---+
         * | > |0  | 1 | 0 |
         * +---+---+---+---+
         * |   |1  | 2 | 1 |
         * +---+---+---+---+
         * |   |2  | 3 | 2 |
         * +---+---+---+---+
         * | < |3  | 3 | 3 |
         * +---+---+---+---+
         * </pre>
         */
        public static DFAAutomaton atLeastThreeAs() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return Automaton.importFromCSV(
                        new File(Objects.requireNonNull(AutomatonSamples.class.getClassLoader().getResource("samples/csv/atLeastThreeAs.csv")).getFile())
                ).getDFA();
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }

        /**
         * This will return an empty automaton
         */
        public static Automaton emptyAutomaton() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return Automaton.importFromCSV(
                        new File(Objects.requireNonNull(
                                AutomatonSamples.class.getClassLoader().getResource("samples/csv/empty.csv")).getFile())
                ).getDFA();
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }

        /**
         * Image: samples/images/containsAAA.png
         * <p>
         * This automaton accepts language: L = {x ∈ {a,b}* | x = WaaaY, W,Y ∈ {a,b}*}<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>a</th><th>b</th></tr>
         * <tr><td>&rarr;</td><td>0</td><td>1</td><td>0</td></tr>
         * <tr><td></td><td>1</td><td>2</td><td>0</td></tr>
         * <tr><td></td><td>2</td><td>3</td><td>0</td></tr>
         * <tr><td>&larr;</td><td>3</td><td>3</td><td>3</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +---+---+---+---+
         * |   |   | a | b |
         * +---+---+---+---+
         * | > |0  | 1 | 0 |
         * +---+---+---+---+
         * |   |1  | 2 | 0 |
         * +---+---+---+---+
         * |   |2  | 3 | 0 |
         * +---+---+---+---+
         * | < |3  | 3 | 3 |
         * +---+---+---+---+
         * </pre>
         */
        public static DFAAutomaton containsAAA() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return Automaton.importFromCSV(
                        new File(Objects.requireNonNull(
                                AutomatonSamples.class.getClassLoader().getResource("samples/csv/containsAAA.csv")).getFile())
                ).getDFA();
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }
    }

    // ************************
    //          NFA
    // ************************

    public static class NFASamples {
        /**
         * Image samples/images/troy.png
         * <p>
         * This automaton accepts this language: <br>
         * <b> &Sigma; = {t,r,o,y}, L = {w | w &isin;&Sigma;<sup>*</sup>, w contains "troy  " as a substring}</b>
         * <hr>
         * <table>
         * <tr><th colspan="2"></th><th>t</th><th>r</th><th>o</th><th>y</th></tr>
         * <tr><td>&rarr;</td><td>0</td><td>0,1</td><td>0</td><td>0</td><td>0</td></tr>
         * <tr><td></td><td>1</td><td>&empty;</td><td>2</td><td>&empty;</td><td>&empty;</td></tr>
         * <tr><td></td><td>2</td><td>&empty;</td><td>&empty;</td><td>3</td><td>&empty;</td></tr>
         * <tr><td></td><td>3</td><td>&empty;</td><td>&empty;</td><td>&empty;</td><td>4</td></tr>
         * <tr><td>&larr;</td><td>4</td><td>4</td><td>4</td><td>4</td><td>4</td></tr>
         * </table>
         * </div>
         * <hr>
         * <pre>
         * +---+---+-----+---+---+---+
         * |   |   | t   | r | o | y |
         * +---+---+-----+---+---+---+
         * | > |0  | 0,1 | 0 | 0 | 0 |
         * +---+---+-----+---+---+---+
         * |   |1  | ∅   | 2 | ∅ | ∅ |
         * +---+---+-----+---+---+---+
         * |   |2  | ∅   | ∅ | 3 | ∅ |
         * +---+---+-----+---+---+---+
         * |   |3  | ∅   | ∅ | ∅ | 4 |
         * +---+---+-----+---+---+---+
         * | < |4  | 4   | 4 | 4 | 4 |
         * +---+---+-----+---+---+---+
         * </pre>
         */
        public static NFAAutomaton troy() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return Automaton.importFromCSV(
                        new File(Objects.requireNonNull(
                                AutomatonSamples.class.getClassLoader().getResource("samples/csv/nfa_troy.csv")).getFile())).getNFA();
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }

        /**
         * Image: samples/images/aWa.png
         * <p>
         * This automaton accepts this language: <br>
         * <b>&Sigma; = {a,b}, L = {w | w &isin; &Sigma;<sup>*</sup>, w begins and ends with 'a'}</b>
         * </p>
         * <p>
         * <hr>
         * <table><tr><th></th><th></th><th>a</th><th>b</th></tr><tr><td>→</td><td>0<br></td><td>0,1</td><td>∅</td></tr><tr><td>←</td><td>1</td><td>∅</td><td>0</td></tr></table>
         * <hr>
         * <pre>
         * +---+---+-----+---+
         * |   |   | a   | b |
         * +---+---+-----+---+
         * | → | 0 | 0,1 | ∅ |
         * +---+---+-----+---+
         * | ← | 1 | ∅   | 0 |
         * +---+---+-----+---+
         *     </pre>
         * </p>
         * <p>
         * Its reduced DFA form is:
         * <hr>
         * <table><tr><th></th><th></th><th>a</th><th>b</th></tr><tr><td>→</td><td>0<br></td><td>0,1</td><td>Error<br></td></tr><tr><td>←</td><td>0,1</td><td>0,1<br></td><td>0</td></tr><tr><td></td><td>Error</td><td>Error</td><td>Error</td></tr></table>
         * <hr>
         * <pre>
         * +---+-------+-------+-------+
         * |   |       | a     | b     |
         * +---+-------+-------+-------+
         * | → | 0     | 0,1   | Error |
         * +---+-------+-------+-------+
         * | ← | 0,1   | 0,1   | 0     |
         * +---+-------+-------+-------+
         * |   | Error | Error | Error |
         * +---+-------+-------+-------+
         * </pre>
         */
        public static NFAAutomaton aWa() {
            String[] Q = new String[]{
                    "0", "1"
            };
            String[] sigma = new String[]{
                    "a", "b"
            };
            HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
            HashMap<String, String> curr;
            curr = new HashMap<>();
            curr.put("a", "0,1");
            curr.put("b", "");
            transitions.put("0", curr);
            curr = new HashMap<>();
            curr.put("a", "");
            curr.put("b", "0");
            transitions.put("1", curr);
            String[] initialStates = new String[]{"0"};
            String[] acceptingStates = new String[]{"1"};
            try {
                return new NFAAutomaton(Q, sigma, transitions, initialStates, acceptingStates);
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                return null;
            }
        }

        /**
         * Image: samples/images/nfaCustom1.png
         * <p>
         * This automaton accepts this language: <br>
         * <b>&Sigma; = {a,b}, L = {w | w &isin; &Sigma;<sup>*</sup>, w satisfies: second character is 'a', and
         * the last but one character is ’b’ and |w| ≥ 3 or w = &epsilon;}</b>
         * </p>
         * <p>
         * <hr>
         * <table><tr><th></th><th></th><th>a</th><th>b</th></tr><tr><td>↔</td><td>0<br></td><td>1</td><td>1</td></tr><tr><td><br></td><td>1</td><td>2</td><td>∅</td></tr><tr><td></td><td>2</td><td>2</td><td>2,3</td></tr><tr><td></td><td>3</td><td>4</td><td>4</td></tr><tr><td>←</td><td>4</td><td>∅</td><td>∅</td></tr></table>
         * <hr>
         * <pre>
         * +---+---+---+-----+
         * |   |   | a | b   |
         * +---+---+---+-----+
         * | ↔ | 0 | 1 | 1   |
         * +---+---+---+-----+
         * |   | 1 | 2 | ∅   |
         * +---+---+---+-----+
         * |   | 2 | 2 | 2,3 |
         * +---+---+---+-----+
         * |   | 3 | 4 | 4   |
         * +---+---+---+-----+
         * | ← | 4 | ∅ | ∅   |
         * +---+---+---+-----+
         * </pre></p>
         *
         * The reduced DFA form is:
         * <hr>
         * <table><tr><th><br></th><th></th><th>a</th><th>b</th></tr><tr><td>↔</td><td>0<br></td><td>1</td><td>1</td></tr><tr><td><br></td><td>1</td><td>2</td><td>Error</td></tr><tr><td></td><td>2</td><td>2</td><td>2,3</td></tr><tr><td></td><td>Error</td><td>Error</td><td>Error</td></tr><tr><td><br></td><td>2,3</td><td>2,4</td><td>2,3,4</td></tr><tr><td>←</td><td>2,4</td><td>2</td><td>2,3</td></tr><tr><td>←</td><td>2,3,4</td><td>2,4</td><td>2,3,4</td></tr></table>
         * <hr>
         * <pre>
         * +---+-------+-------+-------+
         * |   |       | a     | b     |
         * +---+-------+-------+-------+
         * | ↔ | 0     | 1     | 1     |
         * +---+-------+-------+-------+
         * |   | 1     | 2     | Error |
         * +---+-------+-------+-------+
         * |   | 2     | 2     | 2,3   |
         * +---+-------+-------+-------+
         * |   | Error | Error | Error |
         * +---+-------+-------+-------+
         * |   | 2,3   | 2,4   | 2,3,4 |
         * +---+-------+-------+-------+
         * | ← | 2,4   | 2     | 2,3   |
         * +---+-------+-------+-------+
         * | ← | 2,3,4 | 2,4   | 2,3,4 |
         * +---+-------+-------+-------+
         * </pre>
         */
        public static NFAAutomaton custom1() {
            String[] states = new String[]{"0", "1", "2", "3", "4"};
            String[] sigma = new String[]{"a", "b"};
            HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
            HashMap<String, String> curr;
            curr = new HashMap<>();
            curr.put("a", "1");
            curr.put("b", "1");
            transitions.put("0", curr);
            curr = new HashMap<>();
            curr.put("a", "2");
            curr.put("b", "");
            transitions.put("1", curr);
            curr = new HashMap<>();
            curr.put("a", "2");
            curr.put("b", "2,3");
            transitions.put("2", curr);
            curr = new HashMap<>();
            curr.put("a", "4");
            curr.put("b", "4");
            transitions.put("3", curr);
            curr = new HashMap<>();
            curr.put("a", "");
            curr.put("b", "");
            transitions.put("4", curr);
            String[] initials = new String[]{"0"};
            String[] accepting = new String[]{"0", "4"};
            try {
                return new NFAAutomaton(states, sigma, transitions, initials, accepting);
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                return null;
            }
        }

        /**
         * Image: samples/images/nfaRegex1.png <br>
         * This automaton accepts this language: <br>
         * <b>L = {w | w &isin; {a,b,c}<sup>*</sup>, w is described by regular expression: (ba)<sup>*</sup>c<sup>*</sup>a(bc)<sup>*</sup>}</b>
         * <p>
         * <hr>
         * <table><tr><th><br></th><th></th><th>a</th><th>b</th><th>c<br></th></tr><tr><td>→</td><td>0<br></td><td>4</td><td>1</td><td>3</td></tr><tr><td><br></td><td>1</td><td>2<br></td><td><br></td><td></td></tr><tr><td></td><td>2</td><td>4</td><td>1</td><td>3</td></tr><tr><td></td><td>3</td><td>4</td><td></td><td>3</td></tr><tr><td>←</td><td>4</td><td></td><td>5</td><td></td></tr><tr><td></td><td>5</td><td></td><td></td><td>6</td></tr><tr><td>←</td><td>6</td><td></td><td>5</td><td></td></tr></table>
         * <hr>
         * <pre>
         * +---+---+---+---+---+
         * |   |   | a | b | c |
         * +---+---+---+---+---+
         * | → | 0 | 4 | 1 | 3 |
         * +---+---+---+---+---+
         * |   | 1 | 2 |   |   |
         * +---+---+---+---+---+
         * |   | 2 | 4 | 1 | 3 |
         * +---+---+---+---+---+
         * |   | 3 | 4 |   | 3 |
         * +---+---+---+---+---+
         * | ← | 4 |   | 5 |   |
         * +---+---+---+---+---+
         * |   | 5 |   |   | 6 |
         * +---+---+---+---+---+
         * | ← | 6 |   | 5 |   |
         * +---+---+---+---+---+
         * </pre>
         * <p>
         * <p>
         * In reduced DFA form it is:
         * <hr>
         * <table>
         * <tr><td></td><td></td><td>a</td><td>b</td><td>c</td></tr>
         * <tr><td>&rarr;</td><td>0</td><td>1</td><td>2</td><td>3</td></tr>
         * <tr><td>&larr;</td><td>1</td><td>4</td><td>5</td><td>4</td></tr>
         * <tr><td></td><td>2</td><td>0</td><td>4</td><td>4</td></tr>
         * <tr><td></td><td>3</td><td>1</td><td>4</td><td>3</td></tr>
         * <tr><td></td><td>4</td><td>4</td><td>4</td><td>4</td></tr>
         * <tr><td></td><td>5</td><td>4</td><td>4</td><td>1</td></tr>
         * </table>
         * <hr>
         * <pre>
         * a b c
         * >0 1 2 3
         * <1 4 5 4
         * 2 0 4 4
         * 3 1 4 3
         * 4 4 4 4
         * 5 4 4 1
         * </pre>
         * </p>
         */
        public static NFAAutomaton regex1() {
            String[] states = new String[]{"0", "1", "2", "3", "4", "5", "6"};
            String[] letters = new String[]{"a", "b", "c"};
            HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
            HashMap<String, String> curr;
            curr = new HashMap<>();
            curr.put("a", "4");
            curr.put("b", "1");
            curr.put("c", "3");
            transitions.put("0", curr);
            curr = new HashMap<>();
            curr.put("a", "2");
            curr.put("b", "");
            curr.put("c", "");
            transitions.put("1", curr);
            curr = new HashMap<>();
            curr.put("a", "4");
            curr.put("b", "1");
            curr.put("c", "3");
            transitions.put("2", curr);
            curr = new HashMap<>();
            curr.put("a", "4");
            curr.put("b", "");
            curr.put("c", "3");
            transitions.put("3", curr);
            curr = new HashMap<>();
            curr.put("a", "");
            curr.put("b", "5");
            curr.put("c", "");
            transitions.put("4", curr);
            curr = new HashMap<>();
            curr.put("a", "");
            curr.put("b", "");
            curr.put("c", "6");
            transitions.put("5", curr);
            curr = new HashMap<>();
            curr.put("a", "");
            curr.put("b", "5");
            curr.put("c", "");
            transitions.put("6", curr);
            String[] init = new String[]{"0"};
            String[] acc = new String[]{"4", "6"};
            try {
                return new NFAAutomaton(states, letters, transitions, init, acc);
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                return null;
            }
        }

        /**
         * Image: samples/images/onlyAlpha.png
         * This automaton is actually a reduced DFA automaton which accepts language: <br>
         * <b>L = {w | w &isin; {\alpha, \beta}<sup>*</sup>, w contains only '\alpha'}</b>
         * <p>
         * <hr>
         * <table><tr><th><br></th><th></th><th>\alpha</th><th>\beta<br></th></tr><tr><td>↔</td><td>0<br></td><td>0<br></td><td>1</td></tr><tr><td><br></td><td>1</td><td>1<br></td><td>Error</td></tr></table>
         * <hr>
         * <pre>
         * +---+---+--------+----------+
         * |   |   | \alpha | \beta    |
         * +---+---+--------+----------+
         * | ↔ | 0 | 0      | 1        |
         * +---+---+--------+----------+
         * |   | 1 | 1      | Error    |
         * +---+---+--------+----------+
         * </pre>
         * </p>
         */
        public static NFAAutomaton onlyAlpha() {
            String[] states = new String[]{"0", "1"};
            String[] letters = new String[]{"\\alpha", "\\beta"};
            HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
            HashMap<String, String> curr;
            curr = new HashMap<>();
            curr.put("\\alpha", "0");
            curr.put("\\beta", "1");
            transitions.put("0", curr);
            curr = new HashMap<>();
            curr.put("\\alpha", "1");
            transitions.put("1", curr);
            String[] initial = new String[]{"0"};
            String[] accepting = new String[]{"0"};
            try {
                return new NFAAutomaton(states, letters, transitions, initial, accepting);
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                return null;
            }
        }

        /**
         * Image: samples/images/nfaRegex2.png
         * <p>
         * Automaton accepts: ab*+ba*<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>a</th><th>b</th></tr>
         * <tr><td>&rarr;</td><td>0</td><td>1</td><td>2</td></tr>
         * <tr><td>&larr;</td><td>1</td><td>&empty;</td><td>1</td></tr>
         * <tr><td>&larr;</td><td>2</td><td>2</td><td>&empty;</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +---+---+---+---+
         * |   |   | a | b |
         * +---+---+---+---+
         * | > |0  | 1 | 2 |
         * +---+---+---+---+
         * | < |1  | ∅ | 1 |
         * +---+---+---+---+
         * | < |2  | 2 | ∅ |
         * +---+---+---+---+
         * </pre>
         */
        public static NFAAutomaton regex2() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return Automaton.importFromCSV(
                        new File(Objects.requireNonNull(AutomatonSamples.class.getClassLoader()
                                .getResource("samples/csv/nfa_01_regex.csv")).getFile())).getNFA();
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }

        /**
         * Image: samples/images/regex3.png
         * <p>
         * null<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>a</th><th>b</th><th>c</th></tr>
         * <tr><td></td><td>a0</td><td>a0</td><td>b1</td><td>c2</td></tr>
         * <tr><td></td><td>b1</td><td>a0</td><td>b1</td><td>c2</td></tr>
         * <tr><td></td><td>c2</td><td>a3</td><td>b4</td><td>c5</td></tr>
         * <tr><td></td><td>a3</td><td>a3</td><td>b4</td><td>c5</td></tr>
         * <tr><td></td><td>b4</td><td>a3</td><td>b4</td><td>c5</td></tr>
         * <tr><td>&larr;</td><td>c5</td><td>&empty;</td><td>&empty;</td><td>&empty;</td></tr>
         * <tr><td>&rarr;</td><td>I</td><td>a0</td><td>b1</td><td>c2</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +---+----+----+----+----+
         * |   |    | a  | b  | c  |
         * +---+----+----+----+----+
         * |   |a0  | a0 | b1 | c2 |
         * +---+----+----+----+----+
         * |   |b1  | a0 | b1 | c2 |
         * +---+----+----+----+----+
         * |   |c2  | a3 | b4 | c5 |
         * +---+----+----+----+----+
         * |   |a3  | a3 | b4 | c5 |
         * +---+----+----+----+----+
         * |   |b4  | a3 | b4 | c5 |
         * +---+----+----+----+----+
         * | < |c5  | ∅  | ∅  | ∅  |
         * +---+----+----+----+----+
         * | > |I   | a0 | b1 | c2 |
         * +---+----+----+----+----+
         * </pre>
         */
        public static NFAAutomaton regex3() {
            Automaton a = FromRegexConverter.getAutomaton("(a+b)*c(a+b)*c");
            a.setDescription("This automaton accepts language L = {w ∈ {a,b,c}* | w = (a+b)*c(a+b)*c = XcYc, X,Y ∈ {a,b}*}");
            return a.getNFA();
        }

        /**
         * Image: samples/images/regex4.png
         * <p>
         * This automaton accepts language L = {w ∈ {a,b}* | w = a*ab(b+ε)}<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>a</th><th>b</th></tr>
         * <tr><td></td><td>a0</td><td>a0,a1</td><td>&empty;</td></tr>
         * <tr><td></td><td>a1</td><td>&empty;</td><td>b2</td></tr>
         * <tr><td>&larr;</td><td>b2</td><td>&empty;</td><td>b3</td></tr>
         * <tr><td>&larr;</td><td>b3</td><td>&empty;</td><td>&empty;</td></tr>
         * <tr><td>&rarr;</td><td>I</td><td>a0,a1</td><td>&empty;</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +---+----+-------+----+
         * |   |    | a     | b  |
         * +---+----+-------+----+
         * |   |a0  | a0,a1 | ∅  |
         * +---+----+-------+----+
         * |   |a1  | ∅     | b2 |
         * +---+----+-------+----+
         * | < |b2  | ∅     | b3 |
         * +---+----+-------+----+
         * | < |b3  | ∅     | ∅  |
         * +---+----+-------+----+
         * | > |I   | a0,a1 | ∅  |
         * +---+----+-------+----+
         * </pre>
         */
        public static NFAAutomaton regex4() {
            Automaton a = FromRegexConverter.getAutomaton("a*ab(b+ε)");
            a.setDescription("This automaton accepts language L = {w ∈ {a,b}* | w = a*ab(b+ε)}");
            return a.getNFA();
        }

        /**
         * Image: samples/images/listSyntaxCheck.png
         * <p>
         * This automaton accepts language that accepts words as such: 'list id;n;id;id;...;n;n;id#', where every string begins with the word: list and it ends with a symbol: #. In between of these two symbols is a list of identifiers (id) and numbers (n), in which every two elements are divided from each other with a semicolon.<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>list </th><th>id</th><th>n</th><th>#</th><th>;</th></tr>
         * <tr><td>&rarr;</td><td>S</td><td>L</td><td>&empty;</td><td>&empty;</td><td>&empty;</td><td>&empty;</td></tr>
         * <tr><td></td><td>L</td><td>&empty;</td><td>R</td><td>R</td><td>&empty;</td><td>&empty;</td></tr>
         * <tr><td></td><td>R</td><td>&empty;</td><td>&empty;</td><td>&empty;</td><td>A</td><td>L</td></tr>
         * <tr><td>&larr;</td><td>A</td><td>&empty;</td><td>&empty;</td><td>&empty;</td><td>&empty;</td><td>&empty;</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +---+---+-------+----+---+---+---+
         * |   |   | list  | id | n | # | ; |
         * +---+---+-------+----+---+---+---+
         * | > |S  | L     | ∅  | ∅ | ∅ | ∅ |
         * +---+---+-------+----+---+---+---+
         * |   |L  | ∅     | R  | R | ∅ | ∅ |
         * +---+---+-------+----+---+---+---+
         * |   |R  | ∅     | ∅  | ∅ | A | L |
         * +---+---+-------+----+---+---+---+
         * | < |A  | ∅     | ∅  | ∅ | ∅ | ∅ |
         * +---+---+-------+----+---+---+---+
         * </pre>
         */
        public static NFAAutomaton listSyntaxCheck() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return Automaton.importFromCSV(new File(Objects.requireNonNull(AutomatonSamples.class.getClassLoader().getResource("samples/csv/listSyntax.csv")).getFile())).getNFA();
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }

        /**
         * Image: samples/images/bAtEnd.png
         * <p>
         * This automaton accepts language: L = {w ∈ {a,b}* | w has b as third symbol from the end}<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>a</th><th>b</th></tr>
         * <tr><td>&rarr;</td><td>1</td><td>1</td><td>1,2</td></tr>
         * <tr><td></td><td>2</td><td>3</td><td>3</td></tr>
         * <tr><td></td><td>3</td><td>4</td><td>4</td></tr>
         * <tr><td>&larr;</td><td>4</td><td>&empty;</td><td>&empty;</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +---+---+---+-----+
         * |   |   | a | b   |
         * +---+---+---+-----+
         * | > |1  | 1 | 1,2 |
         * +---+---+---+-----+
         * |   |2  | 3 | 3   |
         * +---+---+---+-----+
         * |   |3  | 4 | 4   |
         * +---+---+---+-----+
         * | < |4  | ∅ | ∅   |
         * +---+---+---+-----+
         * </pre>
         */
        public static NFAAutomaton bAtEnd() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return Automaton.importFromCSV(
                        new File(Objects.requireNonNull(AutomatonSamples.class.getClassLoader()
                                .getResource("samples/csv/bAtEnd.csv")).getFile())).getNFA();
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }
    }

    // ************************
    //          ENFA
    // ************************

    public static class ENFASamples {
        /**
         * Image: samples/images/oneLetter.png
         * This automaton is accepting this language: <br>
         * <b>L = {w | w &isin; {a, b}}</b>
         * <p>
         * <hr>
         * <table>
         * <tr><th> </th><th> </th><th>ε</th><th>a</th><th>b</th></tr>
         * <tr><td>→</td><td>0</td><td>1</td><td>2</td><td> </td></tr>
         * <tr><td> </td><td>1</td><td> </td><td> </td><td>3</td></tr>
         * <tr><td>←</td><td>2</td><td> </td><td> </td><td> </td></tr>
         * <tr><td>←</td><td>3</td><td> </td><td> </td><td> </td></tr>
         * </table>
         * <hr>
         * <pre>
         * +---+---+---+---+---+
         * |   |   | ε | a | b |
         * +---+---+---+---+---+
         * | → | 0 | 1 | 2 |   |
         * +---+---+---+---+---+
         * |   | 1 |   |   | 3 |
         * +---+---+---+---+---+
         * | ← | 2 |   |   |   |
         * +---+---+---+---+---+
         * | ← | 3 |   |   |   |
         * +---+---+---+---+---+
         * </pre>
         */
        public static ENFAAutomaton oneLetter() {
            String[] states = new String[]{"0", "1", "2", "3"};
            String[] sigma = new String[]{"a", "ε", "b"};
            HashMap<String, HashMap<String, String[]>> transitions = new HashMap<>();
            HashMap<String, String[]> curr = new HashMap<>();
            curr.put("a", new String[]{"2"});
            curr.put("b", new String[]{});
            curr.put("ε", new String[]{"1"});
            transitions.put("0", curr);
            curr = new HashMap<>();
            curr.put("ε", new String[0]);
            curr.put("a", new String[0]);
            curr.put("b", new String[]{"3"});
            transitions.put("1", curr);
            curr = new HashMap<>();
            curr.put("ε", new String[0]);
            curr.put("a", new String[0]);
            curr.put("b", new String[0]);
            transitions.put("2", curr);
            curr = new HashMap<>();
            curr.put("ε", new String[0]);
            curr.put("a", new String[0]);
            curr.put("b", new String[0]);
            transitions.put("3", curr);
            String[] initial = new String[]{"0"};
            String[] accepting = new String[]{"2", "3"};

            return new ENFAAutomaton(states, sigma, initial, accepting, transitions);
        }

        /**
         * Image: samples/images/enfaRegex1.png
         * This automaton accepts language: <br>
         * <b>L = {w | w is described by regex: r = (a + b*)a }</b>
         * <p>
         * <hr>
         * <table><tr><th></th><th></th><th>ε</th><th></th><th></th></tr><tr><td>→</td><td>0</td><td>2</td><td>1</td><td></td></tr><tr><td></td><td>1</td><td></td><td>3</td><td></td></tr><tr><td><br></td><td>2</td><td>1</td><td></td><td>2</td></tr><tr><td>←</td><td>3</td><td></td><td></td><td></td></tr></table>
         * <hr>
         * <pre>
         * +---+---+---+---+---+
         * |   |   | ε | a | b |
         * +---+---+---+---+---+
         * | → | 0 | 2 | 1 |   |
         * +---+---+---+---+---+
         * |   | 1 |   | 3 |   |
         * +---+---+---+---+---+
         * |   | 2 | 1 |   | 2 |
         * +---+---+---+---+---+
         * | ← | 3 |   |   |   |
         * +---+---+---+---+---+
         * </pre>
         */
        public static ENFAAutomaton regex1() throws FileNotFoundException, UnsupportedEncodingException {
            File f = new File(Objects.requireNonNull(AutomatonSamples.class.getClassLoader().getResource("samples/csv/enfaRegex1.csv")).getFile());
            try {
                return (ENFAAutomaton) Automaton.importFromCSV(f);
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }

        /**
         * Image: samples/images/enfaRegex2.png
         * This automaton accepts language: <br>
         * <b> L = {w | w is described by regular expression r = aab(a+b)* }</b>
         * <p>
         * <hr>
         * <table><tr><th></th><th></th><th>a</th><th>b</th></tr><tr><td>→</td><td>0</td><td>1</td><td></td></tr><tr><td></td><td>1</td><td>2</td><td></td></tr><tr><td></td><td>2</td><td></td><td>3</td></tr><tr><td>←</td><td>3</td><td></td><td></td></tr></table>
         * <hr>
         * <pre>
         * +---+---+---+---+
         * |   |   | a | b |
         * +---+---+---+---+
         * | → | 0 | 1 |   |
         * +---+---+---+---+
         * |   | 1 | 2 |   |
         * +---+---+---+---+
         * |   | 2 |   | 3 |
         * +---+---+---+---+
         * | ← | 3 |   |   |
         * +---+---+---+---+
         */
        public static ENFAAutomaton regex2() {
            String[] Q = new String[]{"0", "1", "2", "3"};
            String[] sigma = new String[]{"a", "b"};
            String[] initials = new String[]{"0"};
            String[] accepting = new String[]{"3"};
            HashMap<String, HashMap<String, String[]>> transitions = new HashMap<>();
            HashMap<String, String[]> curr = new HashMap<>();
            curr.put("a", new String[]{"1"});
            transitions.put("0", curr);
            curr = new HashMap<>();
            curr.put("a", new String[]{"2"});
            transitions.put("1", curr);
            curr = new HashMap<>();
            curr.put("b", new String[]{"3"});
            transitions.put("2", curr);
            curr = new HashMap<>();
            curr.put("a", new String[]{});
            curr.put("b", new String[]{});
            transitions.put("3", curr);

            return new ENFAAutomaton(Q, sigma, initials, accepting, transitions);
        }

        /**
         * Image: samples/images/aa_c_a.png
         * This automaton accepts language: <br>
         * <b>L = {w | w &isin; {aa, c, a} }</b>
         * <p>
         * <hr>
         * <table><tr><th></th><th></th><th>ε</th><th>c</th><th>a</th></tr><tr><td>→</td><td>0</td><td>1</td><td></td><td>2</td></tr><tr><td></td><td>1</td><td>2</td><td>3</td><td></td></tr><tr><td></td><td>2</td><td></td><td></td><td>4</td></tr><tr><td>←</td><td>3</td><td></td><td></td><td></td></tr><tr><td>←</td><td>4</td><td></td><td></td><td></td></tr><tr><td>→</td><td>5</td><td>0</td><td></td><td></td></tr></table>
         * <hr>
         * <pre>
         * +---+---+---+---+---+
         * |   |   | ε | c | a |
         * +---+---+---+---+---+
         * | → | 0 | 1 |   | 2 |
         * +---+---+---+---+---+
         * |   | 1 | 2 | 3 |   |
         * +---+---+---+---+---+
         * |   | 2 |   |   | 4 |
         * +---+---+---+---+---+
         * | ← | 3 |   |   |   |
         * +---+---+---+---+---+
         * | ← | 4 |   |   |   |
         * +---+---+---+---+---+
         * | → | 5 | 0 |   |   |
         * +---+---+---+---+---+
         */
        public static ENFAAutomaton aa_c_a() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return (ENFAAutomaton) Automaton.importFromCSV(
                        new File(Objects.requireNonNull(AutomatonSamples.class.getClassLoader().getResource("samples/csv/aa_c_a.csv")).getFile()));
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }

        /**
         * Image: samples/images/factors_aba.png
         * <p>
         * This automaton accepts language: L = {w is a factor of word 'aba'}<hr>
         * <p>
         * <table>
         * <tr><th colspan="2"></th><th>eps</th><th>a</th><th>b</th></tr>
         * <tr><td>&rarr;</td><td>0</td><td>1,2</td><td>1</td><td>&empty;</td></tr>
         * <tr><td>&larr;</td><td>1</td><td>&empty;</td><td>&empty;</td><td>2</td></tr>
         * <tr><td>&larr;</td><td>2</td><td>&empty;</td><td>3</td><td>&empty;</td></tr>
         * <tr><td>&larr;</td><td>3</td><td>&empty;</td><td>&empty;</td><td>&empty;</td></tr>
         * </table>
         * </div><hr>
         * <pre>
         * +---+---+-----+---+---+
         * |   |   | ε   | a | b |
         * +---+---+-----+---+---+
         * | > |0  | 1,2 | 1 | ∅ |
         * +---+---+-----+---+---+
         * | < |1  | ∅   | ∅ | 2 |
         * +---+---+-----+---+---+
         * | < |2  | ∅   | 3 | ∅ |
         * +---+---+-----+---+---+
         * | < |3  | ∅   | ∅ | ∅ |
         * +---+---+-----+---+---+
         * </pre>
         */
        public static ENFAAutomaton factors_aba() throws FileNotFoundException, UnsupportedEncodingException {
            try {
                return Automaton.importFromCSV(
                        new File(Objects.requireNonNull(AutomatonSamples.class.getClassLoader()
                                .getResource("samples/csv/factors_aba.csv")).getFile())).getENFA();
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                LOGGER.severe("Invalid table for example!");
                return null;
            }
        }
    }

    /**
     * Used for generating javadoc
     */
    private static void printFormatted(StringBuilder sb) {
        Scanner sc = new Scanner(new ByteArrayInputStream(sb.toString().getBytes(Charset.defaultCharset())));
        System.out.println("    /**");
        boolean skip = false;
        while (sc.hasNext()) {
            String line = sc.nextLine();
            if (!skip && line.contains("div id=\"scoped-content\"")) {
                skip = true;
            }
            if (skip && line.contains("</style>")) {
                skip = false;
                continue;
            }
            if (skip) continue;
            System.out.print("     * ");
            System.out.print(line + "\n");
        }
        System.out.println("     */");
    }


    // This is here only for generating tikz code for image generation of new samples
    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        Method m = null;
        Method[] declaredMethods = DFASamples.class.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            //Fill here!
            if (declaredMethod.getName().equals("not3kPlus1As")) {
                m = declaredMethod;
                break;
            }
        }
        if (m == null) {
            System.err.println("Method not found!");
            return;
        }

        Automaton a = (Automaton) m.invoke(null);
        assert (a != null);
        StringBuilder sb = new StringBuilder();
        sb.append("Image: samples/images/").append(m.getName()).append(".png\n");
        sb.append("<p>\n");
        sb.append(a.getDescription());
        sb.append("<hr>\n");
        sb.append(a.exportToString().getHTML());
        sb.append("<hr>\n<pre>\n");
        sb.append(a.exportToString().getBorderedPlainText());
        sb.append("</pre>");
        printFormatted(sb);
        System.out.println("\n\n\n");
        System.out.println(a.exportToString().getTIKZ());
    }

}
