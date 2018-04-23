package cz.cvut.fel.horovtom.tools;

import java.util.Arrays;
import java.util.HashMap;
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
        boolean chain = false;
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (curr >= len) {
                return new Pair<>(-1, sb.toString());
            }
            char currentChar = line.charAt(curr);
            if (currentChar == separator && !chain) {
                return new Pair<>(curr + 1 >= len ? -1 : curr + 1, sb.toString());
            } else {
                if (currentChar == '\"') {
                    chain = !chain;
                } else {
                    sb.append(currentChar);
                }
                curr++;
            }
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
}
