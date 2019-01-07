package cz.cvut.fel.horovtom.automata.logic;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This class is used for accepting words in bitwise manner, however it is not faster than simple HashTable approach,
 * so it will be unused
 */
@Deprecated
class AutomatonAcceptor {
    private static final Logger LOGGER = Logger.getLogger(AutomatonAcceptor.class.getName());
    private final long[][] transitions;
    private final long initial, accepting;
    private final int QSize, sigmaSize;
    private final long[] qMap;
    private final boolean eps;

    AutomatonAcceptor(int QSize, int sigmaSize, HashMap<Integer, HashMap<Integer, int[]>> transitions, int[] initials, int[] accepting, boolean eps) {
        this.transitions = new long[QSize][sigmaSize];
        this.eps = eps;
        int in = 0;
        for (int initial : initials) {
            in += 1 << initial;
        }
        this.initial = in;
        in = 0;
        for (int i : accepting) {
            in += 1 << i;
        }
        this.accepting = in;
        this.QSize = QSize;
        this.sigmaSize = sigmaSize;

        this.qMap = new long[QSize];
        for (int i = 0; i < QSize; i++) {
            this.qMap[i] = 1 << i;
        }


        for (int state = 0; state < QSize; state++) {
            for (int letter = 0; letter < sigmaSize; letter++) {
                int[] current = transitions.get(state).get(letter);
                int c = 0;
                for (int i : current) {
                    c += 1 << i;
                }
                this.transitions[state][letter] = c;
            }
        }
    }

    boolean accepts(int[] word) {
        long current = initial;

        for (int letter : word) {
            if (current == 0) return false;

            long cNew = current;
            if (eps) {
                if (letter == 0) {
                    LOGGER.warning("Logger accepts got word that has epsilon in it!");
                    //Ignore letter
                    continue;
                }

                long prev = -1;
                while (prev != cNew) {
                    prev = cNew;
                    for (int state = 0; state < QSize; state++) {
                        if ((cNew & qMap[state]) > 0) {
                            cNew |= transitions[state][0];
                        }
                    }
                }
                current = cNew;
            }

            cNew = 0;
            for (int state = 0; state < QSize; state++) {
                long res = current & qMap[state];
                if (res > 0) {
                    cNew |= transitions[state][letter];
                }
            }

            current = cNew;
        }

        current &= accepting;
        return current > 0;
    }
}
