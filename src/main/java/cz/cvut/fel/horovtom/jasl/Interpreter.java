package cz.cvut.fel.horovtom.jasl;

import com.Ostermiller.util.CircularCharBuffer;
import cz.cvut.fel.horovtom.automata.logic.Automaton;
import cz.cvut.fel.horovtom.automata.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.automata.logic.NFAAutomaton;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class Interpreter {
    private static Logger LOGGER = Logger.getLogger(Interpreter.class.getName());

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
            //DFA constructor
            res = getDFA(eval);
        } else if (expression.startsWith("NFA(")) {
            res = getNFA(eval);
        } else if (expression.startsWith("ENFA(")) {
            res = getENFA(eval);
        } else if (expression.startsWith("texImage(")) {
            //TODO: IMPLEMENT CONVERSION TO TEX IMAGE
            LOGGER.severe("NOT IMPLEMENTED function texImage YET!");
            return null;
        } else if (expression.startsWith("texTable(")) {
            //TODO: IMPLEMENT CONVERSION TO TEX TABLE
            LOGGER.severe("Not implemented function texTable Yet!");
            return null;
        } else if (expression.startsWith("fromCSV(")) {
            if (eval.length != 1)
                throw new InvalidSyntaxException("fromCSV function takes 1 arguments!", expression, true);

            String path = (String) eval[0];
            LOGGER.info("Importing Automaton from CSV file at: " + path);
            try {
                return Automaton.importFromCSV(new File(path));
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                throw new InvalidSyntaxException("Could not find file: " + path, expression, true);
            }
        } else if (expression.startsWith("pngImage(")) {
            //TODO: Implement conversion to png image
            LOGGER.severe("Not implemented function pngImage Yet!");
            return null;
        } else if (expression.startsWith("getExample1()")) {
            return getExpressionResult("NFA({{a, b},{>, 0, 1, {2,3}},{>, 1, {}, {1, 4}},{<>, 2, {}, 0},{<, 3, 3, 3},{4,4,2}})");
        } else if (expression.startsWith("fromRegex(")) {
            //TODO: Implement conversion from regex
            LOGGER.severe("Not implemented function fromRegex Yet!");
            return null;
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

    private Reader getReaderFromTable(ArrayList<Object> table) {
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
        return callVarFunction(variables.get(varname), functionName, arguments);
    }

    /**
     * This will call function of a variable and return result as an Object
     *
     * @param varname      variable
     * @param functionName member function of the variable
     * @param arguments    Arguments of the function in an object array.
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
                if (arguments.length == 0) return a.acceptsWord("");
                if (arguments.length != 1)
                    throw new InvalidSyntaxException("Call to accepts should have 1 or 0 arguments", "", true);

                Object argument = arguments[0];
                if (argument instanceof String) {
                    String arg = (String) argument;
                    return a.acceptsWord(arg);
                } else if (argument instanceof ArrayList) {
                    ArrayList<String> arg = (ArrayList<String>) argument;
                    return a.acceptsWord(arg);
                }
            } else if (functionName.equals("toCSV")) {
                //TODO: Implement
                if (arguments.length != 1)
                    throw new InvalidSyntaxException("Invalid number of arguments: " + arguments.length + "for toCSV member function.", "", true);
                String path = (String) arguments[0];
                LOGGER.info("Trying to export to CSV to path: " + path);
                a.exportToCSV(new File(path));
                return null;

            } else if (functionName.equals("toTexImage")) {
                //TODO: Implement

            } else if (functionName.equals("toPNGImage")) {
                //TODO: Implement

            } else if (functionName.equals("toTexTable")) {
                //TODO: Implement
            } else if (functionName.equals("toRegex")) {
                //TODO: Implement
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
