package cz.cvut.fel.horovtom.jasl.interpreter;

import org.fusesource.jansi.AnsiConsole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.Logger;

import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

public class ConsoleInterpreter {
    private static final Logger LOGGER = Logger.getLogger(ConsoleInterpreter.class.getName());
    private final Scanner sc = new Scanner(System.in);
    /**
     * Language: any string except keywords can be a variable
     * Examples:
     * Table: $a={{a, b}, {>, 0, 1, 3}, {<, 1, 2, 1}, {2, 3, 3}, {<, 3, 1, 1}}
     * DFA: $b=DFA($a)
     * NFA: $c=NFA({{as, es},{0, {0,2}, {1,2}},{1, 1, {2}},{2, {0, 1, 2}, {}}})
     * ENFA: $d=ENFA({{{}, as, es}, {0, {}, {}, {}}})
     * Reduction: $a=$d.reduce()
     * Acceptation: $d=$a.accepts({a, b, c, sa})
     * Printing: $a
     * Exit: quit or exit
     */

    private boolean running = true;
    private Interpreter interpreter = new Interpreter();


    /**
     * This process is an infinite loop that will load commands from the console until the users stops it. It will be stopped by setting the running flag to false.
     */
    public void start() {

        System.out.println("Console interpreter started...");

        AnsiConsole.systemInstall();

        while (running) {
            try {
                parseLine();
            } catch (SyntaxException e) {
                System.out.println(ansi().fg(RED).a(e.getMessage()).newline().reset());
            }
        }
        AnsiConsole.systemUninstall();
    }

    /**
     * This will load a line from the console and it will try to parse that line. It will output result of the line to the console.
     *
     * @throws SyntaxException Will be thrown if the syntax was invalid
     */
    private void parseLine() throws SyntaxException {
        System.out.print(">> ");
        String line = sc.nextLine();
        if (line.equals("")) return;
        if (line.charAt(0) == ' ') throw new SyntaxException("Input should not start with ' '");
        switch (line) {
            case "quit":
            case "exit":
                LOGGER.info("Setting running flag to false");
                running = false;
                return;
            case "help":
                LOGGER.info("Displaying help...");
                displayHelpShort();
                return;
            case "helpLong":
                LOGGER.info("Displaying long help...");
                displayLongHelp();
                return;
            case "clear":
                LOGGER.info("Clearing all variables");
                clearVariables();
                return;
        }


        String res = interpreter.parseLine(line);
        System.out.println(res);
    }

    private void clearVariables() {
        interpreter.clear();
    }

    private void displayLongHelp() {
        System.out.println("Long help: ");
        printFile("/JASL/interpreter/commandLineHelpLong.txt");
    }

    private void printFile(String path) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    /**
     * This will print help to the console.
     */
    private void displayHelpShort() {
        System.out.println("Help: ");
        printFile("/JASL/interpreter/commandLineHelpShort.txt");
    }
}

