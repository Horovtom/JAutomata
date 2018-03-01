package cz.cvut.fel.horovtom.tools;

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

}
