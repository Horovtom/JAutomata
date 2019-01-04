package cz.cvut.fel.horovtom.jasl.interpreter;

import java.util.logging.Logger;

class ParsingException extends Exception {
    private static final Logger LOGGER = Logger.getLogger(SyntaxException.class.getName());

    ParsingException(String message) {
        super(message);
        LOGGER.fine("Parsing exception with message: " + message);
    }

}
