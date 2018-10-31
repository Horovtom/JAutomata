package cz.cvut.fel.horovtom.logic.automata.samples;

import cz.cvut.fel.horovtom.automata.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ENFASamplesTest {

    @Test
    public void testFactors_aba() throws FileNotFoundException, UnsupportedEncodingException {
        ENFAAutomaton a = AutomatonSamples.ENFASamples.factors_aba();
        assertTrue(a != null);
        assertTrue(a.acceptsWord(""));
        assertTrue(a.acceptsWord("a"));
        assertTrue(a.acceptsWord("b"));
        assertTrue(a.acceptsWord("ab"));
        assertTrue(a.acceptsWord("aba"));
        assertTrue(a.acceptsWord("ba"));
        assertFalse(a.acceptsWord("aa"));
        assertFalse(a.acceptsWord("bb"));
        assertFalse(a.acceptsWord("abaa"));
        assertFalse(a.acceptsWord("baba"));
        assertFalse(a.acceptsWord("abab"));
    }

    public static void main(String[] args) {
        System.out.println("Vlož číslo a: ");
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        System.out.println("---");
        int[] pole = new int[a];
        int suma = 0;
        for (int i = 0; i < pole.length; i++) {
            pole[i] = i + 1;
            suma += pole[i];
        }
        System.out.println("Součet prvků pole je: " + suma);
    }
}
