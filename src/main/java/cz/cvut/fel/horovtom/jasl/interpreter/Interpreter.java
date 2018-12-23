package cz.cvut.fel.horovtom.jasl.interpreter;

import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.converters.FromRegexConverter;
import cz.cvut.fel.horovtom.jasl.graphviz.GraphvizAPI;
import cz.cvut.fel.horovtom.jasl.graphviz.Layout;
import cz.cvut.fel.horovtom.utilities.Utilities;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class Interpreter {
    private static Logger LOGGER = Logger.getLogger(Interpreter.class.getName());

    private final String tikzIncludes =
            "\\usepackage{tikz}\n" +
                    "\\usetikzlibrary{shapes, angles, calc, quotes,arrows,automata, positioning}\n";

    private HashMap<String, Object> variables;

    public Interpreter() {
        LOGGER.info("Starting up new JASL interpreter");
        variables = new HashMap<>();
    }

    /**
     * This function will try to parse line as a command. It will output a string containing the answer to the command. If it was an assignment, it will output empty string.
     *
     * @param line String containing the command for the interpreter.
     * @return String with the result of the command.
     * @throws InvalidSyntaxException Will be thrown if there was some invalid syntax in the expression.
     */
    public String parseLine(String line) throws InvalidSyntaxException {
        LOGGER.info("Parsing line: " + line);
        LOGGER.fine("Parsing line: " + line + " as an assignment...");
        try {
            if (parseAssignment(line)) return "";
        } catch (InvalidSyntaxException e) {
            if (InvalidSyntaxException.probablyIs) throw e;
        }
        LOGGER.fine("It was not an assignment... Trying to parse it as an expression...");
        try {
            return parseExpression(line);
        } catch (InvalidSyntaxException e) {
            if (InvalidSyntaxException.probablyIs) throw e;
        }

        throw new InvalidSyntaxException("Unknown command!", line);
    }

    /**
     * This will try to parse the line as an expression. It will throw {@link InvalidSyntaxException} if it is not an expression.
     *
     * @param line Line to be parsed
     * @return Answer to the expression
     */
    private String parseExpression(String line) throws InvalidSyntaxException {
        try {
            return getExpressionResult(line).toString();
        } catch (NullPointerException | InvalidSyntaxException e) {
            if (InvalidSyntaxException.probablyIs) throw e;
            return "";
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
            // DFA C-TOR
            res = getDFA(eval);
        } else if (expression.startsWith("NFA(")) {
            // NFA C-TOR
            res = getNFA(eval);
        } else if (expression.startsWith("ENFA(")) {
            // ENFA C-TOR
            res = getENFA(eval);
        } else if (expression.startsWith("fromCSV(")) {
            // IMPORT AUTOMATON FROM CSV
            if (eval.length != 1)
                throw new InvalidSyntaxException("fromCSV function takes 1 arguments!", expression, true);

            String path = (String) eval[0];
            LOGGER.info("Importing Automaton from CSV file at: " + path);
            try {
                res = Automaton.importFromCSV(new File(path));
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                throw new InvalidSyntaxException("Could not find file: " + path, expression, true);
            }
        } else if (expression.startsWith("getExample()")) {
            // GETTING SAMPLE AUTOMATON
            res = getExpressionResult("NFA({{a, b},{>, 0, 1, {2,3}},{>, 1, {}, {1, 4}},{<>, 2, {}, 0},{<, 3, 3, 3},{4,4,2}})");
        } else if (expression.startsWith("fromRegex(")) {
            // IMPORT FROM REGEX
            if (eval.length != 1)
                throw new InvalidSyntaxException("fromRegex function takes 1 argument.", expression, true);
            res = FromRegexConverter.getAutomaton((String) eval[0]);
        } else if (expression.equals("getTikzIncludes()")) {
            res = tikzIncludes;
        } else {
            throw new InvalidSyntaxException("Unknown parse command", expression);
        }

        // We have to check, whether there is some chaining:
        String commandToChain = null;
        if (insideIndices.length > 0) {
            // It had some arguments
            if (insideIndices[insideIndices.length - 1] < expression.length() - 2)
                commandToChain = expression.substring(insideIndices[insideIndices.length - 1] + 2, expression.length());
        } else {
            // It is without arguments
            // Find end of function:
            int start = expression.indexOf("()") + 2;
            if (start < expression.length()) {
                if (expression.charAt(start) != '.')
                    throw new InvalidSyntaxException("Unexpected token at position: " + (start) + ". '.' expected.", expression, true);
                commandToChain = expression.substring(start, expression.length());
            }
        }

        if (commandToChain != null) {
            res = chainOnTemp(res, commandToChain);
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
            // Create temporary file.
            File temp = Utilities.createTempFile();
            FileWriter writer = new FileWriter(temp);
            ArrayList<String> firstRow = (ArrayList<String>) table.get(0);
            int properLineLength = firstRow.size() + 2;
            writer.write(",");
            for (String aFirstRow : firstRow) {
                writer.write(',' + aFirstRow);
            }
            writer.write('\n');
            for (int i = 1; i < table.size(); i++) {
                ArrayList<Object> strings = (ArrayList<Object>) table.get(i);
                int size = strings.size();

                int commasAtEnd = 0;
                for (int j = 0; j < size; j++) {
                    String elem = getStringFromElementOfTable(strings.get(j));
                    if (j == 0) {
                        if (size == properLineLength) {
                            writer.write(elem);
                        } else if (size < properLineLength) {
                            if (elem.equals("<>") || elem.equals("<") || elem.equals(">")) {
                                writer.write(elem);
                                commasAtEnd = properLineLength - size;
                            } else {
                                writer.write(',' + elem);
                            }
                        } else {
                            throw new InvalidSyntaxException("Invalid automaton definition. Line lengths did not match.", "", true);
                        }
                    } else {
                        writer.write(',' + elem);
                    }

                }

                for (int j = 0; j < commasAtEnd; j++) writer.write(',');

                writer.write('\n');
            }
            writer.close();

            return new FileReader(temp);
        } catch (IOException | NullPointerException ignored) {
        }
        return null;

    }

    private Object getDFAFromTable(Reader reader) {

        Automaton a = Automaton.importFromCSV(reader, ',');
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (a == null) return null;
        return a.getDFA();
    }

    private Object getNFA(Object[] eval) throws InvalidSyntaxException {
        if (eval.length == 1) {
            Object o = eval[0];
            if (o instanceof ArrayList) {
                Reader res = getReaderFromTable((ArrayList<Object>) o);
                return getNFAFromTable(res);
            }
        }
        throw new InvalidSyntaxException("Unknown number of parameters...");
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
        }
        throw new InvalidSyntaxException("Unknown number of parameters...");

    }

    /**
     * This will extract string arguments from bracket pair. It will return the extracted string indices.
     * For example:
     * input: aab(2,{3,4,1}, ss(12)). This will return:
     * {4, 4, 6, 12, 15, 20}
     *
     * @return integer array with pairs of elements: {StartIndex, EndIndex}
     */
    private int[] extractFromBrackets(String toExtract) throws InvalidSyntaxException {
        return extractFromBrackets(toExtract, '(', ')', '{', '}');
    }

    /**
     * This will extract argument indices from bracket pair. It will return the extracted string indices.
     * Used by: {@link #extractFromBrackets(String)}, {@link #extractFromCurlyBrackets}
     */
    private int[] extractFromBrackets(String toExtract, char mainOpen, char mainClose, char otherOpen, char otherClose) throws InvalidSyntaxException {
        int depth = 0;
        char[] arr = toExtract.toCharArray();
        ArrayList<Integer> returning = new ArrayList<>();
        boolean in = false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == mainOpen) {
                if (depth == 1 && !in) {
                    in = true;
                    returning.add(i);
                }
                depth++;
            } else if (arr[i] == otherOpen) {
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
            } else if (arr[i] == mainClose) {
                if (in && depth == 1) {
                    in = false;
                    returning.add(i - 1);
                }
                depth--;
                if (depth == 0) break;
            } else if (arr[i] == otherClose) {
                depth--;
                if (depth <= 0)
                    throw new InvalidSyntaxException("List in bracket had invalid closing bracket", toExtract);
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
     * This will extract string arguments from curly bracket pair. It will return the extracted string indices.
     * For example:
     * input: bcs{2, (a, c, a), d} This will return:
     * {4, 4, 6, 15, 17, 18}
     *
     * @return integer array with pairs of elements: {StartIndex, EndIndex}
     */
    private int[] extractFromCurlyBrackets(String toExtract) throws InvalidSyntaxException {
        return extractFromBrackets(toExtract, '{', '}', '(', ')');
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
        Object res = callVarFunction(variables.get(varname), functionName, arguments);

        String toBeChained = null;
        if (argumentIndices.length == 0 && tokens[1].startsWith(")") && tokens[1].length() > 1 && tokens[1].charAt(1) == '.') {
            //Extract chained statement from no arguments call
            toBeChained = tokens[1].substring(1, tokens[1].length());
        } else if (argumentIndices.length > 0 && argumentIndices[argumentIndices.length - 1] + 2 < call.length()) {
            toBeChained = call.substring(argumentIndices[argumentIndices.length - 1] + 2, call.length());
        }

        if (toBeChained != null) {
            res = chainOnTemp(res, toBeChained);
        }

        return res;
    }

    /**
     * This function will execute command on temp object.
     * It will create new 'stack frame'. (Only using $TEMP variable, while saving its old value)
     *
     * @param temp    Object for the command to be executed onto
     * @param command String with commands that should be executed on the object. In form of '.func(args)'
     * @return Object that is the result of execution
     */
    private Object chainOnTemp(Object temp, String command) throws InvalidSyntaxException {
        Object tmp = variables.get("$TEMP");
        variables.put("$TEMP", temp);
        Object res = parseVarFunction("$TEMP" + command);
        if (tmp == null) {
            variables.remove("$TEMP");
        } else {
            variables.put("$TEMP", tmp);
        }
        return res;
    }


    /**
     * This will call automaton member function specified in the arguments.
     * <p>
     * Valid functions are:
     * reduce(),
     * accepts(String[]), accepts(ArrayList), accepts(String)
     * toCSV(String),
     * toTikz(),
     * toPNG(String),
     * toTexTable(),
     * toRegex(),
     * toDot(),
     * toSimpleDot()
     * equals(Automaton)
     */
    private Object callAutomatonMemberFunction(Automaton a, String functionName, Object[] arguments) throws InvalidSyntaxException {
        Object argument;
        switch (functionName) {
            case "equals":
                if (arguments.length != 1)
                    throw new InvalidSyntaxException("Call to equals should have 1 argument.", "", true);
                argument = arguments[0];
                if (!(argument instanceof Automaton)) return false;
                Automaton other = (Automaton) argument;
                return a.equals(other);

            case "reduce":
                if (arguments.length > 0)
                    throw new InvalidSyntaxException("Call to reduce should not have any arguments.", "", true);
                return a.getReduced();

            case "accepts":
                if (arguments.length == 0) return a.acceptsWord("");
                if (arguments.length != 1)
                    throw new InvalidSyntaxException("Call to accepts should have 1 or 0 arguments", "", true);

                argument = arguments[0];
                if (argument instanceof String) {
                    String arg = (String) argument;
                    return a.acceptsWord(arg);
                } else if (argument instanceof ArrayList) {
                    ArrayList<String> arg = (ArrayList<String>) argument;
                    return a.acceptsWord(arg);
                } else {
                    throw new InvalidSyntaxException("Invalid class of argument of accepts: " + argument.getClass(), "", true);
                }

            case "toCSV":
                if (arguments.length != 1)
                    throw new InvalidSyntaxException("Invalid number of arguments: " + arguments.length + " for toCSV member function.", "", true);
                String path = (String) arguments[0];
                LOGGER.info("Trying to export to CSV to path: " + path);
                a.exportToCSV(new File(path));
                return null;
            case "toTikz":
                boolean fixed = true;
                if (arguments.length > 1)
                    throw new InvalidSyntaxException("Invalid number of arguments: " + arguments.length + " expected 1.", "", true);
                if (arguments.length == 1) fixed = Boolean.parseBoolean((String) arguments[0]);

                try {
                    return GraphvizAPI.toTikz(a, fixed);
                } catch (IOException e) {
                    throw new InvalidSyntaxException("IOException when converting to TEX", "", true);
                }

            case "toPNG":
                return convertToPng(a, arguments);

            case "toTexTable":
                return a.exportToString().getTEX();

            case "toRegex":
                return a.getRegex();

            case "toDot":
                if (arguments.length == 1) {
                    if (!(arguments[0] instanceof String)) throw new InvalidSyntaxException(
                            "Invalid type of argument: " + arguments[0].getClass() + ". toDot expects String.", "", true);
                    try {
                        return GraphvizAPI.toFormattedDot(a, Layout.fromString((String) arguments[0]));
                    } catch (Layout.InvalidLayoutException e) {
                        throw new InvalidSyntaxException("Layout has to be one of the valid layouts e.g. 'neato'.", "", true);
                    }
                } else if (arguments.length > 1) {
                    throw new InvalidSyntaxException("toDot expects at most one argument.", "", true);
                } else {
                    return GraphvizAPI.toFormattedDot(a);
                }
            case "toSimpleDot":
                return GraphvizAPI.toDot(a);

            default:
                throw new InvalidSyntaxException("Unknown function call", "", true);
        }

    }

    /**
     * This converts an automaton to PNG image.
     *
     * @param a         Automaton to convert
     * @param arguments Arugments of the function call. First is the path to the PNG image. Second is optional and it is
     *                  the layout algorithm.
     */
    private Object convertToPng(Automaton a, Object[] arguments) throws InvalidSyntaxException {
        if (arguments.length == 0 || arguments.length > 2)
            throw new InvalidSyntaxException(
                    "Invalid number of arguments: " + arguments.length + ". toPNGImage expects 1 or 2 arguments.",
                    "", true);

        if (!(arguments[0] instanceof String)) throw new InvalidSyntaxException(
                "Invalid type of argument: " + arguments[0].getClass() + ". toPNGImage expects String.",
                "", true);
        String p = (String) arguments[0];
        File f = Paths.get(p).toFile();


        if (f.isDirectory())
            throw new InvalidSyntaxException("Cannot write to file at: " + p, "", true);
        if (f.exists()) {
            LOGGER.info("Overwriting file at: " + p);
            if (!f.delete()) {
                throw new InvalidSyntaxException("Cannot overwrite file at: " + p, "", true);
            }
        }

        if (arguments.length > 1) {
            if (!(arguments[1] instanceof String)) throw new InvalidSyntaxException(
                    "Invalid type of argument: " + arguments[1].getClass() + ". toPNGImage expects String, String.",
                    "", true);
            String layout = (String) arguments[1];
            layout = layout.trim();
            try {
                GraphvizAPI.toPNG(a, p, Layout.fromString(layout));
            } catch (Layout.InvalidLayoutException e) {
                throw new InvalidSyntaxException("Layout has to be one of the valid layouts e.g. 'neato'.", "", true);
            }
        } else {
            GraphvizAPI.toPNG(a, p);
        }
        return null;
    }

    /**
     * This will call specified function of string on specified arguments.
     */
    private Object callStringMemberFunction(String s, String functionName, Object[] arguments) throws InvalidSyntaxException {
        switch (functionName) {
            case "save":
                if (!(arguments.length == 1 && arguments[0] instanceof String)) throw new InvalidSyntaxException(
                        "save function needs path argument to be specified!", "", true);
                String path = (String) arguments[0];

                File f = new File(path);
                if (f.isDirectory()) throw new InvalidSyntaxException("Path does not lead to a file!", "", true);
                if (f.isFile()) {
                    // This means it exists, so we will append to it!
                    LOGGER.info("Appending to file: " + path);
                    try {
                        Files.write(Paths.get(path), s.getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        LOGGER.warning("IOException occurred when trying to append to file. " + e.getMessage());
                        throw new InvalidSyntaxException("I/O exception when trying to append to file: " + path, "", true);
                    }
                } else {
                    // Create and write:
                    try {
                        Files.write(Paths.get(path), s.getBytes());
                    } catch (IOException e) {
                        LOGGER.warning("IOException occurred when trying to write to file. " + e.getMessage());
                        throw new InvalidSyntaxException("I/O exception when trying to write to file: " + path, "", true);
                    }
                }

                break;

            default:
                throw new InvalidSyntaxException("Unknown function call on string", "", true);
        }

        return null;
    }

    /**
     * This will call function of a variable and return result as an Object
     *
     * @param var          variable
     * @param functionName member function of the variable
     * @param arguments    Arguments of the function in an object array.
     * @return Object containing the result of the functions.
     */
    private Object callVarFunction(Object var, String functionName, Object[] arguments) throws InvalidSyntaxException {
        if (var instanceof Boolean) var = var.toString();
        if (var instanceof Automaton) {
            Automaton a = (Automaton) var;

            return callAutomatonMemberFunction(a, functionName, arguments);
        } else if (var instanceof String) {
            String s = (String) var;

            return callStringMemberFunction(s, functionName, arguments);
        } else {
            throw new InvalidSyntaxException("Unknown function call", "", true);
        }

//        return null;
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
        int[] elemsIndices = extractFromCurlyBrackets(expression);
        if (elemsIndices.length == 0) return new ArrayList<>();
        if (elemsIndices[elemsIndices.length - 1] != expression.length() - 2)
            throw new InvalidSyntaxException("List does not have valid ending", expression);
        ArrayList<Object> listItems = new ArrayList<>();
        int len = elemsIndices.length / 2;
        for (int i = 0; i < len; i++) {
            listItems.add(getExpressionResult(expression.substring(elemsIndices[i * 2], elemsIndices[i * 2 + 1] + 1).trim()));
        }

        return listItems;
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
     * @return Whether the expression was indeed an assignment.
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

        InvalidSyntaxException(String line) {
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
        InvalidSyntaxException(String message, String line, boolean probablyIs) {
            super(message);
            LOGGER.fine("Syntax error when parsing line: " + line + ", but it " + (probablyIs ? "found" : "not found") + " the expression");
            InvalidSyntaxException.probablyIs = probablyIs;
            if (probablyIs) {
                LOGGER.info("Syntax error when parsing line: " + line + ", but it found the expression.");
            }
        }
    }
}
