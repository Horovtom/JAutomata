package cz.cvut.fel.horovtom.automata.logic.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

public class RegexTree {
    private static final Logger LOGGER = Logger.getLogger(RegexTree.class.getName());

    private final Regex regex;
    private final char[] sigma;
    private Node root;

    public RegexTree(String s) throws IllegalArgumentException {
        regex = new Regex(s);

        if (!regex.isValid()) {
            LOGGER.severe("String: " + s + " is not a valid regular expression!");
            throw new IllegalArgumentException("String " + s + " is not valid regular expression");
        }


        root = Node.compile(regex.getString());
        HashSet<Character> tmp = new HashSet<>();
        for (char c : root.getLetterIndices()) {
            if (c != 'ε')
                tmp.add(c);
        }
        sigma = new char[tmp.size()];
        int curr = 0;
        for (Character character : tmp) {
            sigma[curr++] = character;
        }
    }

    public boolean isNullable() {
        return root.isNullable();
    }

    public char[] getLetterIndices() {
        return root.getLetterIndices();
    }

    public int[] getStartingIndices() {
        return root.getStartingIndices();
    }

    public int[] getEndingIndices() {
        return root.getEndingIndices();
    }

    public ArrayList<int[]> getFollowers() {
        return root.getFollowers();
    }

    public char[] getSigma() {
        return Arrays.copyOf(sigma, sigma.length);
    }

    public Node getTree() {
        return root.copy();
    }


    public Regex getRegex() {
        return regex;
    }

    /**
     * This method will check string for uneven bracketing.
     *
     * @return whether the bracketing is right
     */
    @Deprecated
    private static boolean checkBrackets(String s) {
        int currLevel = 0;
        char ch;
        for (int i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            if (ch == '(') {
                currLevel++;
            } else if (ch == ')') {
                if (currLevel <= 0) return false;
                currLevel--;
            }
        }

        return currLevel == 0;
    }


    //FIXME: TESTER
    public static void main(String[] args) {
        String s = "b·a(a+b*a*+aab)*a(a)";
        RegexTree regex = new RegexTree(s);
        Node tree = regex.getTree();
        System.out.println(tree);
    }
}
