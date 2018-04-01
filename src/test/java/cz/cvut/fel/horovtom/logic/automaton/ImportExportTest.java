package cz.cvut.fel.horovtom.logic.automaton;

import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.logic.NFAAutomaton;
import cz.cvut.fel.horovtom.logic.abstracts.Automaton;
import cz.cvut.fel.horovtom.logic.samples.Samples;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImportExportTest {
    @Test
    public void testImportCSVDFA() {
        Automaton automaton = Automaton.importFromCSV(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("dfa.csv")).getFile()));
        assertTrue("Automaton did not import correctly", automaton != null && automaton instanceof ENFAAutomaton);
        assertEquals("Automaton had wrong number of states", 4, automaton.getQSize());
        assertEquals("Automaton had wrong number of letters", 3, automaton.getSigmaSize());
        assertEquals("Automaton import had wrong result",
                "            ambiente bellethorne callea  \n" +
                        " >andartes  andartes submarine   ERTEPLE \n" +
                        "< submarine GHETTO   submarine   ERTEPLE \n" +
                        "  ERTEPLE   ERTEPLE  submarine   GHETTO  \n" +
                        "< GHETTO    ERTEPLE  GHETTO      GHETTO  "
                , automaton.getAutomatonTablePlainText());

    }

    @Test
    public void testImportCSVNFA() {
        Automaton automaton = Automaton.importFromCSV(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("nfa.csv")).getFile()));
        assertTrue("Automaton did not import correctly", automaton != null && automaton instanceof ENFAAutomaton);
        assertEquals("Automaton had wrong number of states", 3, automaton.getQSize());
        assertEquals("Automaton had wrong number of letters", 2, automaton.getSigmaSize());
        assertEquals("Automaton import had wrong result",
                "    a     b   \n" +
                        "<>A A,B,C     \n" +
                        " >B A     C   \n" +
                        "  C A     B,A "
                , automaton.getAutomatonTablePlainText());

    }

    @Test
    public void testImportCSVENFA() {
        Automaton automaton = Automaton.importFromCSV(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("enfa.csv")).getFile()));
        assertTrue("Automaton did not import correctly", automaton != null && automaton instanceof ENFAAutomaton);
        assertEquals("Automaton had wrong number of states", 3, automaton.getQSize());
        assertEquals("Automaton had wrong number of letters", 3, automaton.getSigmaSize());
        assertEquals("Automaton import had wrong result",
                "    ε a     b   \n" +
                        "<>A C A,B,C     \n" +
                        " >B   A     C   \n" +
                        "  C   A     B,A "
                , automaton.getAutomatonTablePlainText());

    }

    @Test
    public void testExportCSVDFA() {
        HashMap<String, HashMap<String, String>> map = new HashMap<>();
        HashMap<String, String> current;
        current = new HashMap<>();
        current.put("a", "A");
        current.put("b", "S");
        map.put("S", current);
        current = new HashMap<>();
        current.put("a", "B");
        current.put("b", "B");
        map.put("A", current);
        current = new HashMap<>();
        current.put("a", "S");
        current.put("b", "A");
        map.put("B", current);
        String[] states = new String[]{"S", "A", "B"};
        String[] letters = new String[]{"a", "b"};
        String[] accepting = new String[]{"A", "B"};
        String initial = "S";
        Automaton automaton = new DFAAutomaton(states, letters, map, initial, accepting);
        try {
            File file = File.createTempFile("dfa", "csv");
            automaton.exportCSV(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            assertEquals("CSV was incorrect", ",,\"a\",\"b\"", br.readLine());
            assertEquals("CSV was incorrect", ">,\"S\",\"A\",\"S\"", br.readLine());
            assertEquals("CSV was incorrect", "<,\"A\",\"B\",\"B\"", br.readLine());
            assertEquals("CSV was incorrect", "<,\"B\",\"S\",\"A\"", br.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExportCSVNFA() {
        HashMap<String, HashMap<String, String[]>> map = new HashMap<>();
        HashMap<String, String[]> current;
        current = new HashMap<>();
        current.put("a", new String[0]);
        current.put("b", new String[]{"S", "A"});
        map.put("S", current);
        current = new HashMap<>();
        current.put("a", new String[]{"S", "B"});
        current.put("b", new String[]{"B"});
        map.put("A", current);
        current = new HashMap<>();
        current.put("a", new String[0]);
        current.put("b", new String[0]);
        map.put("B", current);
        String[] states = new String[]{"S", "A", "B"};
        String[] letters = new String[]{"a", "b"};
        String[] initials = new String[]{"S", "A"};
        String[] accepting = new String[]{"A"};
        NFAAutomaton automaton = new NFAAutomaton(states, letters, initials, accepting, map);
        try {
            File file = File.createTempFile("nfa", "csv");
            automaton.exportCSV(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            assertEquals("CSV was incorrect", ",,\"a\",\"b\"", br.readLine());
            assertEquals("CSV was incorrect", ">,\"S\",,\"S,A\"", br.readLine());
            assertEquals("CSV was incorrect", "<>,\"A\",\"S,B\",\"B\"", br.readLine());
            assertEquals("CSV was incorrect", ",\"B\",,", br.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExportCSVNFA2() {
        NFAAutomaton automaton = Samples.getNFA3();
        try {
            File file = File.createTempFile("nfa2", "csv");
            automaton.exportCSV(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            assertEquals("CSV was incorrect", ",,\"a\",\"b\",\"c\"", br.readLine());
            assertEquals("CSV was incorrect", ">,\"0\",\"4\",\"1\",\"3\"", br.readLine());
            assertEquals("CSV was incorrect", ",\"1\",\"2\",,", br.readLine());
            assertEquals("CSV was incorrect", ",\"2\",\"4\",\"1\",\"3\"", br.readLine());
            assertEquals("CSV was incorrect", ",\"3\",\"4\",,\"3\"", br.readLine());
            assertEquals("CSV was incorrect", "<,\"4\",,\"5\",", br.readLine());
            assertEquals("CSV was incorrect", ",\"5\",,,\"6\"", br.readLine());
            assertEquals("CSV was incorrect", "<,\"6\",,\"5\",", br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExportCSVENFA() {
        ENFAAutomaton automaton =
                Samples.getENFA1();
        try {
            File file = File.createTempFile("enfa1", "csv");
            automaton.exportCSV(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            assertEquals("CSV was incorrect", ",,\"ε\",\"a\",\"b\"", br.readLine());
            assertEquals("CSV was incorrect", ">,\"0\",\"1\",\"2\",", br.readLine());
            assertEquals("CSV was incorrect", ",\"1\",,,\"3\"", br.readLine());
            assertEquals("CSV was incorrect", "<,\"2\",,,", br.readLine());
            assertEquals("CSV was incorrect", "<,\"3\",,,", br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
