package cz.cvut.fel.horovtom.graphics.main;

import cz.cvut.fel.horovtom.logic.DFAAutomaton;
import cz.cvut.fel.horovtom.logic.abstracts.Automaton;

import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class Main {
    static Automaton current;

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
        if (current != null) {
            displayMenuLoaded();
            return;
        } else {

            System.out.println("Main menu:");
            System.out.println("No automaton loaded!");
            System.out.println("Select one option from the following:");
            System.out.println("1: Enter a new automaton");
            System.out.println("2: Use pre-defined automaton");
            System.out.println("3: Exit");

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
                    return;
                default:
                    System.err.println("Your choice was invalid!");
                    displayMenu();
                    return;
            }
        }
    }

    static void loadPredefinedAutomaton() {
        //TODO: IMPLEMENT
        System.err.println("Not implemented yet!");
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
        //TODO: IMPLEMENT
        System.err.println("Not implemented yet!");
        userCreateAutomaton();
    }

    static void userCreateNFA() {
        //TODO: IMPLEMENT
        System.err.println("Not implemented yet!");
        userCreateAutomaton();
    }

    static void userCreateDFA() {
        current = new DFAAutomaton();
    }

    static void displayMenuLoaded() {
        System.out.println("Automaton loaded: ");
        System.out.println(current.getAutomatonTablePlainText());
        System.out.println("What do you want to do:");
        System.out.println("1: Check if a word is in L");
        System.out.println("2: Get automaton in string");
        System.out.println("3: Reduce");
        System.out.println("4: Delete this automaton");
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
                System.out.println("Are you sure? (Y/N): ");
                char res;
                res = sc.next().charAt(0);
                if (res == 'Y' || res == 'y') {
                    current = null;
                    displayMenu();
                    return;
                }
                break;
            default:
                System.err.println("Your choice was invalid!");
                displayMenuLoaded();
                return;
        }


    }

    static void reduceIt() {
        //TODO: IMPLEMENT
        System.err.println("Not implemented yet!");
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
                System.out.println(current.getAutomatonTablePlainText());
                break;
            case 2:
                System.out.println(current.getAutomatonTableHTML());
                break;
            case 3:
                System.out.println(current.getAutomatonTableTEX());
                break;
            case 4:
                System.out.println(current.getAutomatonTIKZ());
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
