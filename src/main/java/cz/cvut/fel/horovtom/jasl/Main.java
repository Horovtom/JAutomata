package cz.cvut.fel.horovtom.jasl;


import cz.cvut.fel.horovtom.jasl.interpreter.ConsoleInterpreter;
import cz.cvut.fel.horovtom.jasl.interpreter.FileInterpreter;

import java.io.IOException;
import java.util.logging.*;

public class Main {
    private static Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to Java Automata Scripting Language (JASL) interpreter");

        //TODO: FIND BETTER WAY
        setLoggingHandlers();

        if (args.length > 0) {
            if (args[0].equals("-h")) displayHelp();
            if (args[0].equals("-f")) {
                try {
                    loadFromFile(args[1]);

                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("No file path specified...");
                    displayHelp();
                }
            }

        } else {
            System.out.println("There were no arguments, starting console interpreter...");
            ConsoleInterpreter interpreter = new ConsoleInterpreter();
            interpreter.start();
        }
    }

    private static void setLoggingHandlers() throws IOException {
        Handler fh = new FileHandler("LOG.log");
        Logger logger = Logger.getLogger("");
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        logger.addHandler(fh);


        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.WARNING);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.WARNING);
        }
    }

    private static void loadFromFile(String path) {
        System.out.println("Loading from file: " + path);
        FileInterpreter interpreter = new FileInterpreter(path);
        boolean exitStatus = interpreter.start();
        if (!exitStatus) {
            System.err.println("Interpreter exited on error!");
            interpreter.getException().printStackTrace();
        }
    }

    private static void displayHelp() {
        System.out.println("This is help to JASL interpreter");
        System.out.println("Run without any switches to get into the interactive shell environment.");
        System.out.println("Run with -h switch to display this help");
        System.out.println("Run with -f <path_to_file> argument to execute commands from the file.");
    }
}
