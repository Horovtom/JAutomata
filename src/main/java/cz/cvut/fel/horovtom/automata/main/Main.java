package cz.cvut.fel.horovtom.automata.main;

import cz.cvut.fel.horovtom.automata.ui.ConsoleMenu;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class Main {


    public static void main(String[] args) throws IOException {
        Handler fh = new FileHandler("LOG.log");
        Logger logger = Logger.getLogger("");
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        logger.addHandler(fh);

        ConsoleMenu cm = new ConsoleMenu();
        cm.displayMenu();
    }
}
