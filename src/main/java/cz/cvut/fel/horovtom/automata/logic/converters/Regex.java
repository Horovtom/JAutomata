package cz.cvut.fel.horovtom.automata.logic.converters;

import java.util.logging.Logger;

public class Regex {
    private static final Logger LOGGER = Logger.getLogger(Regex.class.getName());

    private String string;
    private String toStringCache = null;

    public Regex(String s) {
        string = normalizeString(s.trim());
    }

    public String getString() {
        return string;
    }

    public boolean isValid() {
        return string != null;
    }

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

        //TODO: IMPLEMENT

    }

    public static Regex concat(Regex a, Regex b) {
        Regex regex;
        if (a == null) {
            regex = new Regex(b.getString());
        } else if (b == null) {
            regex = new Regex(a.getString());
        } else {
            StringBuilder s = new StringBuilder();
            if (a.isSingleCharacter() || a.hasTop(true)) {
                s.append(a.getString());
            } else {
                s.append("(").append(a.getString()).append(")");
            }
            s.append("·");
            if (b.isSingleCharacter() || b.hasTop(true)) {
                s.append(b.getString());
            } else {
                s.append("(").append(b.getString()).append(")");
            }
            regex = new Regex(s.toString());
        }
        regex.simplify();
        return regex;
    }

    private boolean hasTop(boolean concat) {
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
            if (a.isSingleCharacter() || a.hasTop(false)) {
                s.append(a.getString());
            } else {
                s.append("(").append(a.getString()).append(")");
            }
            s.append("+");
            if (b.isSingleCharacter() || b.hasTop(false)) {
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
