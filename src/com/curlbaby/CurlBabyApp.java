package com.curlbaby;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CurlBabyApp {

    private static final UIManager uiManager = new UIManager();
    private static final HttpRequestHandler requestHandler = new HttpRequestHandler(uiManager);
    // Fixed: CommandProcessor constructor takes no arguments
    private static final CommandProcessor commandProcessor = new CommandProcessor();
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<String> commandHistory = new ArrayList<>();
    private static int historyIndex = 0;
    private static final String HISTORY_FILE = System.getProperty("user.home") + "/.curlbaby_history";

    public static void main(String[] args) {
        uiManager.displayWelcomeScreen();
        loadCommandHistory();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveCommandHistory();
        }));

        Console console = System.console();
        boolean supportsArrowKeys = (console != null);

        if (supportsArrowKeys) {
            uiManager.displayInfo("Tip: Use UP and DOWN arrow keys to navigate command history");
        }

        while (true) {
            try {
                uiManager.displayPrompt();
                String input;

                if (supportsArrowKeys) {
                    // Read input with potential arrow key handling
                    input = readLineWithArrows();
                } else {
                    // Fallback to regular scanner
                    input = scanner.nextLine().trim();
                }

                if (input.isEmpty()) {
                    continue;
                }

                // Handle exit commands
                if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                    uiManager.printExitMessage();
                    break;
                }

                // Add command to history if it's not a duplicate of the last command
                if (commandHistory.isEmpty() || !commandHistory.get(commandHistory.size() - 1).equals(input)) {
                    commandHistory.add(input);
                    historyIndex = commandHistory.size();
                }

                // Handle special commands directly
                if (input.toLowerCase().startsWith("history")) {
                    String[] parts = input.split("\\s+", 2);
                    String argument = parts.length > 1 ? parts[1] : "";

                    if (argument.equals("clear")) {
                        commandHistory.clear();
                        historyIndex = 0;
                        uiManager.displaySuccess("Command history cleared");
                    } else if (argument.isEmpty()) {
                        printHistory();
                    }
                    continue;
                }

                // Fixed: processCommand takes only one String argument (the full input)
                commandProcessor.processCommand(input);

            } catch (Exception e) {
                uiManager.displayError("Unexpected error: " + e.getMessage());
                System.err.println("Stack trace:");
                e.printStackTrace();
            }
        }
    }

    private static String readLineWithArrows() {
        try {
            StringBuilder buffer = new StringBuilder();

            while (true) {
                int c = System.in.read();

                if (c == '\n' || c == '\r') {
                    System.out.println();
                    break;
                }

                if (c == 127 || c == 8) {
                    if (buffer.length() > 0) {
                        buffer.deleteCharAt(buffer.length() - 1);
                        System.out.print("\b \b");
                    }
                    continue;
                }

                if (c == 27) {
                    if (System.in.available() > 0 && System.in.read() == 91) { // [
                        if (System.in.available() > 0) {
                            int arrowType = System.in.read();

                            if (arrowType == 65 && !commandHistory.isEmpty()) { // UP
                                if (historyIndex > 0) {
                                    historyIndex--;
                                    clearLine(buffer.length());
                                    buffer = new StringBuilder(commandHistory.get(historyIndex));
                                    System.out.print(buffer.toString());
                                }
                            } else if (arrowType == 66 && !commandHistory.isEmpty()) { // DOWN
                                if (historyIndex < commandHistory.size() - 1) {
                                    historyIndex++;
                                    clearLine(buffer.length());
                                    buffer = new StringBuilder(commandHistory.get(historyIndex));
                                    System.out.print(buffer.toString());
                                } else if (historyIndex == commandHistory.size() - 1) {
                                    historyIndex++;
                                    clearLine(buffer.length());
                                    buffer = new StringBuilder();
                                }
                            }
                        }
                    }
                    continue;
                }

                // Regular character - add to buffer and echo
                if (c >= 32 && c < 127) { // Printable ASCII
                    buffer.append((char) c);
                    System.out.print((char) c);
                }
            }

            return buffer.toString().trim();

        } catch (IOException e) {
            // Fallback to scanner if any error occurs
            try {
                if (scanner.hasNextLine()) {
                    return scanner.nextLine().trim();
                }
            } catch (Exception ex) {
                // If scanner also fails, return empty string
            }
            return "";
        }
    }

    // Helper method to clear the current line
    private static void clearLine(int length) {
        // Move cursor to beginning, print spaces, and move back again
        System.out.print("\r");
        uiManager.displayPrompt();
        for (int i = 0; i < length; i++) {
            System.out.print(" ");
        }
        System.out.print("\r");
        uiManager.displayPrompt();
    }

    private static void loadCommandHistory() {
        File historyFile = new File(HISTORY_FILE);
        if (historyFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        commandHistory.add(line);
                    }
                }
                historyIndex = commandHistory.size();
            } catch (IOException e) {
                uiManager.displayWarning("Could not load command history: " + e.getMessage());
            }
        }
    }

    private static void saveCommandHistory() {
        try {
            File historyFile = new File(HISTORY_FILE);
            if (!historyFile.exists()) {
                historyFile.createNewFile();
            }

            // Keep only the last 100 commands
            List<String> historyToSave = commandHistory;
            if (commandHistory.size() > 100) {
                historyToSave = commandHistory.subList(commandHistory.size() - 100, commandHistory.size());
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile))) {
                for (String cmd : historyToSave) {
                    writer.write(cmd);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving command history: " + e.getMessage());
        }
    }

    private static void printHistory() {
        if (commandHistory.isEmpty()) {
            uiManager.displayInfo("Command history is empty");
            return;
        }

        System.out.println("\n" + uiManager.getBoldYellow() + "Command History:" + uiManager.getReset());
        for (int i = 0; i < commandHistory.size(); i++) {
            System.out.printf("  %3d  %s\n", i + 1, commandHistory.get(i));
        }
    }
}
