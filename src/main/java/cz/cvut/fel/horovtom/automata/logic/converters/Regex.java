package cz.cvut.fel.horovtom.automata.logic.converters;

import java.util.logging.Logger;

public class Regex {
    private static final Logger LOGGER = Logger.getLogger(Regex.class.getName());

    private String string;
    private String toStringCache = null;

    public Regex(String s) {
        string = normalizeString(s.trim());
    }

    /**
     * This will return this regular expression.
     * It will return null if it was not a valid regular expression.
     */
    public String getString() {
        return string;
    }

    /**
     * @return Whether this regular expression was valid.
     */
    public boolean isValid() {
        return string != null;
    }

    /**
     * This will attempt to simplify this regular expression to a shorter (simpler) one,
     * that describes the same language as the original one.
     */
    public void simplify() {
        string = string.trim();
        StringBuilder sb = new StringBuilder();

        char lastSymb = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '·' && lastSymb == 'ε') {
                sb.deleteCharAt(sb.length() - 1);
            } else {
                sb.append(string.charAt(i));
            }
            lastSymb = string.charAt(i);
        }
        string = sb.toString();

        //TODO: THIS COULD BE IMPLEMENTED IN THE FUTURE
    }

    /**
     * Performs concatenation of regular expressions.
     * Returns (a)·(b)
     */
    public static Regex concat(Regex a, Regex b) {
        Regex regex;
        if (a == null) {
            regex = new Regex(b.getString());
        } else if (b == null) {
            regex = new Regex(a.getString());
        } else {
            StringBuilder s = new StringBuilder();
            if (a.isSingleCharacter() || a.hasTopOperator(true)) {
                s.append(a.getString());
            } else {
                s.append("(").append(a.getString()).append(")");
            }
            s.append("·");
            if (b.isSingleCharacter() || b.hasTopOperator(true)) {
                s.append(b.getString());
            } else {
                s.append("(").append(b.getString()).append(")");
            }
            regex = new Regex(s.toString());
        }
        regex.simplify();
        return regex;
    }

    /**
     * This function will search the regex for it's most top operator.
     * If the concat parameter is true, it will return true if the top operator is concatenation.
     * If the concat parameter is false, it will return true if the top operator is or.
     *
     * @param concat Whether we are searching for concatenation operator or 'or' operator.
     */
    private boolean hasTopOperator(boolean concat) {
        int brackets = 0;
        int length = string.length();
        int curr = -1;
        while (-curr <= length) {
            if (string.charAt(length + curr) == ')') {
                brackets++;
                curr--;
            } else if (string.charAt(length + curr) == '(') {
                brackets--;
                curr--;
            } else if (brackets == 0 && string.charAt(length + curr) == (concat ? '·' : '+')) {
                return true;
            } else if (brackets == 0 && string.charAt(length + curr) == (concat ? '+' : '·')) {
                return false;
            } else {
                curr--;
            }
        }
        return false;
    }

    public static Regex or(Regex a, Regex b) {
        Regex regex;
        if (a == null) {
            regex = new Regex(b.getString());
        } else if (b == null) {
            regex = new Regex(a.getString());
        } else {
            StringBuilder s = new StringBuilder();
            if (a.isSingleCharacter() || a.hasTopOperator(false)) {
                s.append(a.getString());
            } else {
                s.append("(").append(a.getString()).append(")");
            }
            s.append("+");
            if (b.isSingleCharacter() || b.hasTopOperator(false)) {
                s.append(b.getString());
            } else {
                s.append("(").append(b.getString()).append(")");
            }
            regex = new Regex(s.toString());
        }
        regex.simplify();
        return regex;
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
            if (ch == ' ') continue;

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

    public static Regex kleene(Regex regex) {
        if (regex == null) {
            return new Regex("ε");
        }
        if (regex.isSingleCharacter()) {
            return new Regex(regex.getString() + "*");
        }
        Regex r = new Regex("(" + regex.getString() + ")*");
        r.simplify();
        return r;
    }

    public boolean isSingleCharacter() {
        return string.length() == 1 || (string.length() == 2 && string.charAt(1) == '*');
    }

    @Override
    public String toString() {
        if (toStringCache == null) {
            buildToStringCache();
        }
        return toStringCache;
    }

    private void buildToStringCache() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) != '·') sb.append(string.charAt(i));
        }
        toStringCache = sb.toString();
    }
}
