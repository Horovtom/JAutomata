package cz.cvut.fel.horovtom.automata.logic.converters;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.NFAAutomaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * This class is used for converting regular expressions to automatons
 */
public class FromRegexConverter {
    private static final Logger LOGGER = Logger.getLogger(FromRegexConverter.class.getName());

    /**
     * This function will convert regular expression to automaton.
     * Examples of regular expressions:
     * ε+(a+b)(a+b)((a+b)(a+b)(a+b))*(a+b)
     * (a+eps)*b
     *
     * @param r Regular expression
     * @return Automaton that accepts the language described by regular expression
     */
    public static Automaton getAutomaton(String r) {
        LOGGER.fine("Trying to convert regex: " + r + " to automaton!");
        r = r.replaceAll("eps", "ε");
        RegexTree regex = new RegexTree(r);
        char[] sigma = regex.getSigma();
        char[] letterIndices = regex.getLetterIndices();
        int[] initials = regex.getStartingIndices();
        int[] accepting = regex.getEndingIndices();
        ArrayList<int[]> followers = regex.getFollowers();

        return convertToAutomaton(sigma, letterIndices, initials, accepting, followers, regex.isNullable());
    }

    private static Automaton convertToAutomaton(char[] sigma, char[] letterIndices, int[] initials, int[] accepting, ArrayList<int[]> followers, boolean nullable) {
        if (sigma == null || sigma.length == 0) {
            LOGGER.fine("User input empty regular expression.");
            try {
                return AutomatonSamples.DFASamples.emptyAutomaton();
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                LOGGER.warning("Did not find empty automaton!");
            }
        }
        ArrayList<String> Q = new ArrayList<>();

        HashMap<Integer, Integer> sigmaMapIntToInt = new HashMap<>(letterIndices.length);
        for (int i = 0; i < letterIndices.length; i++) {
            for (int i1 = 0; i1 < sigma.length; i1++) {
                if (sigma[i1] == letterIndices[i]) {
                    sigmaMapIntToInt.put(i, i1);
                }
            }
        }

        //Construct Q
        for (int i = 0; i < letterIndices.length; i++) {
            Q.add(String.valueOf(letterIndices[i]) + i);
        }

        Q.add("I");
        HashMap<Integer, HashMap<Integer, int[]>> transitions = new HashMap<>();

        //Initial state
        HashMap<Integer, int[]> curr = new HashMap<>();
        transitions.put(Q.size() - 1, curr);
        for (int i = 0; i < sigma.length; i++) {
            HashSet<Integer> targs = new HashSet<>();
            for (int initial : initials) {
                if (sigmaMapIntToInt.get(initial) == i) targs.add(initial);
            }


            curr.put(i, targs.stream().mapToInt(a -> a).toArray());
        }

        //Normal transitions
        for (int i = 0; i < letterIndices.length; i++) {
            curr = new HashMap<>();
            transitions.put(i, curr);

            for (int letter = 0; letter < sigma.length; letter++) {
                HashSet<Integer> targs = new HashSet<>();

                for (int follower : followers.get(i)) {
                    if (sigmaMapIntToInt.get(follower) == letter) targs.add(follower);
                }

                curr.put(letter, targs.stream().mapToInt(a -> a).toArray());
            }
        }

        String[] newSigma = new String[sigma.length];
        Arrays.setAll(newSigma, i -> String.valueOf(sigma[i]));

        if (nullable) {
            int[] newAccepting = new int[accepting.length + 1];
            System.arraycopy(accepting, 0, newAccepting, 0, accepting.length);
            newAccepting[accepting.length] = Q.size() - 1;
            accepting = newAccepting;
        }

        return new NFAAutomaton(Q.toArray(new String[]{}), newSigma, transitions, new int[]{Q.size() - 1}, accepting);
    }

    public static void main(String[] args) {
        Automaton a = FromRegexConverter.getAutomaton("ε+(a+b)(a+b)((a+b)(a+b)(a+b))*(a+b)");
        DFAAutomaton r = a.getReduced();
        System.out.println(r);
    }
}
