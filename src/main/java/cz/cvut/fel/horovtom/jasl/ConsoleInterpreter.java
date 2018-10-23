package cz.cvut.fel.horovtom.jasl;

import org.fusesource.jansi.AnsiConsole;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

class ConsoleInterpreter {
    private static Logger LOGGER = Logger.getLogger(ConsoleInterpreter.class.getName());
    /**
     * Language: any string except keywords can be a variable
     * Examples:
     * Table: a={{a, b}, {>, 0, 1, 3}, {<, 1, 2, 1}, {2, 3, 3}, {<, 3, 1, 1}}
     * DFA: b=DFA(a)
     * NFA: c=NFA({{as, es},{0, {0,2}, {1,2}},{1, 1, {2}},{2, {0, 1, 2}, {}}})
     * ENFA: d=ENFA({{{}, as, es}, {0, {}, {}, {}}})
     * Reduction: a=d.reduced
     * Acceptation: d=a.accepts({a, b, c, sa})
     * Printing: a
     * Exit: quit or exit
     */

    private boolean running = true;
    private Scanner sc = new Scanner(System.in);
    HashMap<String, Object> variables = new HashMap<>();


    void start() {

        System.out.println("Console interpreter started...");

        AnsiConsole.systemInstall();

        while (running) {
            try {
                parseLine();
            } catch (InvalidSyntaxException e) {
                System.out.println(ansi().fg(RED).a(e.getMessage()).newline().reset());
//                System.out.println(e.getMessage());
            }
        }
        AnsiConsole.systemUninstall();
    }

    void parseLine() throws InvalidSyntaxException {
        System.out.print(">> ");
        String line = sc.nextLine();
        if (line.equals("")) return;
        if (line.charAt(0) == ' ') throw new InvalidSyntaxException("Input should not start with ' '", line);
        if (line.equals("quit") || line.equals("exit")) {
            LOGGER.info("Setting running flag to false");
            running = false;
            return;
        }

        LOGGER.fine("Parsing line: " + line + " as an assignment...");
        if (parseAssignment(line)) return;

        LOGGER.fine("It was not an assignment... Trying to parse it as an expression...");
        if (parseExpression(line)) return;

        throw new InvalidSyntaxException("Unknown syntax", line);
    }

    /**
     * This will try to parse the line as an expression. It will throw {@link InvalidSyntaxException} if it is not an expression.
     *
     * @param line Line to be parsed
     * @return If true, input was indeed an assignment
     */
    private boolean parseExpression(String line) {
        System.out.println("Parsing as expression: " + line);
        //TODO: IMPLEMENT
        return false;
    }

    /**
     * This will try to parse the line as an assignment. It will throw {@link InvalidSyntaxException} if there was any syntax error
     *
     * @param line Line to be parsed
     * @return If true, input was indeed an assignment.
     */
    private boolean parseAssignment(String line) throws InvalidSyntaxException {
        if (line.indexOf('=') == -1) return false;
        String[] tokens = getNextToken(line, '=');

        String toWhat = tokens[0];
        if (toWhat.length() == 0) throw new InvalidSyntaxException("You have to assign to a variable", line);
        System.out.println("ToWhat was: " + toWhat);
        String what = tokens[1];
        System.out.println("What was: " + what);

        return true;
    }

    /**
     * This function will get next token from inputted string.
     * It will output an array of Strings that has two elements: Token and the rest of the string.
     *
     * @param input String to be tokenized
     * @param delim Delimiter to find in the input.
     * @return Array with two elements: [token, rest_of_string]
     */
    static String[] getNextToken(String input, char delim) {
        int inputLength = input.length();
        int breakingPoint = input.indexOf(delim);
        if (breakingPoint == -1) {
            return new String[]{input, ""};
        }
        return new String[]{input.substring(0, breakingPoint), input.substring(breakingPoint + 1, inputLength)};
    }

    static class InvalidSyntaxException extends Exception {
        public InvalidSyntaxException(String line) {
            super();
            LOGGER.info("Syntax error when parsing line: " + line);
        }

        public InvalidSyntaxException(String message, String line) {
            super(message);
            LOGGER.info("Syntax error when parsing line: " + line);
        }
    }
}

