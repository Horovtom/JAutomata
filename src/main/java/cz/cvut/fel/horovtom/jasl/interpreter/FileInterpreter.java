package cz.cvut.fel.horovtom.jasl.interpreter;


import java.io.*;
import java.util.Arrays;
import java.util.logging.Logger;

public class FileInterpreter {
    private static final Logger LOGGER = Logger.getLogger(FileInterpreter.class.getName());

    private Interpreter interpreter;
    private File f;
    private Exception error = null;
    private OutputStream output = System.out;

    public FileInterpreter(Interpreter interpreter, String path) {
        initialize(interpreter, path);
    }

    public FileInterpreter(String path) {
        initialize(new Interpreter(), path);
    }

    private void initialize(Interpreter interpreter, String path) {
        this.interpreter = interpreter;
        LOGGER.info("Loading file interpreter on file: " + path);
        f = new File(path);
        if (!f.isFile()) {
            LOGGER.warning("Could not open file: " + path + "!");
            error = new IllegalArgumentException("Could not open file: " + path);
        } else {
            LOGGER.fine("Success opening file: " + path);
        }
    }

    /**
     * This function will set output to something else than the standard output.
     *
     * @param os Stream that we will write to instead of stdout.
     */
    public void setOutput(OutputStream os) {
        output = os;
    }

    /**
     * This function will return the exception thrown by the interpreter.
     */
    public Exception getException() {
        return error;
    }

    /**
     * This function will return interpreter used by this FileInterpreter.
     *
     * @return Interpreter with all variables set by this interpreter.
     */
    public Interpreter getInterpreter() {
        return interpreter;
    }

    /**
     * This will start the interpreting of the file. It will return true if it run successfully. False if there were any errors.
     * Details for the errors will be in the logger, or obtainable by getException command.
     *
     * @return True if the interpreter run successfully.
     */
    public boolean start() {
        if (error != null) {
            LOGGER.warning("Trying to run interpreter, that exited on error previously!");
            return false;
        }

        LOGGER.info("Starting interpreting process.");
        try (FileReader fr = new FileReader(f)) {
//            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            int lineNumber = 1;
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String o = interpreter.parseLine(line);
                    output.write(o.getBytes());
                } catch (SyntaxException e) {
                    String message = "Syntax error while reading line: " + lineNumber;
                    LOGGER.warning(message);
                    output.write(message.getBytes());
                    error = e;
                    e.printStackTrace();
                    return false;
                }
                lineNumber++;
            }
        } catch (IOException e) {
            LOGGER.warning("Error while reading file \n" + Arrays.toString(e.getStackTrace()));
            error = e;
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
