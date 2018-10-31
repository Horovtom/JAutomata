package cz.cvut.fel.horovtom.jasl.console;

import com.Ostermiller.util.CircularCharBuffer;
import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.NFAAutomaton;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
        Object[] eval = new Object[insideIndices.length / 2];
        for (int i = 0; i < insideIndices.length; i += 2) {
            eval[i / 2] = getExpressionResult(expression.substring(insideIndices[i], insideIndices[i + 1] + 1));
        }
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

        if (insideIndices[insideIndices.length - 1] < expression.length() - 2) {
            //There is something that follows this declaration...
            Object tmp = variables.get("$TEMP");


            variables.put("$TEMP", res);
            res = parseVarFunction("$TEMP" + expression.substring(insideIndices[insideIndices.length - 1] + 2, expression.length()));
            if (tmp == null)
                variables.remove("$TEMP");
            else
                variables.put("$TEMP", tmp);

        }

        return res;
    }

    private Object getDFA(Object[] eval) throws InvalidSyntaxException {
        if (eval.length == 1) {
            Object o = eval[0];
            if (o instanceof ArrayList) {
                Reader res = getReaderFromTable((ArrayList<Object>) o);
                return getDFAFromTable(res);
            }
        } else if (eval.length == 5) {
            return getDFAFromArgs(eval);
        }
        throw new InvalidSyntaxException("Unknown number of parameters...");
    }

    private String getStringFromElementOfTable(Object o) {
        if (o instanceof ArrayList) {
            StringBuilder sb = new StringBuilder();
            sb.append("\"");
            ArrayList<String> as = (ArrayList<String>) o;
            for (int i1 = 0; i1 < as.size(); i1++) {
                if (i1 != 0) {
                    sb.append(",");
                }
                sb.append(as.get(i1));
            }

            sb.append("\"");
            return sb.toString();
        } else if (o instanceof String) {
            return (String) o;
        } else {
            return "";
        }
    }

    private Reader getReaderFromTable(ArrayList<Object> table) throws InvalidSyntaxException {
        try {
            // Create circularCharBuffer
            CircularCharBuffer ccb = new CircularCharBuffer();
            Writer writer = ccb.getWriter();
            ArrayList<String> firstRow = (ArrayList<String>) table.get(0);
            int properLineLength = firstRow.size() + 2;
            writer.write(",");
            for (String aFirstRow : firstRow) {
                writer.write(',' + aFirstRow);
            }
            writer.write('\n');
            for (int i = 1; i < table.size(); i++) {
                ArrayList<Object> strings = (ArrayList<Object>) table.get(i);

                for (int j = 0; j < strings.size(); j++) {

                    if (j == 0 && strings.size() == properLineLength) {
                        writer.write(getStringFromElementOfTable(strings.get(j)));
                    } else {
                        writer.write(',' + getStringFromElementOfTable(strings.get(j)));
                    }
                }
                writer.write('\n');
            }
            writer.close();

            return ccb.getReader();
        } catch (IOException | NullPointerException ignored) {
        }
        return null;

    }

    private Object getDFAFromTable(Reader reader) {

        Automaton a = Automaton.importFromCSV(reader, ',');
        if (a == null) return null;

        return a.getDFA();

    }

    private Object getDFAFromArgs(Object[] args) {
        //TODO: IMPLEMENT THIS
        return null;
    }

    private Object getNFA(Object[] eval) throws InvalidSyntaxException {
        if (eval.length == 1) {
            Object o = eval[0];
            if (o instanceof ArrayList) {
                Reader res = getReaderFromTable((ArrayList<Object>) o);
                return getNFAFromTable(res);
            }
        } else if (eval.length == 5) {
            return getNFAFromArgs(eval);
        }
        throw new InvalidSyntaxException("Unknown number of parameters...");
    }

    private Object getNFAFromArgs(Object[] eval) {
        //TODO: IMPLEMENT
        return null;
    }

    private Object getNFAFromTable(Reader reader) {
        Automaton a = Automaton.importFromCSV(reader, ',');
        if (a == null) return null;

        return a.getNFA();
    }

    private Object getENFAFromTable(Reader reader) {
        Automaton a = Automaton.importFromCSV(reader, ',');
        if (a == null) return null;

        return a.getENFA();
    }

    private Object getENFA(Object[] eval) throws InvalidSyntaxException {
        if (eval.length == 1) {
            Object o = eval[0];
            if (o instanceof ArrayList) {
                Reader res = getReaderFromTable((ArrayList<Object>) o);
                return getENFAFromTable(res);
            }
        } else if (eval.length == 5) {
            return getENFAFromArgs(eval);
        }
        throw new InvalidSyntaxException("Unknown number of parameters...");

    }

    private Object getENFAFromArgs(Object[] eval) {
        //TODO: IMPLEMENT
        return null;
    }

    /**
     * This will extract string arguments from bracket pair. It will return the extracted string indices.
     * For example:
     * input: aab(2,{3,4,1}, ss(12)). This will return:
     * {4, 4, 6, 12, 15, 20}
     *
     * @return integer array with pairs of elements: {StartIndex, EndIndex}
     * @throws InvalidSyntaxException If it could not find closing bracket or any brackets at all
     */
    private int[] extractFromBrackets(String toExtract) throws InvalidSyntaxException {
        int depth = 0;
        char[] arr = toExtract.toCharArray();
        ArrayList<Integer> returning = new ArrayList<>();
        boolean in = false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '(') {
                depth++;

            } else if (arr[i] == '{') {
                if (depth == 1 && !in) {
                    in = true;
                    returning.add(i);
                }
                depth++;
            } else if (arr[i] == ',') {
                if (in && depth == 1) {
                    in = false;
                    returning.add(i - 1);
                }
            } else if (arr[i] == ')' || arr[i] == '}') {
                if (in && depth == 1) {
                    in = false;
                    returning.add(i - 1);
                }
                depth--;
            } else {
                if (depth == 1 && !in) {
                    in = true;
                    returning.add(i);

                }
            }
        }

        return returning.stream().mapToInt(a -> a).toArray();
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
        String call = tokens[1];
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
        int[] argumentIndices = extractFromBrackets(call);
        Object[] arguments = new Object[argumentIndices.length / 2];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = getExpressionResult(call.substring(argumentIndices[i * 2], argumentIndices[i * 2 + 1] + 1));
        }
        return callVarFunction(variables.get(varname), functionName, arguments);
    }

    /**
     * This will call function of a variable and return result as an Object
     *
     * @param varname      variable
     * @param functionName member function of the variable
     * @param arguments     Arguments of the function in an object array.
     * @return Object containing the result of the functions.
     */
    private Object callVarFunction(Object varname, String functionName, Object[] arguments) throws InvalidSyntaxException {


        if (varname instanceof Automaton || varname instanceof DFAAutomaton || varname instanceof NFAAutomaton || varname instanceof ENFAAutomaton) {
            Automaton a = (Automaton) varname;
            // FIXME: Maybe use reflection here?

            if (functionName.equals("reduce")) {
                if (arguments.length > 0)
                    throw new InvalidSyntaxException("Call to reduce should not have any arguments.", "", true);
                return a.getReduced();
            } else if (functionName.equals("accepts")) {
                if (arguments.length != 1)
                    throw new InvalidSyntaxException("Call to accepts should have 1 argument", "", true);
                Object argument = arguments[0];
                if (argument instanceof String) {
                    String arg = (String) argument;
                    return a.acceptsWord(arg);
                } else if (argument instanceof ArrayList) {
                    ArrayList<String> arg = (ArrayList<String>) argument;
                    return a.acceptsWord(arg);
                }
            } else {
                throw new InvalidSyntaxException("Unknown function call", "", true);
            }
        } else {
            throw new InvalidSyntaxException("Unknown function call", "", true);
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

