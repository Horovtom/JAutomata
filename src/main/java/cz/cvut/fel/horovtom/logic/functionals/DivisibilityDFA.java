package cz.cvut.fel.horovtom.logic.functionals;

public class DivisibilityDFA {

/*
This DFA automaton depends on two integer parameters: <modulus> and <base>.
It accepts all strings which represent a non-negative integer
written in base <base> and divisible by <modulus>.
For example, if  <modulus> = 3, <base> = 10,
the automaton accepts language 0* . { 3, 6, 9, 12, 15, .... }.
Similarly, if <modulus> = 5, <base> = 2,
the automaton accepts language 0* . { 101, 1010, 1111, 10100, 11001, .... }.

For simplicity of this application, <base> is limited to 2,3, ... 10.

The states of the automaton, labeled 0, 1, ..., <modulus>-1,
represent the congruence classes modulo <modulus>.
The transition function T is loosely defined as
T( state, digit ) = ( <base>*state + digit ) % <modulus>,
where digit is the input character and its value is in { 0, 1, ... <base>-1 }.

The start state and the only final state is always 0.

some easy references:
http://www.exstrom.com/blog/abrazolica/posts/divautomata.html
https://www.geeksforgeeks.org/dfa-based-division/

*/

    // ---------------------------------------------------------------------------
    //     A U T O M A T O N
    // ---------------------------------------------------------------------------

    private int modulus, base;
    private int[][] T;

    // constructor
    public DivisibilityDFA(int mod, int bas) {
        base = bas;
        modulus = mod;
        // create and fill the transition table
        T = new int[modulus][base];
        for (int state = 0; state < modulus; state++)
            for (int letter = 0; letter < base; letter++)
                T[state][letter] = (base * state + letter) % modulus;
    }

    boolean process(String s) {
        int state = 0; // start
        for (int i = 0; i < s.length(); i++)
            state = T[state][(int) (s.charAt(i)) - 48];
        return state == 0;
    }

    // ---------------------------------------------------------------------------
    //     I / O
    // ---------------------------------------------------------------------------
    // the tedious job of printing nicely...  abrakadabra...

    private void line(String leadStr, int state, int[] values, int cellWidth, char separ) {
        String format = "%" + cellWidth + "d";
        System.out.printf("%s", leadStr + separ);
        if (state == -1) System.out.printf("%s", "         ".substring(0, cellWidth) + separ + " ");
        else System.out.printf(format + separ + " ", state);
        for (int i = 0; i < values.length; i++) {
            System.out.printf(format, values[i]);
            if (i < values.length - 1) System.out.printf("%s", separ);
        }
        System.out.printf("\n");
    }

    void print() {
        char separator = ',';
        int[] alphabet = new int[base];
        for (int i = 0; i < base; i++) alphabet[i] = i;
        int cellWidth = (int) Math.log10(base - 1) + 1 + 1;
        // header
        line("  ", -1, alphabet, cellWidth, separator);
        // states
        line("<>", 0, T[0], cellWidth, separator);
        for (int i = 1; i < modulus; i++)
            line("  ", i, T[i], cellWidth, separator);
    }

    // ---------------------------------------------------------------------------
    //     S A N D B O X
    // ---------------------------------------------------------------------------

    // small utility converts decimal integer to another base
    static String decToBase(int n, int base) {
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            sb.append((new StringBuilder(String.valueOf(n % base)).reverse()));
            n /= base;
        }
        return sb.reverse().toString();
    }

    public static void main(String[] args) {
        int mod = 17;
        int base = 9;

        DivisibilityDFA dfa = new DivisibilityDFA(mod, base);
        dfa.print();

        String inbaseb;
        for (int i = 100; i < 133; i++) {
            inbaseb = decToBase(i, base);
            System.out.printf("%d %s %s\n", i, inbaseb, dfa.process(inbaseb));
        }
    }
}