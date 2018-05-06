package cz.cvut.fel.horovtom.automata.logic.functionals;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.converters.FromRegexConverter;

import java.util.HashMap;

/**
 * This class is solely used for creating functional automatons
 */
public class FunctionalCreator {
    private FunctionalCreator() {
    }

    /**
     * <p>
     * This DFA automaton depends on two integer parameters: [modulus] and [base].
     * It accepts all strings which represent a non-negative integer
     * written in base [base] and divisible by [modulus].
     * For example, if  [modulus] = 3, [base] = 10,
     * the automaton accepts language 0* . { 3, 6, 9, 12, 15, .... }.
     * Similarly, if [modulus] = 5, [base] = 2,
     * the automaton accepts language 0* . { 101, 1010, 1111, 10100, 11001, .... }.
     * <p>
     * For simplicity of this application, [base] is limited to 2,3, ... 10.
     * </p>
     * The states of the automaton, labeled 0, 1, ..., [modulus]-1,
     * represent the congruence classes modulo [modulus].
     * The transition function T is loosely defined as
     * T( state, digit ) = ( [base]*state + digit ) % [modulus],
     * where digit is the input character and its value is in { 0, 1, ... [base]-1 }.
     * </p>
     * <p>
     * The start state and the only final state is always 0.
     * </p>
     * <p>
     * some easy references:<br/>
     * <a href="http://www.exstrom.com/blog/abrazolica/posts/divautomata.html">exstrom.com</a><br/>
     * <a href="https://www.geeksforgeeks.org/dfa-based-division/">geeksforgeeks.org</a>
     * </p>
     */
    public static Automaton getDivisibilityAutomaton(int mod, int base) {
        String[] sigma = new String[base];
        for (int i = 0; i < base; i++) {
            if (i < 10)
                sigma[i] = String.valueOf((char) (48 + i));
            else if (i < 36)
                sigma[i] = String.valueOf((char) (65 + (i - 10)));
            else
                sigma[i] = String.valueOf((char) (97 + (i - 36)));
        }

        String[] Q = new String[mod];
        for (int i = 0; i < mod; i++) {
            Q[i] = String.valueOf(i);
        }

        HashMap<Integer, HashMap<Integer, Integer>> transition = new HashMap<>();
        for (int state = 0; state < Q.length; state++) {
            HashMap<Integer, Integer> current = new HashMap<>();
            for (int letter = 0; letter < sigma.length; letter++) {
                current.put(letter, (base * state + letter) % mod);
            }
            transition.put(state, current);
        }

        int initial = 0;
        int[] accepting = new int[]{0};

        return new DFAAutomaton(Q, sigma, transition, initial, accepting);
    }

    /**
     * This function will output a length divisibility checker automaton M.
     * This automaton will measure input string length and output if it is divisible by a specified mod.
     *
     * @param sigma Letters of Sigma that are accepted by automaton M.
     * @param mod   The input word has to be divisible by length, in order to be accepted by automaton M
     */
    public static Automaton getLengthDivisibilityAutomaton(char[] sigma, int mod) {
        //FIXME: Maybe rewrite without regex in order to generify even to String type letters
        StringBuilder sb = new StringBuilder("(");
        StringBuilder unit = new StringBuilder();
        unit.append(sigma[0]);
        for (int i = 1; i < sigma.length; i++) {
            unit.append("+").append(sigma[i]);
        }

        for (int i = 0; i < mod; i++) {
            sb.append("(").append(unit).append(")");
        }
        sb.append(")*");
        return FromRegexConverter.getAutomaton(sb.toString()).getReduced();
    }

}
