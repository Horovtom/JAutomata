package cz.cvut.fel.horovtom.automata.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

public class Utilities {
    /**
     * Returns a random number between upper and upper
     *
     * @param lower
     * @param upper
     */
    public static int getRandInt(int lower, int upper) {
        return ThreadLocalRandom.current().nextInt(lower, upper + 1);
    }

    /**
     * Returns a random number between 0 and 1
     */
    public static double getRandDouble() {
        return Math.random();
    }

    /**
     * Returns pair of NextPosition and String with the next token on the line from specified position
     * E.G.:
     * getNextToken("Hello,World,Trebuchet", 0, ',') -> Pair(6,"Hello")
     * getNextToken("Hello,World,Trebuchet", 6, ',') -> Pair(12, "World")
     * getNextToken("Hello,World,Trebuchet", 12, ',')-> Pair(-1, "Trebuchet)
     * getNextToken("Hello,World,Trebuchet", -1, ',')-> Pair(-1, null)
     */
    public static Pair<Integer, String> getNextToken(String line, int pos, char separator) {
        int len = line.length();
        if (pos < 0 || pos >= len) return new Pair<>(-1, "");
        int curr = pos;
        boolean chain = false, hadBeenChained = false;
        StringBuilder sb = new StringBuilder();

        //Get next unchained comma:
        while (true) {
            if (curr >= len) {
                return new Pair<>(-1, sb.toString());
            }
            char currentChar = line.charAt(curr++);
            if (currentChar == separator && !chain) {
                break;
            } else if (currentChar == '\"') {
                chain = !chain;
                hadBeenChained = true;
                sb.append(currentChar);
            } else {
                sb.append(currentChar);
            }
        }

        int retIndex = curr >= len ? -1 : curr;

        String trim = sb.toString().trim();
        if (hadBeenChained) {
            return new Pair<>(retIndex, trim.substring(1, trim.length() - 1));
        } else {
            return new Pair<>(retIndex, trim);
        }
    }

    public static HashMap<Integer, HashMap<Integer, int[]>> getCopyOfHashMap(HashMap<Integer, HashMap<Integer, int[]>> transitions) {
        HashMap<Integer, HashMap<Integer, int[]>> copyOfTransitions = new HashMap<>();
        for (Integer integer : transitions.keySet()) {
            HashMap<Integer, int[]> current = transitions.get(integer);
            HashMap<Integer, int[]> currentRow = new HashMap<>();
            for (Integer integer1 : current.keySet()) {
                int[] ints = current.get(integer1);
                currentRow.put(integer1, Arrays.copyOf(ints, ints.length));
            }
            copyOfTransitions.put(integer, currentRow);
        }
        return copyOfTransitions;
    }

    /**
     * Returns an array of elements, parsed from comma-separated list of elements
     */
    public static String[] getArrFromCommaSepList(String list) {
        StringTokenizer st = new StringTokenizer(list, ",");
        String[] ret = new String[st.countTokens()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = st.nextToken();
        }
        return ret;
    }
}
