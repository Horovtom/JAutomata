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
     * @throws SyntaxException Will be thrown if there was some invalid syntax in the expression.
     */
    public String parseLine(String line) throws SyntaxException {
        LOGGER.info("Parsing line: " + line);
        LOGGER.fine("Parsing line: " + line + " as an assignment...");
        try {
            parseAssignment(line);
            return "";
        } catch (ParsingException ignored) {
        }

        LOGGER.fine("It was not an assignment... Trying to parse it as an expression...");
        return parseExpression(line);

    }

    /**
     * This will try to parse the line as an assignment. It will throw {@link SyntaxException} if there was any syntax error
     *
     * @param line Line to be parsed
     */
    private void parseAssignment(String line) throws SyntaxException, ParsingException {
        if (line.indexOf('=') == -1)
            throw new ParsingException("The line is not an assignment, as it is missing the '=' operator");
        String[] tokens = getNextToken(line, '=');

        String toWhat = tokens[0].trim();
        if (toWhat.length() == 0) throw new SyntaxException("You have to assign to a variable");
        if (toWhat.charAt(0) != '$') throw new SyntaxException("Variables must start with '$'");

        try {
            Object result = getExpressionResult(tokens[1].trim());
            variables.put(toWhat, result);
        } catch (SyntaxException e) {
            throw new SyntaxException("Invalid right side of an assignment!");
        }
    }

    /**
     * This will try to parse the line as an expression. It will throw {@link SyntaxException} if it is not an expression.
     *
     * @param line Line to be parsed
     * @return Answer to the expression
     */
    private String parseExpression(String line) throws SyntaxException {
        try {
            Object o = getExpressionResult(line);
            if (o == null) return "";
            return o.toString();
        } catch (NullPointerException | SyntaxException e) {
            throw new SyntaxException(e.getMessage());
        }
    }

    /**
     * This will try to parse the line as an expression. It will throw {@link SyntaxException} if it is not valid.
     *
     * @param expression Line to be parsed
     * @return evaluation result
     */
    private Object getExpressionResult(String expression) throws SyntaxException {
        // Check, whether it is a variable by itself
        if (variables.containsKey(expression)) {
            return variables.get(expression);
        }

        if (expression.equals("")) return "";

        // Check, whether it is a call to variable function
        try {
            return parseVarFunction(expression);
        } catch (ParsingException ignored) {
        }

        try {
            return parseList(expression);
        } catch (ParsingException ignored) {
        }

        try {
            return parseCommand(expression);
        } catch (ParsingException ignored) {
        }

        return expression;
    }

    /**
     * This will try to parse expression as a command (meaning constructor) of object. It will throw {@link SyntaxException} if it is not valid
     * e.g.: Automaton($table) e.t.c.
     *
     * @param expression Expression to be parsed
     * @return A resulting object of the command call
     * @throws SyntaxException If there any syntax error
     * @throws ParsingException If it was not a command
     */
    private Object parseCommand(String expression) throws SyntaxException, ParsingException {
        int[] insideIndices = extractFromBrackets(expression);
        Object[] eval = new Object[insideIndices.length / 2];
        for (int i = 0; i < insideIndices.length; i += 2) {
            eval[i / 2] = getExpressionResult(expression.substring(insideIndices[i], insideIndices[i + 1] + 1));
        }
        Object res;
        if (expression.startsWith("Automaton(")) {
            // DFA C-TOR
            res = getAutomaton(eval);
        } else if (expression.startsWith("fromCSV(")) {
            // IMPORT AUTOMATON FROM CSV
            if (eval.length != 1)
                throw new SyntaxException("fromCSV function takes 1 arguments!");

            String path = (String) eval[0];
            LOGGER.info("Importing Automaton from CSV file at: " + path);
            try {
                res = Automaton.importFromCSV(new File(path));
                if (res == null) throw new SyntaxException("Corrupted CSV file.");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                throw new SyntaxException("Could not find file: " + path);
            } catch (Automaton.InvalidAutomatonDefinitionException e) {
                throw new SyntaxException("Corrupted .csv file!");
            }
        } else if (expression.startsWith("getExample()")) {
            // GETTING SAMPLE AUTOMATON
            res = getExpressionResult("Automaton({{a, b},{>, 0, 1, {2,3}},{>, 1, {}, {1, 4}},{<>, 2, {}, 0},{<, 3, 3, 3},{4,4,2}})");
        } else if (expression.startsWith("fromRegex(")) {
            // IMPORT FROM REGEX
            if (eval.length != 1)
                throw new SyntaxException("fromRegex function takes 1 argument.");
            res = FromRegexConverter.getAutomaton((String) eval[0]);
        } else if (expression.equals("getTikzIncludes()")) {
            res = tikzIncludes;
        } else if (expression.startsWith("execute(")) {
            if (eval.length != 1 || !(eval[0] instanceof String))
                throw new SyntaxException("Execute takes 1 argument, the path to the file to be executed in a string.");
            String path = (String) eval[0];
            FileInterpreter fi = new FileInterpreter(this, path);
            fi.start();
            res = null;
        } else {
            throw new ParsingException("Unknown command to parse");
        }

        // We have to check, whether there is some chaining:
        String commandToChain = null;
        if (insideIndices.length > 0) {
            // It had some arguments
            if (insideIndices[insideIndices.length - 1] < expression.length() - 2)
                commandToChain = expression.substring(insideIndices[insideIndices.length - 1] + 2);
        } else {
            // It is without arguments
            // Find end of function:
            int start = expression.indexOf("()") + 2;
            if (start < expression.length()) {
                if (expression.charAt(start) != '.')
                    throw new SyntaxException("Unexpected token at position: " + (start) + ". '.' expected.");
                commandToChain = expression.substring(start);
            }
        }

        if (commandToChain != null) {
            res = chainOnTemp(res, commandToChain);
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
    private Object callAutomatonMemberFunction(Automaton a, String functionName, Object[] arguments) throws SyntaxException {
        Object argument;
        switch (functionName) {
            case "equals":
                if (arguments.length != 1)
                    throw new SyntaxException("Call to equals should have 1 argument.");
                argument = arguments[0];
                if (!(argument instanceof Automaton)) return false;
                Automaton other = (Automaton) argument;
                return a.equals(other);

            case "reduce":
                if (arguments.length > 0)
                    throw new SyntaxException("Call to reduce should not have any arguments.");
                return a.getReduced();

            case "accepts":
                if (arguments.length == 0) return a.acceptsWord("");
                if (arguments.length != 1)
                    throw new SyntaxException("Call to accepts should have 1 or 0 arguments");

                argument = arguments[0];
                if (argument instanceof String) {
                    String arg = (String) argument;
                    return a.acceptsWord(arg);
                } else if (argument instanceof ArrayList) {
                    ArrayList<String> arg = (ArrayList<String>) argument;
                    return a.acceptsWord(arg);
                } else {
                    throw new SyntaxException("Invalid class of argument of accepts: " + argument.getClass());
                }

            case "toCSV":
                if (arguments.length != 1)
                    throw new SyntaxException("Invalid number of arguments: " + arguments.length + " for toCSV member function.");
                String path = (String) arguments[0];
                LOGGER.info("Trying to export to CSV to path: " + path);
                a.exportToCSV(new File(path));
                return null;
            case "toTikz":
                if (arguments.length > 1)
                    throw new SyntaxException("Invalid number of arguments: " + arguments.length + " expected 1.");

                try {
                    if (arguments.length == 1) {
                        return GraphvizAPI.toTikz(a, Layout.fromString((String) arguments[0]));
                    } else {
                        return GraphvizAPI.toTikz(a);
                    }
                } catch (IOException e) {
                    throw new SyntaxException("IOException when converting to TEX");
                } catch (Layout.InvalidLayoutException e) {
                    throw new SyntaxException("Invalid layout specified! " + arguments[0]);
                }


            case "toPNG":
                return convertToPng(a, arguments);

            case "toTexTable":
                return a.exportToString().getTEX();

            case "toRegex":
                return a.getRegex();

            case "toDot":
                if (arguments.length == 1) {
                    if (!(arguments[0] instanceof String)) throw new SyntaxException(
                            "Invalid type of argument: " + arguments[0].getClass() + ". toDot expects String.");
                    try {
                        return GraphvizAPI.toFormattedDot(a, Layout.fromString((String) arguments[0]));
                    } catch (Layout.InvalidLayoutException e) {
                        throw new SyntaxException("Layout has to be one of the valid layouts e.g. 'neato'.");
                    }
                } else if (arguments.length > 1) {
                    throw new SyntaxException("toDot expects at most one argument.");
                } else {
                    return GraphvizAPI.toFormattedDot(a);
                }
            case "toSimpleDot":
                return GraphvizAPI.toDot(a);

            case "union":
                if (arguments.length != 1)
                    throw new SyntaxException("Union expects exactly one argument.");

                if (!(arguments[0] instanceof Automaton))
                    throw new SyntaxException("Invalid type of argument: " + arguments[0].getClass());

                return Automaton.getUnion(a, (Automaton) arguments[0]);

            case "intersection":
                if (arguments.length != 1)
                    throw new SyntaxException("Intersection expects exactly one argument.");

                if (!(arguments[0] instanceof Automaton))
                    throw new SyntaxException("Invalid type of argument: " + arguments[0].getClass());

                return Automaton.getIntersection(a, (Automaton) arguments[0]);

            case "complement":
                if (arguments.length != 0)
                    throw new SyntaxException("Complement expects no arguments.");

                return a.getComplement();

            case "concatenation":
                if (arguments.length != 1)
                    throw new SyntaxException("Concatenation expects exactly one argument.");

                if (!(arguments[0] instanceof Automaton))
                    throw new SyntaxException("Invalid type of argument: " + arguments[0].getClass());

                return Automaton.getConcatenation(a, (Automaton) arguments[0]);

            case "kleene":
                if (arguments.length != 0)
                    throw new SyntaxException("Kleene expects no arguments.");

                return a.getKleene();

            case "renameState":
                if (arguments.length != 2)
                    throw new SyntaxException("Renaming state takes 2 arguments.");

                if (!(arguments[0] instanceof String) || !(arguments[1] instanceof String))
                    throw new SyntaxException("Renaming state expects two strings as arguments.");

                renameState(a, (String) arguments[0], (String) arguments[1]);
                return null;

            case "renameTerminal":
                if (arguments.length != 2)
                    throw new SyntaxException("Renaming terminal takes 2 arguments.");

                if (!(arguments[0] instanceof String) || !(arguments[1] instanceof String))
                    throw new SyntaxException("Renaming terminal expects two strings as arguments.");

                renameTerminal(a, (String) arguments[0], (String) arguments[1]);
                return null;

            default:
                throw new SyntaxException("Unknown function call");
        }

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

    private Reader getReaderFromTable(ArrayList<Object> table) throws SyntaxException {
        try {
            // Create temporary file.
            File temp = Utilities.createTempFile();
            FileWriter writer;
            if (temp != null) {
                writer = new FileWriter(temp);
            } else throw new SyntaxException("Unable to create temporary file!");
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
                                if (size == 1)
                                    throw new SyntaxException("Invalid automaton definition. There was only I/O symbol on a line. State name needed.");
                                commasAtEnd = properLineLength - size;
                            } else {
                                writer.write(',' + elem);
                            }
                        } else {
                            throw new SyntaxException("Invalid automaton definition. Line lengths did not match.");
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

    private Object getAutomatonFromTable(Reader reader) throws SyntaxException {
        Automaton a;
        try {
            a = Automaton.importFromCSV(reader, ',');
        } catch (Automaton.InvalidAutomatonDefinitionException e) {
            throw new SyntaxException("Invalid automaton table.");
        }
        if (a == null) throw new SyntaxException("Invalid automaton table.");

        return a.getENFA();
    }

    private Object getAutomaton(Object[] eval) throws SyntaxException, ParsingException {
        if (eval.length == 1) {
            Object o = eval[0];
            if (o instanceof ArrayList) {
                Reader res = getReaderFromTable((ArrayList<Object>) o);
                return getAutomatonFromTable(res);
            }
        }
        throw new ParsingException("Unknown number of parameters...");

    }

    /**
     * This will extract string arguments from bracket pair. It will return the extracted string indices.
     * For example:
     * input: aab(2,{3,4,1}, ss(12)). This will return:
     * {4, 4, 6, 12, 15, 20}
     *
     * @return integer array with pairs of elements: {StartIndex, EndIndex}
     */
    private int[] extractFromBrackets(String toExtract) throws SyntaxException, ParsingException {
        return extractFromBrackets(toExtract, '(', ')', '{', '}');
    }

    /**
     * This will extract argument indices from bracket pair. It will return the extracted string indices.
     * Used by: {@link #extractFromBrackets(String)}, {@link #extractFromCurlyBrackets}
     */
    private int[] extractFromBrackets(String toExtract, char mainOpen, char mainClose, char otherOpen, char otherClose) throws SyntaxException, ParsingException {
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
                    throw new ParsingException("List in bracket had invalid closing bracket");
            } else {
                if (depth == 1 && !in) {
                    in = true;
                    returning.add(i);

                }
            }
        }

        if (depth > 0) {
            throw new SyntaxException("Unbalanced brackets.");
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
    private int[] extractFromCurlyBrackets(String toExtract) throws SyntaxException, ParsingException {
        return extractFromBrackets(toExtract, '{', '}', '(', ')');
    }

    /**
     * This will try to parse the expression as a variable function call. It will throw {@link SyntaxException} if it is not valid
     * e.g.: $a.reduce() or $b.accepts({2, 3, 4})
     *
     * @param expression Expression to be parsed
     * @return A result object if it is a variable function call
     * @throws SyntaxException if it is not a variable function call.
     */
    private Object parseVarFunction(String expression) throws SyntaxException, ParsingException {
        if (expression.charAt(0) != '$')
            throw new ParsingException("Variable names have to start with $");

        // Find variable name
        String[] tokens = getNextToken(expression, '.');
        String call = tokens[1];
        if (tokens[1].equals(""))
            throw new SyntaxException("Variable does not exist or member function of variable not specified");
        String varname = tokens[0];
        if (!variables.containsKey(varname)) throw new SyntaxException("Unknown variable");

        // Find function name
        tokens = getNextToken(tokens[1], '(');

        if (tokens[1].equals(""))
            throw new SyntaxException("Function calls have to end with parenthesis");
        String functionName = tokens[0];

        //Find arguments

        if (!tokens[1].endsWith(")"))
            throw new SyntaxException("Could not find closing parenthesis for function call");
        int[] argumentIndices = extractFromBrackets(call);

        Object[] arguments = new Object[argumentIndices.length / 2];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = getExpressionResult(call.substring(argumentIndices[i * 2], argumentIndices[i * 2 + 1] + 1));
        }
        Object res = callVarFunction(variables.get(varname), functionName, arguments);

        String toBeChained = null;
        if (argumentIndices.length == 0 && tokens[1].startsWith(")") && tokens[1].length() > 1 && tokens[1].charAt(1) == '.') {
            //Extract chained statement from no arguments call
            toBeChained = tokens[1].substring(1);
        } else if (argumentIndices.length > 0 && argumentIndices[argumentIndices.length - 1] + 2 < call.length()) {
            toBeChained = call.substring(argumentIndices[argumentIndices.length - 1] + 2);
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
    private Object chainOnTemp(Object temp, String command) throws SyntaxException, ParsingException {
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
     * This will attempt renaming of terminal of automaton a.
     *
     * @throws SyntaxException If the renaming was not successful.
     */
    private void renameTerminal(Automaton a, String from, String to) throws SyntaxException {
        if (!(a.renameLetter(from.trim(), to.trim())))
            throw new SyntaxException("An error occurred when renaming terminals. " +
                    "Maybe the original terminal has not been found or new terminal name already exists in the automaton.");
    }

    /**
     * This will attempt renaming of state of automaton a.
     *
     * @throws SyntaxException If the renaming was not successful.
     */
    private void renameState(Automaton a, String from, String to) throws SyntaxException {
        if (!(a.renameState(from.trim(), to.trim())))
            throw new SyntaxException("An error occurred when renaming states. " +
                    "Maybe the original state name has not been found or the target state name already exists in the automaton.");
    }

    /**
     * This converts an automaton to PNG image.
     *
     * @param a         Automaton to convert
     * @param arguments Arguments of the function call. First is the path to the PNG image. Second is optional and it is
     *                  the layout algorithm.
     */
    private Object convertToPng(Automaton a, Object[] arguments) throws SyntaxException {
        if (arguments.length == 0 || arguments.length > 2)
            throw new SyntaxException(
                    "Invalid number of arguments: " + arguments.length + ". toPNGImage expects 1 or 2 arguments.");

        if (!(arguments[0] instanceof String)) throw new SyntaxException(
                "Invalid type of argument: " + arguments[0].getClass() + ". toPNGImage expects String.");
        String p = (String) arguments[0];
        File f = Paths.get(p).toFile();


        if (f.isDirectory())
            throw new SyntaxException("Cannot write to file at: " + p);
        if (f.exists()) {
            LOGGER.info("Overwriting file at: " + p);
            if (!f.delete()) {
                throw new SyntaxException("Cannot overwrite file at: " + p);
            }
        }

        if (arguments.length > 1) {
            if (!(arguments[1] instanceof String)) throw new SyntaxException(
                    "Invalid type of argument: " + arguments[1].getClass() + ". toPNGImage expects String, String.");
            String layout = (String) arguments[1];
            layout = layout.trim();
            try {
                GraphvizAPI.toPNG(a, p, Layout.fromString(layout));
            } catch (Layout.InvalidLayoutException e) {
                throw new SyntaxException("Layout has to be one of the valid layouts e.g. 'neato'.");
            }
        } else {
            GraphvizAPI.toPNG(a, p);
        }
        return null;
    }

    /**
     * This will call specified function of string on specified arguments.
     */
    private Object callStringMemberFunction(String s, String functionName, Object[] arguments) throws SyntaxException {
        switch (functionName) {
            case "save":
                if (!(arguments.length == 1 && arguments[0] instanceof String)) throw new SyntaxException(
                        "save function needs path argument to be specified!");
                String path = (String) arguments[0];

                File f = new File(path);
                if (f.isDirectory()) throw new SyntaxException("Path does not lead to a file!");
                if (f.isFile()) {
                    // This means it exists, so we will append to it!
                    LOGGER.info("Appending to file: " + path);
                    try {
                        Files.write(Paths.get(path), s.getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        LOGGER.warning("IOException occurred when trying to append to file. " + e.getMessage());
                        throw new SyntaxException("I/O exception when trying to append to file: " + path);
                    }
                } else {
                    // Create and write:
                    try {
                        Files.write(Paths.get(path), s.getBytes());
                    } catch (IOException e) {
                        LOGGER.warning("IOException occurred when trying to write to file. " + e.getMessage());
                        throw new SyntaxException("I/O exception when trying to write to file: " + path);
                    }
                }

                break;

            default:
                throw new SyntaxException("Unknown function call on string");
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
    private Object callVarFunction(Object var, String functionName, Object[] arguments) throws SyntaxException, ParsingException {
        if (var instanceof Boolean) var = var.toString();
        if (var instanceof Automaton) {
            Automaton a = (Automaton) var;

            return callAutomatonMemberFunction(a, functionName, arguments);
        } else if (var instanceof String) {
            String s = (String) var;

            return callStringMemberFunction(s, functionName, arguments);
        } else {
            throw new ParsingException("Unknown function call");
        }
    }

    /**
     * This will try to parse the expression as a list. It will throw {@link SyntaxException} if it is not valid
     * e.g.: {a, b, c}
     *
     * @param expression Expression to be parsed into a list
     * @return A list of Objects if it is a list.
     * @throws SyntaxException if it is not a list
     */
    private JASLList parseList(String expression) throws SyntaxException, ParsingException {
        if (expression.charAt(0) != '{')
            throw new ParsingException("List cannot start with " + expression.charAt(0));
        int[] elemsIndices = extractFromCurlyBrackets(expression);
        if (elemsIndices.length == 0) return new JASLList();
        if (elemsIndices[elemsIndices.length - 1] != expression.length() - 2)
            throw new ParsingException("List does not have valid ending");
        JASLList listItems = new JASLList();
        int len = elemsIndices.length / 2;
        for (int i = 0; i < len; i++) {
            listItems.add(getExpressionResult(expression.substring(elemsIndices[i * 2], elemsIndices[i * 2 + 1] + 1).trim()));
        }

        return listItems;
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

    /**
     * This function will clear all variables stored in the interpreter.
     */
    public void clear() {
        variables.clear();
    }


}
