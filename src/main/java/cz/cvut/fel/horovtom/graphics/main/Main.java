package cz.cvut.fel.horovtom.graphics.main;

import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.logic.ENFAAutomaton;
import cz.cvut.fel.horovtom.logic.NFAAutomaton;
import cz.cvut.fel.horovtom.logic.Automaton;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class Main {
    private static Automaton current;

    public static void main(String[] args) throws IOException {
        Handler fh = new FileHandler("LOG.log");
        Logger logger = Logger.getLogger("");
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
        logger.addHandler(fh);
        displayMenu();
    }

    static void displayMenu() {
        System.out.println("-----------");
        if (current != null) {
            displayMenuLoaded();
            return;
        } else {

            System.out.println("Main menu:");
            System.out.println("No automaton loaded!");
            System.out.println("Select one option from the following:");
            System.out.println("1: Enter a new automaton");
            System.out.println("2: Use pre-defined automaton");
            System.out.println("3: Load automaton from file");
            System.out.println("4: Exit");

            System.out.println("Your choice: ");
            int choice;
            Scanner sc = new Scanner(System.in);
            choice = sc.nextInt();
            switch (choice) {
                case 1:
                    userCreateAutomaton();
                    break;
                case 2:
                    loadPredefinedAutomaton();
                    break;
                case 3:
                    loadAutomaton();
                    break;
                case 4:
                    return;
                default:
                    System.err.println("Your choice was invalid!");
                    displayMenu();
                    return;
            }
        }
    }

    private static void loadAutomaton() {
        JFileChooser jfc = new JFileChooser();
        jfc.setCurrentDirectory(new File(System.getProperty("user.home")));
        jfc.setDialogTitle("Load automaton");
        jfc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().matches(".*.csv") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "CSV file";
            }
        });
        JFrame frame = new JFrame("Save automaton");
        frame.setFocusable(true);
        frame.setVisible(true);
        frame.requestFocus();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        int returnVal = jfc.showSaveDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fileToSave = jfc.getSelectedFile();
            if (!fileToSave.getName().contains(".")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }
            System.out.println("Attempting to load from: " + fileToSave.getAbsolutePath());
            current = Automaton.importFromCSV(fileToSave);
        } else if (returnVal == JFileChooser.ERROR_OPTION) {
            System.err.println("Unexpected error ocurred, while opening the save dialog!");
        }
        frame.dispose();
        displayMenu();
    }

    static void loadPredefinedAutomaton() {
        System.out.println("Which predefined automaton do you want to create?");
        System.out.println("1: DFA1 - w starts and ends with the same character\n" +
                "2: DFA2 - w contains \"0.12 -6.38 0.12 0 213.002 -6.38 213.002\" as a substring\n" +
                "3: DFA3 - w = α*\n" +
                "4: NFA1 - w begins and ends with 'a'\n" +
                "5: NFA2 - w satisfies: second character is 'a', and the last but one character is ’b’ and |w| ≥ 3 or w = ε\n" +
                "6: NFA3 - w is described by regular expression: (ba)*c*a(bc)*\n" +
                "7: NFA4 - w contains only '\\alpha'\n" +
                "8: ENFA1 - w ∈ {a, b}");
        Scanner sc = new Scanner(System.in);
        switch (sc.nextInt()) {
            case 1:
                try {
                    InputStream in = Objects.requireNonNull(Main.class.getClassLoader().getResource("predefined/DFA1.txt")).openStream();
                    current = new DFAAutomaton(in);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                current = Automaton.importFromCSV(
                        new File(Objects.requireNonNull(Main.class.getClassLoader().getResource("predefined/DFA2.csv")).getFile()));
                break;
            case 3:
                current = Automaton.importFromCSV(
                        new File(Objects.requireNonNull(Main.class.getClassLoader().getResource("predefined/DFA3.csv")).getFile()));
                break;
            case 4:
                current = Automaton.importFromCSV(
                        new File(Objects.requireNonNull(Main.class.getClassLoader().getResource("predefined/NFA1.csv")).getFile()));
                break;
            case 5:
                current = Automaton.importFromCSV(
                        new File(Objects.requireNonNull(Main.class.getClassLoader().getResource("predefined/NFA2.csv")).getFile()));
                break;
            case 6:
                current = Automaton.importFromCSV(
                        new File(Objects.requireNonNull(Main.class.getClassLoader().getResource("predefined/NFA3.csv")).getFile()));
                break;
            case 7:
                current = Automaton.importFromCSV(
                        new File(Objects.requireNonNull(Main.class.getClassLoader().getResource("predefined/NFA4.csv")).getFile()));
                break;
            case 8:
                current = Automaton.importFromCSV(
                        new File(Objects.requireNonNull(Main.class.getClassLoader().getResource("predefined/ENFA1.csv")).getFile()));
                break;
            default:
                System.err.println("Invalid choice");
        }

        displayMenu();
    }

    static void userCreateAutomaton() {
        System.out.println("Which type of automaton do you want to create?");
        System.out.println("1: DFA");
        System.out.println("2: NFA");
        System.out.println("3: e-NFA");
        System.out.println("4: Back");
        System.out.println("Your choice: ");
        int choice;
        Scanner sc = new Scanner(System.in);
        choice = sc.nextInt();
        switch (choice) {
            case 1:
                userCreateDFA();
                break;
            case 2:
                userCreateNFA();
                break;
            case 3:
                userCreateENFA();
                break;
            case 4:
                break;
            default:
                System.err.println("Your choice was invalid!");
                userCreateAutomaton();
                return;
        }

        displayMenu();
    }

    static void userCreateENFA() {
        current = new ENFAAutomaton();

        displayMenu();
    }

    static void userCreateNFA() {
        current = new NFAAutomaton();

        displayMenu();
    }

    static void userCreateDFA() {
        current = new DFAAutomaton();
    }

    static void displayMenuLoaded() {
        System.out.println("Automaton loaded: ");
        System.out.println(current.exportToString().getPlainText());
        System.out.println("What do you want to do:");
        System.out.println("1: Check if a word is in L");
        System.out.println("2: Get automaton in string");
        System.out.println("3: Reduce");
        System.out.println("4: Renaming");
        System.out.println("5: Operations");
        System.out.println("6: Export to CSV");
        System.out.println("7: Delete this automaton");
        System.out.println("8: Exit");
        System.out.println("Your choice: ");
        int choice;
        Scanner sc = new Scanner(System.in);
        choice = sc.nextInt();
        switch (choice) {
            case 1:
                isWordInL();
                break;
            case 2:
                getInString();
                break;
            case 3:
                reduceIt();
                break;
            case 4:
                System.out.println("1: State");
                System.out.println("2: Letter");
                choice = sc.nextInt();
                String original, newName;
                if (choice == 1) {
                    System.out.println("Input original and new name separated by spaces: ");
                    original = sc.next();
                    newName = sc.next();
                    current.renameState(original, newName);
                } else if (choice == 2) {
                    System.out.println("Input original and new name separated by spaces: ");
                    original = sc.next();
                    newName = sc.next();
                    current.renameLetter(original, newName);
                } else {
                    System.err.println("Invalid choice!");
                }

                displayMenu();
                break;
            case 5:
                displayMenuOperations();
                break;
            case 6:
                saveAutomaton();
                break;
            case 7:
                System.out.println("Are you sure? (Y/N): ");
                char res;
                res = sc.next().charAt(0);
                if (res == 'Y' || res == 'y') {
                    current = null;
                    displayMenu();
                    return;
                }
                displayMenu();
                break;
            case 8:
                return;
            default:
                System.err.println("Your choice was invalid!");
                displayMenuLoaded();
                return;
        }
    }

    private static void displayMenuOperations() {
        System.out.println("--");
        System.out.println("1: Kleene star");

        Scanner sc = new Scanner(System.in);
        int choice = sc.nextInt();
        if (choice == 1) {
            current = current.getKleene();
        }
        displayMenu();
    }

    private static void saveAutomaton() {
        JFileChooser jfc = new JFileChooser();
        jfc.setCurrentDirectory(new File(System.getProperty("user.home")));
        jfc.setDialogTitle("Save automaton");
        jfc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().matches(".*.csv") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "CSV file";
            }
        });
        JFrame frame = new JFrame("Save automaton");
        frame.setFocusable(true);
        frame.setVisible(true);
        frame.requestFocus();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        int returnVal = jfc.showSaveDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fileToSave = jfc.getSelectedFile();
            if (!fileToSave.getName().contains(".")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }
            System.out.println("Attempting to save to: " + fileToSave.getAbsolutePath());
            current.exportToCSV(fileToSave);
        } else if (returnVal == JFileChooser.ERROR_OPTION) {
            System.err.println("Unexpected error ocurred, while opening the save dialog!");
        }
        frame.dispose();
        displayMenu();
    }

    static void reduceIt() {
        current = current.getReduced();
        displayMenu();
    }

    static void getInString() {
        System.out.println("In which format do you want to get the automaton?");
        System.out.println("1: Plain text");
        System.out.println("2: HTML");
        System.out.println("3: TEX");
        System.out.println("4: TIKZ");
        System.out.println("5: Back");
        System.out.println("Your choice: ");
        int choice;
        Scanner sc = new Scanner(System.in);
        choice = sc.nextInt();
        switch (choice) {
            case 1:
                System.out.println(current.exportToString().getPlainText());
                break;
            case 2:
                System.out.println(current.exportToString().getHTML());
                break;
            case 3:
                System.out.println(current.exportToString().getTEX());
                break;
            case 4:
                System.out.println(current.exportToString().getTIKZ());
                break;
            case 5:
                break;
            default:
                System.err.println("Your choice was invalid!");
                getInString();
                return;
        }


        displayMenu();
    }

    static void isWordInL() {
        System.out.println("Input a word with letters separated by spaces to check, (two blank lines for Back): ");
        String input;
        boolean blank = false;

        Scanner sc = new Scanner(System.in);
        while (true) {
            input = sc.nextLine();
            if (input.equals("")) {
                if (blank) break;
                else blank = true;
            } else blank = false;

            StringTokenizer st = new StringTokenizer(input);
            String[] in = new String[st.countTokens()];
            for (int i = 0; i < in.length; i++) {
                in[i] = st.nextToken();
            }
            System.out.println(current.acceptsWord(in) ? "Accepts" : "Does not accept");
        }

        displayMenu();
    }
}
