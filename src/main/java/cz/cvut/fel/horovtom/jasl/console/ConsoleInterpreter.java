package cz.cvut.fel.horovtom.jasl.console;

import cz.cvut.fel.horovtom.jasl.interpreter.Interpreter;
import cz.cvut.fel.horovtom.jasl.interpreter.Interpreter.InvalidSyntaxException;
import org.fusesource.jansi.AnsiConsole;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Logger;

import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

public class ConsoleInterpreter {
    private static Logger LOGGER = Logger.getLogger(ConsoleInterpreter.class.getName());
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
    private Scanner sc = new Scanner(System.in);
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
            } catch (InvalidSyntaxException e) {
                System.out.println(ansi().fg(RED).a(e.getMessage()).newline().reset());
            }
        }
        AnsiConsole.systemUninstall();
    }

    /**
     * This will load a line from the console and it will try to parse that line. It will output result of the line to the console.
     *
     * @throws InvalidSyntaxException Will be thrown if the syntax was invalid
     */
    private void parseLine() throws InvalidSyntaxException {
        System.out.print(">> ");
        String line = sc.nextLine();
        if (line.equals("")) return;
        if (line.charAt(0) == ' ') throw new InvalidSyntaxException("Input should not start with ' '", line);
        if (line.equals("quit") || line.equals("exit")) {
            LOGGER.info("Setting running flag to false");
            running = false;
            return;
        } else if (line.equals("help")) {
            LOGGER.info("Displaying help...");
            displayHelp();
            return;
        } else if (line.equals("helpLong")) {
            LOGGER.info("Displaying long help...");
            displayLongHelp();
            return;
        }


        String res = interpreter.parseLine(line);
        System.out.println(res);
    }

    private void displayLongHelp() {
        System.out.println("Long help: ");
        printFile("JASL/interpreter/commandLineHelp.txt");
    }

    private void printFile(String path) {
        File f = new File(Objects.requireNonNull(ConsoleInterpreter.class.getClassLoader().getResource(path)).getFile());
        try (BufferedReader br = new BufferedReader(new FileReader(f))){
            String line;
            while((line= br.readLine()) != null) {
                System.out.println(line);
            }
        } catch(IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    /**
     * This will print help to the console.
     */
    private void displayHelp() {
        System.out.println("Help: ");
        printFile("JASL/interpreter/commandLineHelpShort.txt");
    }
}

