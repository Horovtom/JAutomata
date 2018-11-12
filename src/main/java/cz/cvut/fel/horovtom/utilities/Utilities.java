package cz.cvut.fel.horovtom.utilities;

import java.io.File;
import java.io.IOException;

public class Utilities {

    private Utilities() {
    }

    /**
     * This function will create and return temporary file.
     */
    public static File createTempFile() {
        try {
            File tempFile = File.createTempFile("test-", "");
            tempFile.deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            System.err.println("Could not create temporary file!");
            e.printStackTrace();
            return null;
        }
    }
}
