package com.curlbaby;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConsoleReader {

    private final CommandHistory history;
    private final UIManager uiManager;
    private int historyIndex = -1;

    private static final String CLEAR_LINE = "\u001b[2K";
    private static final String RETURN_TO_LINE_START = "\r";

    private static final int ARROW_PREFIX1 = 27;
    private static final int ARROW_PREFIX2 = 91;
    private static final int UP_ARROW = 65;
    private static final int DOWN_ARROW = 66;
    private static final int RIGHT_ARROW = 67;
    private static final int LEFT_ARROW = 68;
    private static final int BACKSPACE = 127;
    private static final int ENTER = 10;
    private static final int CTRL_C = 3;

    private final BlockingQueue<Integer> keyPressQueue = new LinkedBlockingQueue<>();
    private volatile boolean isReading = false;

    public ConsoleReader(UIManager uiManager) {
        this.history = new CommandHistory();
        this.uiManager = uiManager;
    }

    public void startKeyListener() {
        Thread keyListenerThread = new Thread(() -> {
            try {
                // Check if we're on Windows or Unix-like system
                String os = System.getProperty("os.name").toLowerCase();

                if (!os.contains("win")) {
                    // Unix/Linux/Mac - enable raw mode
                    String[] rawCmd = {"/bin/sh", "-c", "stty raw -echo </dev/tty"};
                    Runtime.getRuntime().exec(rawCmd).waitFor();
                }

                while (isReading) {
                    try {
                        int key = System.in.read();
                        if (key != -1) {
                            keyPressQueue.put(key);

                            // Handle Ctrl+C
                            if (key == CTRL_C) {
                                System.out.println();
                                System.out.println("^C");
                                System.exit(0);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading input: " + e.getMessage());
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in key listener: " + e.getMessage());
            } finally {
                resetTerminal();
            }
        });

        keyListenerThread.setDaemon(true);
        isReading = true;
        keyListenerThread.start();
    }

    public void stopKeyListener() {
        isReading = false;
        resetTerminal();
    }

    private void resetTerminal() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (!os.contains("win")) {
                // Reset terminal mode on Unix-like systems
                String[] resetCmd = {"/bin/sh", "-c", "stty sane </dev/tty"};
                Runtime.getRuntime().exec(resetCmd).waitFor();
            }
        } catch (Exception e) {
            System.err.println("Failed to reset terminal: " + e.getMessage());
        }
    }

    public String readLine() {
        // Check if we're on Windows - use simple Scanner fallback
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return readLineSimple();
        }

        startKeyListener();

        StringBuilder buffer = new StringBuilder();
        int cursorPosition = 0;
        historyIndex = -1; // Reset history index

        uiManager.displayPrompt();

        try {
            while (true) {
                Integer key = keyPressQueue.take();

                if (key == ENTER) {
                    System.out.println();
                    break;
                } else if (key == BACKSPACE) {
                    if (cursorPosition > 0) {
                        buffer.deleteCharAt(cursorPosition - 1);
                        cursorPosition--;
                        // Redraw line
                        System.out.print(CLEAR_LINE + RETURN_TO_LINE_START);
                        uiManager.displayPrompt();
                        System.out.print(buffer.toString());
                        // Move cursor to correct position
                        if (cursorPosition < buffer.length()) {
                            System.out.print("\u001b[" + (buffer.length() - cursorPosition) + "D");
                        }
                    }
                } else if (key == ARROW_PREFIX1) {
                    // Handle arrow keys
                    Integer prefix2 = keyPressQueue.take();
                    if (prefix2 == ARROW_PREFIX2) {
                        Integer arrowCode = keyPressQueue.take();

                        if (arrowCode == UP_ARROW) {
                            String previousCommand = getPreviousFromHistory();
                            if (previousCommand != null) {
                                // Clear current line and show previous command
                                System.out.print(CLEAR_LINE + RETURN_TO_LINE_START);
                                uiManager.displayPrompt();
                                buffer = new StringBuilder(previousCommand);
                                System.out.print(buffer.toString());
                                cursorPosition = buffer.length();
                            }
                        } else if (arrowCode == DOWN_ARROW) {
                            String nextCommand = getNextFromHistory();
                            // Clear current line and show next command (or empty)
                            System.out.print(CLEAR_LINE + RETURN_TO_LINE_START);
                            uiManager.displayPrompt();
                            buffer = new StringBuilder(nextCommand != null ? nextCommand : "");
                            System.out.print(buffer.toString());
                            cursorPosition = buffer.length();
                        } else if (arrowCode == LEFT_ARROW) {
                            if (cursorPosition > 0) {
                                cursorPosition--;
                                System.out.print("\u001b[1D");  // Move cursor left
                            }
                        } else if (arrowCode == RIGHT_ARROW) {
                            if (cursorPosition < buffer.length()) {
                                cursorPosition++;
                                System.out.print("\u001b[1C");  // Move cursor right
                            }
                        }
                    }
                } else {
                    char c = (char) key.intValue();
                    if (c >= 32 && c < 127) {  // Printable ASCII
                        if (cursorPosition == buffer.length()) {
                            // Append at the end
                            buffer.append(c);
                            System.out.print(c);
                        } else {
                            // Insert at cursor position
                            buffer.insert(cursorPosition, c);
                            // Redraw from cursor position
                            System.out.print(buffer.substring(cursorPosition));
                            // Move cursor back to correct position
                            int charsToMoveBack = buffer.length() - cursorPosition - 1;
                            if (charsToMoveBack > 0) {
                                System.out.print("\u001b[" + charsToMoveBack + "D");
                            }
                        }
                        cursorPosition++;
                    }
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Input reading interrupted: " + e.getMessage());
        } finally {
            stopKeyListener();
        }

        String result = buffer.toString();
        if (!result.isEmpty()) {
            history.addCommand(result);
        }
        return result;
    }

    /**
     * Simple fallback for Windows systems
     */
    private String readLineSimple() {
        uiManager.displayPrompt();
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        String input = scanner.nextLine();
        if (!input.isEmpty()) {
            history.addCommand(input);
        }
        return input;
    }

    private String getPreviousFromHistory() {
        java.util.List<String> allCommands = history.getAllCommands();
        if (allCommands.isEmpty()) {
            return null;
        }

        if (historyIndex == -1) {
            historyIndex = allCommands.size() - 1;
        } else if (historyIndex > 0) {
            historyIndex--;
        }

        return allCommands.get(historyIndex);
    }

    private String getNextFromHistory() {
        java.util.List<String> allCommands = history.getAllCommands();
        if (allCommands.isEmpty() || historyIndex == -1) {
            return null;
        }

        if (historyIndex < allCommands.size() - 1) {
            historyIndex++;
            return allCommands.get(historyIndex);
        } else {
            historyIndex = -1; // Reset to allow new input
            return null; // Return null to clear the line
        }
    }

    public CommandHistory getHistory() {
        return history;
    }
}
