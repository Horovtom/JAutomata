package cz.cvut.fel.horovtom.jasl.graphviz;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.logging.Logger;

public class GraphvizAPI {
    private static final Logger LOGGER = Logger.getLogger(GraphvizAPI.class.getName());
    // It is a static class
    private GraphvizAPI() {}

    private static class UnknownCommandException extends Exception {
        UnknownCommandException(String message) {
            super(message);
        }
    }

    /**
     * This function will return output of command executed in command line..
     *
     * @param command  Command to be executed
     * @param argument Input for the command.
     * @return String containing the output of the process.
     * @throws IOException If an I/O error occurs
     * @throws UnknownCommandException If graphviz did not return 0.
     *
     */
    private static String execute(String command, String argument) throws IOException, UnknownCommandException {
        Process p = Runtime.getRuntime().exec(command);
        OutputStream os = p.getOutputStream();
        os.write(argument.getBytes());
        os.close();

        // We ignore the InterruptedException, because it would have no effect on the process,
        // since it would be interrupted anyway!
        try {
            p.waitFor();

        } catch (InterruptedException ignored) {}

        StringWriter sw = new StringWriter();
        InputStream is = p.getInputStream();
        IOUtils.copy(is, sw, "UTF-8");
        is.close();
        if (p.exitValue() != 0) throw new UnknownCommandException(command);
        return sw.toString();
    }

    /**
     * This function will attempt to convert dot code to formatted dot code. It will return the formatted code.
     * @param graph String containing the dot code to display the graph.
     * @return String containing the dot code to display the formatted graph.
     */
    private static String dotToDot(String graph) throws IOException {
        try {
            // We have to do it twice to get the real result.
            String s = execute("dot -Tdot \n", graph);
            return execute("dot -Tdot \n", s);
        } catch (UnknownCommandException e) {
            LOGGER.warning("Graphviz has thrown an exception while processing graph: " + graph);
            e.printStackTrace();
            return null;
        }
    }



    /**
     * This function will attempt to convert dot code to PNG image at specified path.
     * @param graph Dot code of the image to be converted
     * @param path Path to which the image should be saved
     * @throws IOException If graphviz did not return 0.
     */
    private static void dotToPng(String graph, String path) throws IOException {
        try {
            String s = execute("dot -Tdot \n", graph);
            execute("dot -Tpng -o " + path + " \n", s);
        } catch (UnknownCommandException e) {
            LOGGER.warning("Graphviz has thrown an exception while saving PNG to " + path + " of graph: " + graph);
            e.printStackTrace();
        }
    }

    /**
     * This function will try to convert Automaton to PNG image and save it at specified path
     */
    public static void toPNG(Automaton a, String path){
        try {
            dotToPng(ToDotConverter.convertToDot(a), path);
        } catch (IOException e) {
            LOGGER.warning("Graphviz has thrown IOException! " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
    }

    /**
     * This function will return dot code of the graphviz-ed automaton.
     *
     * @param a Automaton to be converted
     * @return Dot code of the formatted automaton
     */
    public static String toFormattedDot(Automaton a) {
        try {
            return dotToDot(ToDotConverter.convertToDot(a));
        } catch (IOException e) {
            LOGGER.warning("Graphviz has thrown IOException! " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This function will return dot code of the automaton before it has been graphviz-ed.
     *
     * @param a automaton to be converted
     * @return Dot code of the automaton before formatting
     */
    public static String toDot(Automaton a) {
        return ToDotConverter.convertToDot(a);
    }

    /**
     * This function will return tikz code to display specified automaton. If fixed is true, it will use fixed coordinates
     * for the display
     * @param automaton Automaton to be displayed
     * @param fixed Whether tikz code should use fixed coordinates
     * @return TIKZ code to display specified automaton
     */
    public static String toTikz(Automaton automaton, boolean fixed) throws IOException {
        return DotToTex.convert(automaton, dotToDot(ToDotConverter.convertToDot(automaton)), fixed);
    }
}
