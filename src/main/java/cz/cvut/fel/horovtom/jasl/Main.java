package cz.cvut.fel.horovtom.jasl;


import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {
        System.out.println("Welcome to Java Automata Scripting Language (JASL) interpreter");


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

    private static void loadFromFile(String path) {
        System.out.println("Loading from file: " + path);
    }

    private static void displayHelp() {
        System.out.println("This is help to JASL interpreter");
    }
}
