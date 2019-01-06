package cz.cvut.fel.horovtom.jasl.interpreter;


import java.util.logging.Logger;

public enum Layout {
    DOT, CIRCO, NEATO, TWOPI;


    private static final Logger LOGGER = Logger.getLogger(Layout.class.getName());

    /**
     * This function will return layout string from layout value
     */
    public String getLayoutString() {
        String layout;
        if (this == Layout.DOT) {
            layout = "dot";
        } else if (this == Layout.CIRCO) {
            layout = "circo";
        } else if (this == Layout.NEATO) {
            layout = "neato";
        } else if (this == Layout.TWOPI) {
            layout = "twopi";
        } else {
            LOGGER.warning("Unknown layout! " + this);
            layout = "dot";
        }

        return layout;
    }

    public static Layout fromString(String layout) throws InvalidLayoutException {
        layout = layout.toLowerCase().trim();
        switch (layout) {
            case "dot":
                return DOT;
            case "circo":
                return CIRCO;
            case "neato":
                return NEATO;
            case "twopi":
                return TWOPI;
            default:
                throw new InvalidLayoutException();
        }
    }

    static class InvalidLayoutException extends Exception {
    }

}