package cz.cvut.fel.horovtom.jasl.console;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import org.fusesource.jansi.AnsiConsole;

import java.util.ArrayList;
import java.util.HashMap;
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
     * Reduction: $a=$d.reduced
     * Acceptation: $d=$a.accepts({a, b, c, sa})
     * Printing: $a
     * Exit: quit or exit
     */

    private boolean running = true;
    private Scanner sc = new Scanner(System.in);
    private HashMap<String, Object> variables = new HashMap<>();


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
    private boolean parseExpression(String line) throws InvalidSyntaxException {
        try {
            String result = getExpressionResult(line).toString();
            System.out.println(result);
            return true;
        } catch (NullPointerException | InvalidSyntaxException e) {
            if (InvalidSyntaxException.probablyIs) throw e;
            return false;
        }
    }

    /**
     * This will try to parse the line as an expression. It will throw {@link InvalidSyntaxException} if it is not valid.
     *
     * @param expression Line to be parsed
     * @return evaluation result
     */
    private Object getExpressionResult(String expression) throws InvalidSyntaxException {
        // Check, whether it is a variable by itself
        if (variables.containsKey(expression)) {
            return variables.get(expression);
        }

        if (expression.equals("")) return "";

        // Check, whether it is a call to variable function
        try {
            return parseVarFunction(expression);
        } catch (InvalidSyntaxException e) {
            if (InvalidSyntaxException.probablyIs) throw e;
        }

        try {
            return parseList(expression);
            //ArrayList<Object> objectList = list.stream().map(e -> getExpressionResult(e)).collecct(Collectors.toList());
        } catch (InvalidSyntaxException e) {
            if (InvalidSyntaxException.probablyIs) throw e;
        }

        try {
            return parseCommand(expression);
        } catch (InvalidSyntaxException e) {
            if (InvalidSyntaxException.probablyIs) throw e;
        }

        return expression;
    }

    /**
     * This will try to parse expression as a command (meaning constructor) of object. It will throw {@link InvalidSyntaxException} if it is not valid
     * e.g.: DFA($table) e.t.c.
     *
     * @param expression Expression to be parsed
     * @return A resulting object of the command call
     * @throws InvalidSyntaxException if it is not a command
     */
    private Object parseCommand(String expression) throws InvalidSyntaxException {
        int[] insideIndices = extractFromBrackets(expression);
        Object eval = getExpressionResult(expression.substring(insideIndices[0], insideIndices[1]));
        Object res;
        if (expression.startsWith("DFA(")) {
            //DFA constructor
            res = getDFA(eval);
        } else if (expression.startsWith("NFA(")) {
            res = getNFA(eval);
        } else if (expression.startsWith("ENFA(")) {
            res = getENFA(eval);
        } else {
            // TODO: IDK WHAT WILL GO IN HERE YET...
            throw new InvalidSyntaxException("Unknown parse command", expression);
        }

        if (insideIndices[1] != expression.length() - 2) {
            //There is something that follows this declaration...
            //TODO: IMPLEMENT THIS
            throw new InvalidSyntaxException("Not implemented yet! You have to assign first before calling on it!", expression);
        }
        return null;
    }

    private Object getDFA(Object eval) throws InvalidSyntaxException {
        //TODO: IMPLEMENT THIS
        if (eval instanceof ArrayList) {
            // We need to somehow extract info about Sigma, Q and transitions from the table.
            return getDFAFromTable((ArrayList<String>) eval);
        }
        return null;
    }

    private Object getDFAFromTable(ArrayList<String> table) {
        return null;
    }

    private Object getNFA(Object eval) {
        //TODO: IMPLEMENT THIS

        return null;
    }

    private Object getENFA(Object eval) {
        //TODO: IMPLEMENT THIS

        return null;
    }

    /**
     * This will extract string from bracket pair. It will return the extracted string indices.
     *
     * @return integer array with two elements: StartIndex, EndIndex
     * @throws InvalidSyntaxException If it could not find closing bracket or any brackets at all
     */
    private int[] extractFromBrackets(String toExtract) throws InvalidSyntaxException {
        int length = toExtract.length();
        int depth = 0;
        int startIndex = 0;
        char[] arr = toExtract.toCharArray();
        for (int i = 0; i < length; i++) {
            if (arr[i] == '(') {
                if (depth == 0) {
                    startIndex = i + 1;
                }
                depth++;
            } else if (arr[i] == ')') {
                depth--;
            }
            if (depth == 0) {
                return new int[]{startIndex, i + 1};
            }
        }

        if (depth != 0)
            throw new InvalidSyntaxException("Could not find closing bracket...", toExtract);
        throw new InvalidSyntaxException("Could not find any brackets...", toExtract);
    }

    /**
     * This will try to parse the expression as a variable function call. It will throw {@link InvalidSyntaxException} if it is not valid
     * e.g.: $a.reduce() or $b.accepts({2, 3, 4})
     *
     * @param expression Expression to be parsed
     * @return A result object if it is a variable function call
     * @throws InvalidSyntaxException if it is not a variable function call.
     */
    private Object parseVarFunction(String expression) throws InvalidSyntaxException {
        if (expression.charAt(0) != '$')
            throw new InvalidSyntaxException("Variable names have to start with $", expression);

        // Find variable name
        String[] tokens = getNextToken(expression, '.');
        if (tokens[1].equals(""))
            throw new InvalidSyntaxException("Member function of variable not specified", expression, true);
        String varname = tokens[0];
        if (!variables.containsKey(varname)) throw new InvalidSyntaxException("Unknown variable", expression, true);

        // Find function name
        tokens = getNextToken(tokens[1], '(');

        if (tokens[1].equals(""))
            throw new InvalidSyntaxException("Function calls have to end with parenthesis", expression, true);
        String functionName = tokens[0];

        //Find arguments
        if (!tokens[1].endsWith(")"))
            throw new InvalidSyntaxException("Could not find closing parenthesis for function call", expression, true);
        Object argument = getExpressionResult(tokens[1].substring(0, tokens[1].length() - 1));

        return callVarFunction(variables.get(varname), functionName, argument);
    }

    /**
     * This will call function of a variable and return result as an Object
     *
     * @param varname      variable
     * @param functionName member function of the variable
     * @param argument     Argument of the function in an object.
     * @return Object containing the result of the functions.
     */
    private Object callVarFunction(Object varname, String functionName, Object argument) throws InvalidSyntaxException {
        if (varname instanceof Automaton) {
            Automaton a = (Automaton) varname;
            // FIXME: Maybe use reflection here?

            if (functionName.equals("reduce")) {
                return a.getReduced();
            } else if (functionName.equals("accepts")) {
                if (argument instanceof String) {
                    String arg = (String) argument;
                    return a.acceptsWord(arg);
                } else if (argument instanceof ArrayList) {
                    ArrayList<String> arg = (ArrayList<String>) argument;
                    return a.acceptsWord(arg);
                }
            }
        }

        //TODO: IMPLEMENT
        return null;
    }

    /**
     * This will try to parse the expression as a list. It will throw {@link InvalidSyntaxException} if it is not valid
     * e.g.: {a, b, c}
     *
     * @param expression Expression to be parsed into a list
     * @return A list of Objects if it is a list.
     * @throws InvalidSyntaxException if it is not a list
     */
    private ArrayList<Object> parseList(String expression) throws InvalidSyntaxException {
        if (expression.charAt(0) != '{')
            throw new InvalidSyntaxException("List cannot start with " + expression.charAt(0), expression);
        ArrayList<String> elements = new ArrayList<>();

        String[] token = new String[]{"", expression.substring(1, expression.length())};
        while (true) {
            token[1] = token[1].trim();
            if (token[1].startsWith("{")) {
                // It is a nested list...
                token = getNextTokenList(token[1]);
                elements.add(token[0]);
                if (token[1].length() == 0)
                    throw new InvalidSyntaxException("Did not find closing bracket of list", expression);

                if (token[1].equals("}")) break;

                continue;
            }
            token = getNextToken(token[1], ',');


            if (token[1].equals("")) {
                if (!token[0].endsWith("}"))
                    throw new InvalidSyntaxException("Could not find end of list", expression, true);
                elements.add(token[0].substring(0, token[0].length() - 1));
                break;
            } else {
                elements.add(token[0]);
            }
        }

        // Correct list loaded. Now we need to call expression result on each element
        ArrayList<Object> objectsList = new ArrayList<>();
        for (String element : elements) {
            objectsList.add(getExpressionResult(element));
        }

        return objectsList;
    }

    /**
     * This function will extract the next list token from input. It will return array of strings with two elements: list, rest
     * It will throw {@link InvalidSyntaxException} if it didn't find the end of the list.
     *
     * @param input String to be tokenized
     * @return Array with two elements: [token, rest]
     * @throws InvalidSyntaxException If there was no closing bracket to the list.
     */
    private String[] getNextTokenList(String input) throws InvalidSyntaxException {
        if (input.charAt(0) != '{') throw new InvalidSyntaxException("Next token is not a list", input);
        int index = input.indexOf('}');
        if (index == -1) throw new InvalidSyntaxException("Could not find end of nested list", input);

        char[] inputArr = input.toCharArray();
        int level = 0;
        int length = input.length();
        for (int i = 0; i < length; i++) {
            if (inputArr[i] == '{') {
                level++;
            } else if (inputArr[i] == '}') {
                level--;
            }

            if (level == 0) {
                if (i == length - 1) {
                    return new String[]{input, ""};
                }
                if (inputArr[i + 1] == '}') {
                    return new String[]{new String(inputArr, 0, i + 1), new String(inputArr, i, length - i - 1)};
                } else if (inputArr[i + 1] == ',') {
                    return new String[]{new String(inputArr, 0, i + 1), new String(inputArr, i + 2, length - i - 2)};
                } else {
                    throw new InvalidSyntaxException("Unexpected character at the end of list element", input);
                }
            }
        }

        throw new InvalidSyntaxException("Could not find closing bracket of list element", input);
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

        String toWhat = tokens[0].trim();
        if (toWhat.length() == 0) throw new InvalidSyntaxException("You have to assign to a variable", line);
        if (toWhat.charAt(0) != '$') throw new InvalidSyntaxException("Variables must start with '$'", line);

        Object result = getExpressionResult(tokens[1].trim());
        variables.put(toWhat, result);

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
    public static String[] getNextToken(String input, char delim) {
        int inputLength = input.length();
        int breakingPoint = input.indexOf(delim);
        if (breakingPoint == -1) {
            return new String[]{input, ""};
        }
        return new String[]{input.substring(0, breakingPoint), input.substring(breakingPoint + 1, inputLength)};
    }

    public static class InvalidSyntaxException extends Exception {
        static boolean probablyIs = false;

        public InvalidSyntaxException(String line) {
            super();
            LOGGER.fine("Syntax error when parsing line: " + line);
            probablyIs = false;
        }

        public InvalidSyntaxException(String message, String line) {
            super(message);
            LOGGER.fine("Syntax error when parsing line: " + line);
            probablyIs = false;
        }

        /**
         * @param probablyIs It is a flag for catching function that the caller probably found what type of expression it was
         */
        public InvalidSyntaxException(String message, String line, boolean probablyIs) {
            super(message);
            LOGGER.fine("Syntax error when parsing line: " + line + ", but it " + (probablyIs ? "found" : "not found") + " the expression");
            InvalidSyntaxException.probablyIs = probablyIs;
            if (probablyIs) {
                LOGGER.info("Syntax error when parsing line: " + line + ", but it found the expression.");
            }
        }
    }
}

