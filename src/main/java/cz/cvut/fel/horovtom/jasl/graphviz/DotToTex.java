package cz.cvut.fel.horovtom.jasl.graphviz;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.samples.AutomatonSamples;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

class DotToTex {
    private static final Logger LOGGER = Logger.getLogger(DotToTex.class.getName());

    private final String result;

    private enum Direction {
        N, W, S, E
    }

    private static class Coordinate {
        double x;
        double y;

        Coordinate(double x, double y) {
            this.x = x;
            this.y = y;
        }

        double magnitude() {
            return Math.sqrt(x * x + y * y);
        }

        void mult(double scalar) {
            x *= scalar;
            y *= scalar;
        }

        @Override
        public String toString() {
            return "Coordinate{" + "x=" + x +
                    ", y=" + y +
                    '}';
        }

        static Coordinate subtract(Coordinate a, Coordinate b) {
            return new Coordinate(a.x - b.x, a.y - b.y);
        }

        static Coordinate add(Coordinate a, Coordinate b) {
            return new Coordinate(a.x - b.x, a.y - b.y);
        }

        static Coordinate mult(Coordinate a, double scalar) {
            return new Coordinate(a.x * scalar, a.y * scalar);
        }

        static Coordinate mult(Coordinate a, Coordinate b) {
            return new Coordinate(a.x * b.x, a.y * b.y);
        }

        public static double dot(Coordinate a, Coordinate b) {
            return a.x * b.x + a.y * b.y;
        }
    }


    // region BOUNDING BOX OF TIKZ IMAGES

    /**
     * Bottom left corner of tikz image
     */
    private final Coordinate tikzMin = new Coordinate(0, 0);
    /**
     * Top right corner of tikz image
     */
    private final Coordinate tikzMax = new Coordinate(11, 14);

    // endregion

    /**
     * This will convert dot code to tikz code. It needs to have reference of the source automaton.
     * @param automaton Automaton reference
     * @param dotCode Dot code to display the automaton
     * @param fixed Whether we want the result tikz code in fixed coordinates or not
     */
    private DotToTex(Automaton automaton, String dotCode, boolean fixed) throws IOException {
        if (fixed)
            result = convertToTexCoordinates(automaton, dotCode);
        else
            result = convertToTexRelative(automaton, dotCode);
    }

    /**
     * This function will attempt to convert automaton to tikz code with relative coordinates
     * @param automaton Automaton
     * @param dotCode Dot string generated for specified automaton
     * @return Tikz code to display this automaton
     */
    private String convertToTexRelative(Automaton automaton, String dotCode) {
        //TODO: IMPLEMENT
        return null;
    }

    /**
     * This will convert automaton to tikz code. If fixed is true, it will generate tikz code with fixed coordinates.
     * It will generate tikz code with relative coordinates otherwise.
     * @param automaton Reference to the automaton
     * @param dotCode Dot code to display the automaton
     * @param fixed If true, the result will use fixed coordinates.
     * @return TIKZ code to display the automaton
     */
    public static String convert(Automaton automaton, String dotCode, boolean fixed) throws IOException {
        // Initialize this class
        DotToTex c = new DotToTex(automaton, dotCode, fixed);
        // return result.
        return c.getResult();
    }

    public String getResult() {
        return result;
    }


    public static void main(String[] args) throws IOException {
//        Automaton a = FromRegexConverter.getAutomaton("a*ba");
//        Automaton a = FromRegexConverter.getAutomaton("a*(b+baa)*a");
//        Automaton a = FromRegexConverter.getAutomaton("(a+b)a*(b+ba*a)a");
        Automaton a = AutomatonSamples.DFASamples.lolipop();

        a = a.getReduced();

        System.out.println(GraphvizAPI.toDot(a));
        String s = GraphvizAPI.toFormattedDot(a);
        System.out.println(s);
        GraphvizAPI.dotToPNG(a, "test.png", "test2.png", "test3.png");

        DotToTex dtt = new DotToTex(a, s, true);
        String b = dtt.getResult();

        System.out.println(b);
    }

    /**
     * This function will attempt to convert automaton to tikz code with fixed coordinates
     * @param automaton Automaton
     * @param dotCode Dot string generated for specified automaton
     * @return Tikz code to display this automaton
     */
    private String convertToTexCoordinates(Automaton automaton, String dotCode) throws IOException {
        final int qSize = automaton.getQSize();
        HashMap<String, Integer> inverseQ = getInverseQ(automaton);


        // This holds the graph parsed from the dot file.
        MutableGraph mg = Parser.read(dotCode);
        // Form our parsed dot file we will need the bounding box:
        String[] bb = ((String) mg.graphAttrs().get("bb")).split(",");
        // And the description for all the nodes
        MutableNode[] nodes = mg.nodes().toArray(new MutableNode[0]);
        // We will hold mapping to nodes indices to those nodes for later use
        HashMap<Integer, MutableNode> nodesMap = new HashMap<>();
        // And we will hold node coordinates in this array, indexed by Q
        Coordinate[] coordinates;

        // We want to get edges attributes from those nodes.
        HashMap<Integer, HashMap<Integer, ArrayList<Coordinate>>> edgesAttributes = new HashMap<>();

        getEdgesAttributes(edgesAttributes, nodesMap, nodes, inverseQ);

        coordinates = getNodeCoordinates(nodesMap, qSize);

        // Get real bounding box of the image
        Coordinate min = new Coordinate(Double.MAX_VALUE, Double.MAX_VALUE);
        Coordinate max = new Coordinate(Double.MIN_VALUE, Double.MIN_VALUE);
        for (Coordinate coordinate : coordinates) {
            if (coordinate.x < min.x) min.x = coordinate.x;
            if (coordinate.y < min.y) min.y = coordinate.y;
            if (coordinate.x > max.x) max.x = coordinate.x;
            if (coordinate.y > max.y) max.y = coordinate.y;
        }

        // This contains tikz coordinates of the nodes.
        Coordinate[] newCoordinates = getScaledCoordinates(coordinates, min, max);

        // Now try to assemble the output
        StringBuilder sb = new StringBuilder("\\begin{tikzpicture}[->,>=stealth',shorten >=1pt,auto,node distance=2.8cm,semithick,initial text=$ \t$]\n" +
                "\t\\tikzset{every state/.style={minimum size=0pt}}\n");

        addNodesToSb(sb, automaton, newCoordinates);

        // Edges:
        sb.append("\t\\path");


        for (int i = 0; i < qSize; i++) {
            HashMap<Integer, String> curr = getEdgesFromState(automaton, i);
            if (curr.size() == 0) continue;
            sb.append("\n\t\t(").append(i).append(")\n");
            Coordinate source = coordinates[i];

            for (Integer target : curr.keySet()) {
                String edgeLabel = curr.get(target);
                sb.append("\n\t\t\tedge ");

                ArrayList<Coordinate> edgeAttrs = edgesAttributes.get(i).get(target);

                String edgeProperties;

                if (target == i) {
                    // It is a loop...
                    Coordinate middlePoint = edgeAttrs.get((int) Math.floor(edgeAttrs.size() / 2.0));

                    String loopDirection = getLoopDirection(source, middlePoint);
                    edgeProperties = "[loop " + loopDirection + "]";
                } else {
                    // Now... How does it curve?

                    int angle = getCurveAngle(source, coordinates[target], edgeAttrs);
                    if (angle < 0) {
                        // We have bend right
                        edgeProperties = "[bend right = " + Math.abs(angle) + "]";
                    } else if (angle > 0) {
                        // We have bend left
                        edgeProperties = "[bend left = " + Math.abs(angle) + "]";
                    } else {
                        // We have no bend
                        edgeProperties = "";
                    }


                }

                // Square bracket for the edge...
                sb.append(edgeProperties).append(" ");

                sb.append("node ");
                // Now a label
                sb.append("{$").append(edgeLabel).append("$} ");

                // And a target
                sb.append("(").append(target).append(")");
            }

        }

        // Now end our misery
        sb.append(";\n\\end{tikzpicture}\n");

        return sb.toString();
    }

    private Coordinate[] getScaledCoordinates(Coordinate[] coordinates, Coordinate min, Coordinate max) {
        // We use scaling that will preserve aspect ratio of the image.

        Coordinate size = Coordinate.subtract(max, min);

        // Either by X:
        double ratio = tikzMax.x / size.x;
        double yMax = size.y * ratio;
        if (yMax > tikzMax.y) {
            // By Y:
            ratio = tikzMax.y / size.y;
        }

        Coordinate[] scaled = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            scaled[i] = Coordinate.mult(coordinates[i], ratio);
        }
        return scaled;
    }

    private Coordinate[] getNodeCoordinates(HashMap<Integer, MutableNode> nodesMap, int qSize) {
        Coordinate[] coordinates = new Coordinate[qSize];
        for (int i = 0; i < qSize; i++) {
            final MutableNode node = nodesMap.get(i);
            String pos = (String) node.attrs().get("pos");
            String[] posSplit = pos.split(",");
            Coordinate position = new Coordinate(Double.parseDouble(posSplit[0]), Double.parseDouble(posSplit[1]));
            coordinates[i] = position;
        }

        return coordinates;
    }

    private void addNodesToSb(StringBuilder sb,
                              Automaton automaton,
                              Coordinate[] coordinates) {

        final int[] initialStates = automaton.getInitialStates();
        final int[] acceptingStates = automaton.getAcceptingStates();
        final String[] Q = automaton.getQ();
        final int qSize = Q.length;

        for (int i = 0; i < qSize; i++) {
            sb.append("\t\\node[state");
            for (int initialState : initialStates) {
                if (initialState == i) {
                    sb.append(", initial, initial where=left");

                    break;
                }
            }

            for (int acceptingState : acceptingStates) {
                if (acceptingState == i) {
                    sb.append(", accepting");
                }
            }

            sb.append("] (").append(i).append(") at (");
            Coordinate pos = coordinates[i];

            sb.append(String.format(Locale.US, "%.2f", pos.x)).append(",").append(String.format(Locale.US, "%.2f", pos.y));
            sb.append(") {$").append(Q[i]).append("$};\n");
        }
    }

    private void getEdgesAttributes(HashMap<Integer, HashMap<Integer, ArrayList<Coordinate>>> edgesAttributes,
                                    HashMap<Integer, MutableNode> nodesMap,
                                    MutableNode[] nodes, HashMap<String, Integer> inverseQ) {

        for (MutableNode node : nodes) {
            HashMap<Integer, ArrayList<Coordinate>> currEdges = new HashMap<>();
            Link[] links = node.links().toArray(new Link[0]);
            for (Link link : links) {
                MutableNode to = ((MutableNode) link.to().asLinkSource());
                int targ = inverseQ.get(to.name().value());

                ArrayList<Coordinate> points = new ArrayList<>();
                String pos = (String) link.attrs().get("pos");
                if (pos.startsWith("e,")) pos = pos.substring(2);
                String[] pairs = pos.split(" ");
                for (String pair : pairs) {
                    // This check in here is because in a bug in graphviz-java, where it cannot parse too long lines
                    if (pair.startsWith("\\\r\n")) pair = pair.substring(3);
                    String[] split = pair.split(",");
                    if (split[1].startsWith("\\\r\n")) split[1] = split[1].substring(3);
                    points.add(new Coordinate(Double.parseDouble(split[0]), Double.parseDouble(split[1])));
                }
                currEdges.put(targ, points);

            }

            edgesAttributes.put(inverseQ.get(node.name().value()), currEdges);

            nodesMap.put(inverseQ.get(node.name().toString()), node);
        }
    }

    private HashMap<String, Integer> getInverseQ(Automaton automaton) {
        String[] Q = automaton.getQ();
        HashMap<String, Integer> inverseQ = new HashMap<>();
        for (int i = 0; i < Q.length; i++) {
            inverseQ.put(Q[i], i);
        }
        return inverseQ;
    }


    /**
     * This function will return the direction of a loop in a string that is specified by passed arguments
     *
     * @param node        Coordinate of the node
     * @param middlePoint Coordinate of the middle point of the edge curve
     * @return String containing one of these options: "above", "below", "left", "right"
     */
    private String getLoopDirection(Coordinate node, Coordinate middlePoint) {
        Coordinate diff = Coordinate.subtract(node, middlePoint);
        if (diff.x > 0) {
            if (diff.x > 0) {
                return "below";
            } else {
                return "left";
            }
        } else {
            if (diff.y > 0) {
                return "right";
            } else {
                return "above";
            }
        }
    }


    // TODO: MAKE THIS FUNCTION BETTER!
    /**
     * This function will return the angle of start of the curve. It will be positive, if the curve is to the left,
     * or negative if the curve is to the right.
     *
     * @param source      Coordinate of the source node
     * @param dest        Coordinate of the destination node
     * @param curvePoints Coordinates of anchor points of the curve spline
     * @return integer containing the angle of the start of the curve.
     */
    private int getCurveAngle(Coordinate source, Coordinate dest, ArrayList<Coordinate> curvePoints) {
        Coordinate a = Coordinate.subtract(dest, source);

        double maxAngle = Double.MIN_VALUE;
        int crossOfMax = 0;
        for (Coordinate curvePoint : curvePoints) {
            Coordinate b = Coordinate.subtract(curvePoint, source);

            double numerator = Coordinate.dot(a, b);
            double denominator = a.magnitude() * b.magnitude();
            double angle = Math.acos(numerator / denominator);
            angle = angle * (180 / Math.PI);
            if (angle > maxAngle) {
                maxAngle = angle;
                crossOfMax = (int) Math.signum(a.x * b.y - a.y * b.x);
            }
        }

        return (int) Math.round(maxAngle) * crossOfMax;
    }


    /**
     * Linear mapping function
     */
    private double map(double value, double oldMin, double oldMax, double newMin, double newMax) {
        return newMin + ((value - oldMin) * (newMax - newMin)) / (oldMax - oldMin);
    }

    private Coordinate mapToTikz(Coordinate dotMin, Coordinate dotMax, Coordinate pos) {
        double x = map(pos.x, dotMin.x, dotMax.x, tikzMin.x, tikzMax.x);
        double y = map(pos.y, dotMin.y, dotMax.y, tikzMin.y, tikzMax.y);
        return new Coordinate(x, y);
    }

    /**
     * @return HashMap that has target state as key and all letters as comma-separated String
     */
    private HashMap<Integer, String> getEdgesFromState(Automaton a, int state) {
        HashMap<Integer, String> res = new HashMap<>();
        HashMap<Integer, HashMap<Integer, int[]>> transitions = a.getTransitions();
        String[] sigma = a.getSigma();
        // key: letter, value: targets
        HashMap<Integer, int[]> stateTransitions = transitions.get(state);
        for (int letter = 0; letter < sigma.length; letter++) {
            String letterString = sigma[letter].equals("eps") || sigma[letter].equals("ε") ? "\\varepsilon" : sigma[letter];
            int[] targs = stateTransitions.get(letter);
            for (int targ : targs) {
                if (!res.containsKey(targ)) {
                    res.put(targ, letterString);
                } else {
                    res.put(targ, res.get(targ) + "," + letterString);
                }
            }
        }
        return res;
    }
}
