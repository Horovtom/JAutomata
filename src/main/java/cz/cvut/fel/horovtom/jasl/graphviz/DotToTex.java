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
import java.util.logging.Logger;

public class DotToTex {
    private static final Logger LOGGER = Logger.getLogger(DotToTex.class.getName());

    private class State {
        String id, label;
        int x, y;
        boolean accepting;
        boolean initial;
    }

    private enum Direction {
        N, W, S, E
    }

    private class Coordinate<T> {
        T X;
        T Y;

        Coordinate(T x, T y) {
            this.X = x;
            this.Y = y;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("Coordinate{");
            sb.append("X=").append(X);
            sb.append(", Y=").append(Y);
            sb.append('}');
            return sb.toString();
        }
    }


    // region BOUNDING BOX OF TIKZ IMAGES

    /**
     * Bottom left corner of tikz image
     */
    private final Coordinate<Integer> tikzMin = new Coordinate<>(0, 0);
    /**
     * Top right corner of tikz image
     */
    private final Coordinate<Integer> tikzMax = new Coordinate<>(12, 16);

    // endregion

    private DotToTex() {

    }

    public static String convert(String s) {
        // Initialize this class
        // return result.

        return null;
    }


    public static void main(String[] args) throws IOException {
//        Automaton a = FromRegexConverter.getAutomaton("a*ba");
//        Automaton a = FromRegexConverter.getAutomaton("a*(b+baa)*a");
//        Automaton a = FromRegexConverter.getAutomaton("(a+b)a*(b+ba*a)a");
        Automaton a = AutomatonSamples.DFASamples.lolipop();

        a = a.getReduced();


        String s = GraphvizAPI.toFormattedDot(a);
        System.out.println(GraphvizAPI.toDot(a));
        GraphvizAPI.dotToPNG(a, "test.png", "test2.png", "test3.png");

        DotToTex dtt = new DotToTex();
        String b = dtt.convertToTex(a, s);

        System.out.println(b);
    }

    private String convertToTex(Automaton automaton, String dotCode) throws IOException {

        MutableGraph mg = Parser.read(dotCode);
        MutableNode[] nodes = mg.nodes().toArray(new MutableNode[0]);
        HashMap<Integer, Link[]> edges = new HashMap<>();
        HashMap<Integer, HashMap<Integer, ArrayList<Coordinate<Double>>>> edgesAttributes = new HashMap<>();

        String[] Q = automaton.getQ();
        HashMap<String, Integer> inverseQ = new HashMap<>();
        for (int i = 0; i < Q.length; i++) {
            inverseQ.put(Q[i], i);
        }

        HashMap<Integer, MutableNode> nodesMap = new HashMap<>();

        for (MutableNode node : nodes) {
            HashMap<Integer, ArrayList<Coordinate<Double>>> currEdges = new HashMap<>();
            Link[] links = node.links().toArray(new Link[0]);
            for (Link link : links) {
                MutableNode to = ((MutableNode) link.to().asLinkSource());
                int targ = inverseQ.get(to.name().value());

                ArrayList<Coordinate<Double>> points = new ArrayList<>();
                String pos = (String) link.attrs().get("pos");
                if (pos.startsWith("e,")) pos = pos.substring(2, pos.length());
                String[] pairs = pos.split(" ");
                for (String pair : pairs) {
                    String[] split = pair.split(",");
                    points.add(new Coordinate<>(Double.parseDouble(split[0]), Double.parseDouble(split[1])));
                }
                currEdges.put(targ, points);

            }

            edgesAttributes.put(inverseQ.get(node.name().value()), currEdges);

            nodesMap.put(inverseQ.get(node.name().toString()), node);
            edges.put(inverseQ.get(node.name().value()), links);
        }

        int[] initialStates = automaton.getInitialStates();
        int[] acceptingStates = automaton.getAcceptingStates();

        String[] bb = ((String) mg.graphAttrs().get("bb")).split(",");
//        Coordinate<Integer> min = new Coordinate<>(Integer.parseInt(bb[0]), Integer.parseInt(bb[1]));
//        Coordinate<Integer> max = new Coordinate<>(Integer.parseInt(bb[2]), Integer.parseInt(bb[3]));

        Coordinate<Integer> min = new Coordinate<>(0, 0);
        Coordinate<Integer> max = new Coordinate<>(576, 216);

        // Now try to assemble the output
        StringBuilder sb = new StringBuilder("\\begin{tikzpicture}[->,>=stealth',shorten >=1pt,auto,node distance=2.8cm,semithick,initial text=$ \t$]\n");

        ArrayList<Coordinate<Double>> coordinates = new ArrayList<>(Q.length);
        // Nodes:

        for (int i = 0; i < Q.length; i++) {
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

            MutableNode currNode = nodesMap.get(i);
            String posString = (String) currNode.attrs().get("pos");
            String[] posSplit = posString.split(",");
            Coordinate<Double> pos = new Coordinate<>(Double.parseDouble(posSplit[0]), Double.parseDouble(posSplit[1]));
            coordinates.add(pos);

            // Now normalize for tikz output
            pos = mapToTikz(min, max, pos);

            sb.append(String.format("%.2f", pos.X)).append(",").append(String.format("%.2f", pos.Y));
            sb.append(") {$").append(Q[i]).append("$};\n");
        }


        // Edges:
        sb.append("\t\\path");


        for (int i = 0; i < Q.length; i++) {
            HashMap<Integer, String> curr = getEdgesFromState(automaton, i);
            if (curr.size() == 0) continue;
            sb.append("\n\t\t(").append(i).append(")\n");
            Coordinate<Double> source = coordinates.get(i);

            for (Integer target : curr.keySet()) {
                String edgeLabel = curr.get(target);

                sb.append("\n\t\t\tedge ");

                ArrayList<Coordinate<Double>> edgeAttrs = edgesAttributes.get(i).get(target);

                //FIXME: For now try the middle of the points:


                Coordinate<Double> middlePoint = edgeAttrs.get((int) Math.floor(edgeAttrs.size() / 2));
                String edgeProperties;

                if (target == i) {
                    // It is a loop...
                    String loopDirection = getLoopDirection(source, middlePoint);
                    edgeProperties = "[loop " + loopDirection + "]";
                } else {
                    // Now... How does it curve?

                    int angle = getCurveAngle(source, coordinates.get(target), middlePoint);
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


    /**
     * This function will return the direction of a loop in a string that is specified by passed arguments
     *
     * @param node        Coordinate of the node
     * @param middlePoint Coordinate of the middle point of the edge curve
     * @return String containing one of these options: "above", "below", "left", "right"
     */
    private String getLoopDirection(Coordinate<Double> node, Coordinate<Double> middlePoint) {
        //FIXME: Test this
        double xDiff = node.X - middlePoint.X;
        double yDiff = node.Y - middlePoint.Y;
        if (xDiff > 0) {
            if (yDiff > 0) {
                return "below";
            } else {
                return "left";
            }
        } else {
            if (yDiff > 0) {
                return "right";
            } else {
                return "above";
            }
        }
    }


    /**
     * This function will return the angle of start of the curve. It will be positive, if the curve is to the left,
     * or negative if the curve is to the right.
     *
     * @param source      Coordinate of the source node
     * @param dest        Coordinate of the destination node
     * @param middlePoint Coordinate of the middle point of the edge curve
     * @return integer containing the angle of the start of the curve.
     */
    private int getCurveAngle(Coordinate<Double> source, Coordinate<Double> dest, Coordinate<Double> middlePoint) {

        Coordinate<Double> a = new Coordinate<>(dest.X - source.X, dest.Y - source.Y);
        Coordinate<Double> b = new Coordinate<>(middlePoint.X - source.X, middlePoint.Y - source.Y);

        double numerator = a.X * b.X + a.Y * b.Y;
        double denominator = Math.sqrt(a.X * a.X + a.Y * a.Y) * Math.sqrt(b.X * b.X + b.Y * b.Y);
        double angle = Math.acos(numerator / denominator);
        angle = angle * (180 / Math.PI);
        int cross = (int) Math.signum(a.X * b.Y - a.Y * b.X);

        return (int) Math.round(angle) * cross;
    }


    /**
     * Linear mapping function
     */
    private double map(double value, double oldMin, double oldMax, double newMin, double newMax) {
        return newMin + ((value - oldMin) * (newMax - newMin)) / (oldMax - oldMin);
    }

    private Coordinate<Double> mapToTikz(Coordinate<Integer> dotMin, Coordinate<Integer> dotMax, Coordinate<Double> pos) {
        double x = map(pos.X, dotMin.X, dotMax.X, tikzMin.X, tikzMax.X);
        double y = map(pos.Y, dotMin.Y, dotMax.Y, tikzMin.Y, tikzMax.Y);
        return new Coordinate<>(x, y);
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
