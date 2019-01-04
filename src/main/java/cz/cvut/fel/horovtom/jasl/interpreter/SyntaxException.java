package cz.cvut.fel.horovtom.jasl.interpreter;

import java.util.logging.Logger;

class SyntaxException extends Exception {
    private static final Logger LOGGER = Logger.getLogger(SyntaxException.class.getName());

    SyntaxException(String message) {
        super(message);
        LOGGER.fine("Syntax error when parsing. Message: " + message);
    }

}