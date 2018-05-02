package cz.cvut.fel.horovtom.logic.converters;

import java.util.logging.Logger;

public class Regex {
    private static final Logger LOGGER = Logger.getLogger(Regex.class.getName());

    private final String regex;
    private Node root;

    public Regex(String s) throws IllegalArgumentException {
        regex = normalizeString(s);
        if (regex == null) {
            LOGGER.severe("String: " + s + " is not a valid regular expression!");
            throw new IllegalArgumentException("String " + s + " is not valid regular expression");
        }
        root = Node.compile(s);
    }

    public Node getTree() {
        return root.copy();
    }

    public String getRegex() {
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

    /**
     * This method will add concatenation separators to the string and complete unclosed brackets.
     * It will return the corrected string, or null if the string had incorrect bracketing
     */
    private String normalizeString(String r) {
        StringBuilder sb = new StringBuilder();
        int bracketsDepth = 0;
        char ch, lastCh = '(';
        for (int i = 0; i < r.length(); i++) {
            ch = r.charAt(i);

            if (ch == '(') bracketsDepth++;
            else if (ch == ')') bracketsDepth--;
            if (bracketsDepth < 0) {
                LOGGER.warning("Invalid regex: " + r);
                return null;
            }

            if (lastCh == '·' || ch == '·') {
                sb.append(ch);
                lastCh = ch;
                continue;
            }

            if (lastCh == '*' || lastCh == ')') {
                if (ch != ')' && ch != '+' && ch != '*')
                    sb.append("·");
            } else if (lastCh != '+' && lastCh != '(') {
                if (ch != ')' && ch != '*' && ch != '+') {
                    sb.append("·");
                }
            }

            sb.append(ch);
            lastCh = ch;
        }

        for (int i = 0; i < bracketsDepth; i++) {
            if (sb.charAt(sb.length() - 1) == '(') {
                sb.deleteCharAt(sb.length() - 1);
                if (sb.charAt(sb.length() - 1) == '·') sb.deleteCharAt(sb.length() - 1);
            } else {
                sb.append(")");
            }
        }
        return sb.toString();
    }

    //FIXME: TESTER
    public static void main(String[] args) {
        String s = "b·a(a+b*a*+aab)*a(a)";
        Regex regex = new Regex(s);
        Node tree = regex.getTree();
        System.out.println(tree);
    }
}
