package cz.cvut.fel.horovtom.logic.dfa;

import cz.cvut.fel.horovtom.logic.DFAAutomaton;

import java.util.HashMap;

public class DFASamples {
    /**
     * Ex. 3.2.3. in script. Image: test/dfa_tests/1.png
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
    public static DFAAutomaton getDFA1() {
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

        return new DFAAutomaton(Q, sigma, transitions, "5", new String[]{"2", "4", "6"});
    }

    /**
     * Image test/dfa_tests/2.png
     * <p>
     * This automaton is <b>reduced</b> and it accepts this language: <br>
     * <b> &Sigma; = {0.12, -6.38, 0, 213.002}, L = {w | w &isin;&Sigma;<sup>*</sup>, w contains "lolipop" as a substring}</b>
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
    public static DFAAutomaton getDFA2() {
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

        return new DFAAutomaton(Q, sigma, transitions, "0", new String[]{"7"});
    }

    /**
     * Image test/dfa_tests/3.png
     * <p>
     * This automaton is <b>reduced</b> and accepts this language: <br>
     * <b>&Sigma; = {α, β}, L = {w | w &isin; &Sigma<sup>*</sup>, w = α<sup>*</sup>} </b>
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
    public static DFAAutomaton getDFA3() {
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
        return new DFAAutomaton(Q, sigma, transitions, "0", new String[]{"0"});
    }
}
