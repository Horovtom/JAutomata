package cz.cvut.fel.horovtom.jasl.graphviz;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class DotToTex {
    private static final Logger LOGGER = Logger.getLogger(DotToTex.class.getName());

    private class State {
        String id, label;
        int x, y;
        boolean accepting;
        boolean initial;
    }

    private final ArrayList<ArrayList<String>> commandTokens;
    private final String dotCode;
    private final State[] states;

    private DotToTex(String s) {
        this.dotCode = s;
        this.commandTokens = parseToCommands();
        this.states = getStates();
    }

    public static String convert(String s) {
        // Initialize this class
        // return result.

        return null;
    }

    /**
     * This function will try to parse string to command tokens ArrayLists.
     *
     * @return ArrayList of commands that consist of ArrayList of tokens of those commands.
     */
    private ArrayList<ArrayList<String>> parseToCommands() {

        ArrayList<String> commands = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(this.dotCode, ";");
        while (st.hasMoreTokens()) {
            commands.add(st.nextToken().trim());
        }

        ArrayList<ArrayList<String>> commandWithTokens = new ArrayList<>();

        for (String command : commands) {
            ArrayList<String> commandTokens = new ArrayList<>();
            st = new StringTokenizer(command);

            outer:
            while (st.hasMoreTokens()) {
                boolean appendClose = false;
                String token = st.nextToken().trim();

                if (token.charAt(0) == '[') {
                    token = token.substring(1, token.length());
                    commandTokens.add("[");
                }

                while (true) {
                    if (token.length() == 0) continue outer;
                    if (token.charAt(token.length() - 1) == ',' || token.charAt(token.length() - 1) == '\"') {
                        token = token.substring(0, token.length() - 1);
                    } else if (token.charAt(token.length() - 1) == ']') {
                        token = token.substring(0, token.length() - 1);
                        appendClose = true;
                    } else {
                        break;
                    }
                }


                commandTokens.add(token);
                if (appendClose) {
                    commandTokens.add("]");
                }
            }
            commandWithTokens.add(commandTokens);
        }

        return commandWithTokens;
    }

    private State[] getStates() {
        ArrayList<Integer> initialStates = new ArrayList<>();
        ArrayList<Integer> acceptingStates = new ArrayList<>();
        ArrayList<State> states = new ArrayList<>();

        if (!this.commandTokens.get(0).get(0).equals("digraph") ||
                !this.commandTokens.get(1).get(0).equals("node")) {
            LOGGER.severe("Unknown DOT format!");
        }

        for (int i = 2; i < this.commandTokens.size(); i++) {
            ArrayList<String> currCommand = this.commandTokens.get(i);
            // State name

            // Is it a state and not a transition?
            if (!currCommand.get(1).equals("->")) {
                // Create state and add it to states
                State s = new State();
                s.id = currCommand.get(0);

                int found = 0;
                // Iterate over fields:
                int numTokens = currCommand.size();
                for (int i1 = 2; i1 < numTokens; i1++) {
                    String field = currCommand.get(i1);
                    if (field.contains("label=")) {
                        s.label = field.substring(6, field.length());
                        found |= 1;
                    } else if (field.contains("pos=")) {
                        String[] split = field.substring(5, field.length()).split(",");
                        if (split.length != 2) {
                            LOGGER.severe("Invalid positional field: " + field);
                        } else {
                            s.x = (int) Double.parseDouble(split[0]);
                            s.y = (int) Double.parseDouble(split[1]);
                        }
                        found |= 2;
                    } else if (field.contains("shape=")) {
                        if (field.substring(6, field.length()).equals("none")) {
                            // We found a pointer to initial state!
                            // TODO: CONTINUE
                            // Maybe screw this and do it other way... Get automaton object as well and then try to get info about states from the automaton, while extracting relevant positional data from the DOT code.
                        }
                    }
                }

            }
        }

        return new State[0];
    }

//    public static void main(String[] args) {
//        ArrayList<ArrayList<String>> commands = parseToCommands("digraph automaton {\n" +
//                "\tgraph [bb=\"0,0,432,302\",\n" +
//                "\t\trankdir=LR,\n" +
//                "\t\tsize=\"8,5\"\n" +
//                "\t];\n" +
//                "\tnode [label=\"\\N\",\n" +
//                "\t\tshape=circle\n" +
//                "\t];\n" +
//                "\tqS0\t [height=0.5,\n" +
//                "\t\tlabel=\"\",\n" +
//                "\t\tpos=\"27,143\",\n" +
//                "\t\tshape=none,\n" +
//                "\t\twidth=0.75];\n" +
//                "\t0\t [height=0.5,\n" +
//                "\t\tpos=\"118,143\",\n" +
//                "\t\twidth=0.5];\n" +
//                "\tqS0 -> 0\t [color=\"red:invis:red\",\n" +
//                "\t\tpos=\"e,99.945,143 54.222,143 65.412,143 78.47,143 89.769,143\"];\n" +
//                "\t1\t [height=0.5,\n" +
//                "\t\tpos=\"214,18\",\n" +
//                "\t\twidth=0.5];\n" +
//                "\t0 -> 1\t [label=a,\n" +
//                "\t\tlp=\"166.5,75.5\",\n" +
//                "\t\tpos=\"e,199.74,29.513 126.84,127.04 134.9,111.32 148.32,86.992 163,68 171.68,56.769 182.72,45.483 192.22,36.484\"];\n" +
//                "\t2\t [height=0.72222,\n" +
//                "\t\tpos=\"406,120\",\n" +
//                "\t\tshape=doublecircle,\n" +
//                "\t\twidth=0.72222];\n" +
//                "\t0 -> 2\t [label=b,\n" +
//                "\t\tlp=\"261.5,185.5\",\n" +
//                "\t\tpos=\"e,387.22,138.06 135.05,149.62 171.78,163.77 264.35,193.84 337,170 352.61,164.88 367.59,154.65 379.5,144.77\"];\n" +
//                "\t3\t [height=0.72222,\n" +
//                "\t\tpos=\"214,243\",\n" +
//                "\t\tshape=doublecircle,\n" +
//                "\t\twidth=0.72222];\n" +
//                "\t0 -> 3\t [label=b,\n" +
//                "\t\tlp=\"166.5,216.5\",\n" +
//                "\t\tpos=\"e,192.01,228.18 128.32,157.9 136.62,170.58 149.5,188.92 163,203 169.3,209.57 176.69,216.04 183.83,221.79\"];\n" +
//                "\tqS1\t [height=0.5,\n" +
//                "\t\tlabel=\"\",\n" +
//                "\t\tpos=\"118,18\",\n" +
//                "\t\tshape=none,\n" +
//                "\t\twidth=0.75];\n" +
//                "\tqS1 -> 1\t [color=\"red:invis:red\",\n" +
//                "\t\tpos=\"e,195.85,18 145.18,18 157.83,18 173,18 185.78,18\"];\n" +
//                "\t1 -> 1\t [label=b,\n" +
//                "\t\tlp=\"214,61.5\",\n" +
//                "\t\tpos=\"e,222.02,34.29 205.98,34.29 204.05,44.389 206.72,54 214,54 218.66,54 221.43,50.056 222.32,44.566\"];\n" +
//                "\t4\t [height=0.5,\n" +
//                "\t\tpos=\"310,18\",\n" +
//                "\t\twidth=0.5];\n" +
//                "\t1 -> 4\t [label=b,\n" +
//                "\t\tlp=\"261.5,25.5\",\n" +
//                "\t\tpos=\"e,291.87,18 232.24,18 246.02,18 265.6,18 281.53,18\"];\n" +
//                "\tqS2\t [height=0.5,\n" +
//                "\t\tlabel=\"\",\n" +
//                "\t\tpos=\"310,143\",\n" +
//                "\t\tshape=none,\n" +
//                "\t\twidth=0.75];\n" +
//                "\tqS2 -> 2\t [color=\"red:invis:red\",\n" +
//                "\t\tpos=\"e,380.57,125.98 337.18,136.59 347.58,134.05 359.68,131.09 370.76,128.38\"];\n" +
//                "\t2 -> 0\t [label=b,\n" +
//                "\t\tlp=\"261.5,126.5\",\n" +
//                "\t\tpos=\"e,135.65,139.33 379.83,118.17 367.01,117.32 351.19,116.42 337,116 313.01,115.3 306.92,114.04 283,116 234.16,119.99 177.96,130.57 \\\n" +
//                "145.65,137.24\"];\n" +
//                "\t3 -> 3\t [label=\"a,b\",\n" +
//                "\t\tlp=\"214,294.5\",\n" +
//                "\t\tpos=\"e,222.55,267.88 205.45,267.88 204.86,278.23 207.71,287 214,287 218.03,287 220.65,283.4 221.85,278.09\"];\n" +
//                "\t4 -> 2\t [label=b,\n" +
//                "\t\tlp=\"358.5,75.5\",\n" +
//                "\t\tpos=\"e,389.14,99.833 323.94,29.973 334.48,39.887 349.58,54.487 362,68 368.87,75.475 376.01,83.848 382.46,91.654\"];\n" +
//                "\t4 -> 4\t [label=a,\n" +
//                "\t\tlp=\"310,61.5\",\n" +
//                "\t\tpos=\"e,318.36,34.29 301.64,34.29 299.62,44.389 302.41,54 310,54 314.86,54 317.76,50.056 318.68,44.566\"];\n" +
//                "}");
//
//        for (ArrayList<String> command : commands) {
//            System.out.println("Next command: =======");
//            for (String s : command) {
//                System.out.println(s);
//            }
//        }
//
//    }


    /*
    Next command: =======
digraph
automaton
{
graph
[
bb="0,0,432,302
rankdir=LR
size="8,5
Next command: =======
node
[
label="\N
shape=circle
Next command: =======
qS0
[
height=0.5
label=
pos="27,143
shape=none
width=0.75
]
Next command: =======
0
[
height=0.5
pos="118,143
width=0.5
]
Next command: =======
qS0
->
0
[
color="red:invis:red
pos="e,99.945,143
54.222,143
65.412,143
78.47,143
89.769,143
]
Next command: =======
1
[
height=0.5
pos="214,18
width=0.5
]
Next command: =======
0
->
1
[
label=a
lp="166.5,75.5
pos="e,199.74,29.513
126.84,127.04
134.9,111.32
148.32,86.992
163,68
171.68,56.769
182.72,45.483
192.22,36.484
]
Next command: =======
2
[
height=0.72222
pos="406,120
shape=doublecircle
width=0.72222
]
Next command: =======
0
->
2
[
label=b
lp="261.5,185.5
pos="e,387.22,138.06
135.05,149.62
171.78,163.77
264.35,193.84
337,170
352.61,164.88
367.59,154.65
379.5,144.77
]
Next command: =======
3
[
height=0.72222
pos="214,243
shape=doublecircle
width=0.72222
]
Next command: =======
0
->
3
[
label=b
lp="166.5,216.5
pos="e,192.01,228.18
128.32,157.9
136.62,170.58
149.5,188.92
163,203
169.3,209.57
176.69,216.04
183.83,221.79
]
Next command: =======
qS1
[
height=0.5
label=
pos="118,18
shape=none
width=0.75
]
Next command: =======
qS1
->
1
[
color="red:invis:red
pos="e,195.85,18
145.18,18
157.83,18
173,18
185.78,18
]
Next command: =======
1
->
1
[
label=b
lp="214,61.5
pos="e,222.02,34.29
205.98,34.29
204.05,44.389
206.72,54
214,54
218.66,54
221.43,50.056
222.32,44.566
]
Next command: =======
4
[
height=0.5
pos="310,18
width=0.5
]
Next command: =======
1
->
4
[
label=b
lp="261.5,25.5
pos="e,291.87,18
232.24,18
246.02,18
265.6,18
281.53,18
]
Next command: =======
qS2
[
height=0.5
label=
pos="310,143
shape=none
width=0.75
]
Next command: =======
qS2
->
2
[
color="red:invis:red
pos="e,380.57,125.98
337.18,136.59
347.58,134.05
359.68,131.09
370.76,128.38
]
Next command: =======
2
->
0
[
label=b
lp="261.5,126.5
pos="e,135.65,139.33
379.83,118.17
367.01,117.32
351.19,116.42
337,116
313.01,115.3
306.92,114.04
283,116
234.16,119.99
177.96,130.57
\
145.65,137.24
]
Next command: =======
3
->
3
[
label="a,b
lp="214,294.5
pos="e,222.55,267.88
205.45,267.88
204.86,278.23
207.71,287
214,287
218.03,287
220.65,283.4
221.85,278.09
]
Next command: =======
4
->
2
[
label=b
lp="358.5,75.5
pos="e,389.14,99.833
323.94,29.973
334.48,39.887
349.58,54.487
362,68
368.87,75.475
376.01,83.848
382.46,91.654
]
Next command: =======
4
->
4
[
label=a
lp="310,61.5
pos="e,318.36,34.29
301.64,34.29
299.62,44.389
302.41,54
310,54
314.86,54
317.76,50.056
318.68,44.566
]
Next command: =======
}
     */
}
