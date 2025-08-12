package com.curlbaby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SimpleJsonEditor {

    private JsonFormatter formatter;
    private Scanner scanner;
    private int currentLine = 1; // Track current line position

    public SimpleJsonEditor() {
        this.formatter = new JsonFormatter();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Console-based vim-like JSON editor
     */
    public String editJson(String json) {
        try {
            String formattedJson = formatter.formatJson(json);
            List<String> lines = new ArrayList<>(Arrays.asList(formattedJson.split("\n")));
            this.currentLine = 1; // Start at line 1

            System.out.println("\n" + repeatString("=", 50));
            System.out.println("🔧 JSON EDITOR (vim-like commands)");
            System.out.println(repeatString("=", 50));
            System.out.println("Navigation: j/k (up/down), gg (first), G (last)");
            System.out.println("Edit: i (insert mode), r (replace mode), e (edit current line)");
            System.out.println("Control: :q (quit), :w (validate), :wq (save & quit)");
            System.out.println("Format: :format, :compact, :help");
            System.out.println(repeatString("=", 50));

            while (true) {
                displayJsonWithLineNumbers(lines, currentLine);

                System.out.printf("\n📝 Editor [Line %d/%d]> ", currentLine, lines.size());
                String command = scanner.nextLine().trim();

                // Handle navigation commands
                if (command.equals("j")) {
                    navigateDown(lines);

                } else if (command.equals("k")) {
                    navigateUp(lines);

                } else if (command.equals("gg")) {
                    currentLine = 1;
                    System.out.println("📍 Moved to first line");

                } else if (command.equals("G")) {
                    currentLine = lines.size();
                    System.out.println("📍 Moved to last line");

                } else if (command.equals("e")) {
                    editCurrentLine(lines);

                    // Handle vim-like insert and replace modes
                } else if (command.equals("i")) {
                    enterInsertMode(lines);

                } else if (command.equals("r")) {
                    enterReplaceMode(lines);

                } else if (command.equals(":q")) {
                    System.out.println("👋 Editor closed without saving");
                    return json; // Return original

                } else if (command.equals(":w")) {
                    String current = String.join("\n", lines);
                    if (formatter.isValidJson(current)) {
                        System.out.println("✅ JSON is valid!");
                    } else {
                        System.out.println("❌ JSON is invalid - please fix errors");
                    }

                } else if (command.equals(":wq")) {
                    String result = String.join("\n", lines);
                    if (formatter.isValidJson(result)) {
                        System.out.println("✅ JSON saved and editor closed");
                        return result;
                    } else {
                        System.out.println("❌ Cannot save invalid JSON. Fix errors first.");
                    }

                } else if (command.equals(":help")) {
                    showDetailedHelp();

                } else if (command.equals(":format")) {
                    String current = String.join("\n", lines);
                    if (formatter.isValidJson(current)) {
                        String reformatted = formatter.formatJson(current);
                        lines.clear();
                        lines.addAll(Arrays.asList(reformatted.split("\n")));
                        if (currentLine > lines.size()) {
                            currentLine = lines.size();
                        }
                        System.out.println("✅ JSON reformatted with proper indentation");
                    } else {
                        System.out.println("❌ Cannot format invalid JSON");
                    }

                } else if (command.equals(":compact")) {
                    String current = String.join("\n", lines);
                    if (formatter.isValidJson(current)) {
                        String compacted = formatter.compactJson(current);
                        lines.clear();
                        lines.add(compacted);
                        currentLine = 1;
                        System.out.println("✅ JSON compacted to single line");
                    } else {
                        System.out.println("❌ Cannot compact invalid JSON");
                    }

                } else if (command.matches("\\d+")) {
                    int targetLine = Integer.parseInt(command);
                    if (targetLine >= 1 && targetLine <= lines.size()) {
                        currentLine = targetLine;
                        System.out.printf("📍 Moved to line %d\n", currentLine);
                    } else {
                        System.out.printf("❌ Invalid line number. Valid range: 1-%d\n", lines.size());
                    }

                } else if (command.equals("o")) {
                    insertNewLineBelow(lines);

                } else if (command.equals("O")) {
                    insertNewLineAbove(lines);

                } else if (command.equals("dd")) {
                    deleteLine(lines, currentLine - 1);
                    if (currentLine > lines.size()) {
                        currentLine = lines.size();
                    }

                } else if (command.isEmpty()) {
                    continue;

                } else {
                    System.out.println("❌ Unknown command: " + command);
                    System.out.println("💡 Navigation: j/k, gg/G | Edit: i/r/e | Type ':help' for more");
                }
            }

        } catch (Exception e) {
            System.err.println("Error in JSON editor: " + e.getMessage());
            return json;
        }
    }

    /**
     * Enter true vim-like insert mode with real-time editing
     */
    private void enterInsertMode(List<String> lines) {
        System.out.println("\n🔤 INSERT MODE - Navigate with j/k, edit lines directly");
        System.out.println("💡 Commands: j/k (navigate), e (edit line), :x (exit insert mode)");
        System.out.println("🎯 Type line content directly or use commands");
        System.out.println(repeatString("-", 60));

        int insertCurrentLine = currentLine;

        while (true) {
            // Display with current position highlighted
            displayJsonWithLineNumbers(lines, insertCurrentLine);

            System.out.printf("\n🔤 INSERT [Line %d/%d]> ", insertCurrentLine, lines.size());
            String input = scanner.nextLine().trim();

            if (input.equals(":x") || input.equals(":exit")) {
                System.out.println("📤 Exited INSERT mode");
                currentLine = insertCurrentLine;
                break;

            } else if (input.equals("j")) {
                if (insertCurrentLine < lines.size()) {
                    insertCurrentLine++;
                    System.out.printf("📍 Line %d/%d\n", insertCurrentLine, lines.size());
                } else {
                    System.out.println("📍 Already at last line");
                }

            } else if (input.equals("k")) {
                if (insertCurrentLine > 1) {
                    insertCurrentLine--;
                    System.out.printf("📍 Line %d/%d\n", insertCurrentLine, lines.size());
                } else {
                    System.out.println("📍 Already at first line");
                }

            } else if (input.equals("gg")) {
                insertCurrentLine = 1;
                System.out.println("📍 First line");

            } else if (input.equals("G")) {
                insertCurrentLine = lines.size();
                System.out.println("📍 Last line");

            } else if (input.matches("\\d+")) {
                int targetLine = Integer.parseInt(input);
                if (targetLine >= 1 && targetLine <= lines.size()) {
                    insertCurrentLine = targetLine;
                    System.out.printf("📍 Line %d\n", insertCurrentLine);
                } else {
                    System.out.printf("❌ Invalid line. Range: 1-%d\n", lines.size());
                }

            } else if (input.equals("e")) {
                // Edit current line in insert mode
                int index = insertCurrentLine - 1;
                System.out.printf("📝 Editing line %d: %s\n", insertCurrentLine, lines.get(index));
                System.out.print("New content: ");
                String newContent = scanner.nextLine();
                lines.set(index, newContent);
                System.out.println("✅ Line updated");

            } else if (input.equals("o")) {
                // Insert line below and edit it
                System.out.print("📝 New line below: ");
                String content = scanner.nextLine();
                lines.add(insertCurrentLine, content);
                insertCurrentLine++; // Move to newly created line
                System.out.printf("✅ Line inserted at %d\n", insertCurrentLine);

            } else if (input.equals("O")) {
                // Insert line above and edit it
                System.out.print("📝 New line above: ");
                String content = scanner.nextLine();
                lines.add(insertCurrentLine - 1, content);
                System.out.printf("✅ Line inserted at %d\n", insertCurrentLine);

            } else if (input.equals("dd")) {
                if (lines.size() > 1) {
                    String deleted = lines.remove(insertCurrentLine - 1);
                    System.out.printf("✅ Deleted: %s\n", deleted);
                    if (insertCurrentLine > lines.size()) {
                        insertCurrentLine = lines.size();
                    }
                } else {
                    System.out.println("❌ Cannot delete last line");
                }

            } else if (input.equals(":format")) {
                String current = String.join("\n", lines);
                if (formatter.isValidJson(current)) {
                    String reformatted = formatter.formatJson(current);
                    lines.clear();
                    lines.addAll(Arrays.asList(reformatted.split("\n")));
                    if (insertCurrentLine > lines.size()) {
                        insertCurrentLine = lines.size();
                    }
                    System.out.println("✅ Formatted in INSERT mode");
                } else {
                    System.out.println("❌ Cannot format invalid JSON");
                }

            } else if (!input.isEmpty()) {
                // Direct line editing - replace current line with input
                int index = insertCurrentLine - 1;
                lines.set(index, input);
                System.out.printf("✅ Line %d updated with: %s\n", insertCurrentLine, input);

                // Auto-advance to next line for continuous editing
                if (insertCurrentLine < lines.size()) {
                    insertCurrentLine++;
                    System.out.printf("📍 Advanced to line %d\n", insertCurrentLine);
                }
            }
        }
    }

    /**
     * Enter true vim-like replace mode with real-time editing
     */
    private void enterReplaceMode(List<String> lines) {
        System.out.println("\n🔄 REPLACE MODE - Navigate and replace content directly");
        System.out.println("💡 Commands: j/k (navigate), type to replace line, :x (exit replace mode)");
        System.out.println("🎯 Any text input will replace the current line");
        System.out.println(repeatString("-", 60));

        int replaceCurrentLine = currentLine;

        while (true) {
            // Display with current position highlighted
            displayJsonWithLineNumbers(lines, replaceCurrentLine);

            System.out.printf("\n🔄 REPLACE [Line %d/%d]> ", replaceCurrentLine, lines.size());
            String input = scanner.nextLine().trim();

            if (input.equals(":x") || input.equals(":exit")) {
                System.out.println("📤 Exited REPLACE mode");
                currentLine = replaceCurrentLine;
                break;

            } else if (input.equals("j")) {
                if (replaceCurrentLine < lines.size()) {
                    replaceCurrentLine++;
                    System.out.printf("📍 Line %d/%d\n", replaceCurrentLine, lines.size());
                } else {
                    System.out.println("📍 Already at last line");
                }

            } else if (input.equals("k")) {
                if (replaceCurrentLine > 1) {
                    replaceCurrentLine--;
                    System.out.printf("📍 Line %d/%d\n", replaceCurrentLine, lines.size());
                } else {
                    System.out.println("📍 Already at first line");
                }

            } else if (input.equals("gg")) {
                replaceCurrentLine = 1;
                System.out.println("📍 First line");

            } else if (input.equals("G")) {
                replaceCurrentLine = lines.size();
                System.out.println("📍 Last line");

            } else if (input.matches("\\d+")) {
                int targetLine = Integer.parseInt(input);
                if (targetLine >= 1 && targetLine <= lines.size()) {
                    replaceCurrentLine = targetLine;
                    System.out.printf("📍 Line %d\n", replaceCurrentLine);
                } else {
                    System.out.printf("❌ Invalid line. Range: 1-%d\n", lines.size());
                }

            } else if (input.equals("o")) {
                // Add new line below
                System.out.print("📝 New line below: ");
                String content = scanner.nextLine();
                lines.add(replaceCurrentLine, content);
                replaceCurrentLine++; // Move to newly created line
                System.out.printf("✅ Line added at %d\n", replaceCurrentLine);

            } else if (input.equals("dd")) {
                if (lines.size() > 1) {
                    String deleted = lines.remove(replaceCurrentLine - 1);
                    System.out.printf("✅ Deleted: %s\n", deleted);
                    if (replaceCurrentLine > lines.size()) {
                        replaceCurrentLine = lines.size();
                    }
                } else {
                    System.out.println("❌ Cannot delete last line");
                }

            } else if (input.equals(":format")) {
                String current = String.join("\n", lines);
                if (formatter.isValidJson(current)) {
                    String reformatted = formatter.formatJson(current);
                    lines.clear();
                    lines.addAll(Arrays.asList(reformatted.split("\n")));
                    if (replaceCurrentLine > lines.size()) {
                        replaceCurrentLine = lines.size();
                    }
                    System.out.println("✅ Formatted in REPLACE mode");
                } else {
                    System.out.println("❌ Cannot format invalid JSON");
                }

            } else if (!input.isEmpty()) {
                // Replace current line with input
                int index = replaceCurrentLine - 1;
                String oldContent = lines.get(index);
                lines.set(index, input);
                System.out.printf("✅ Replaced: '%s' → '%s'\n", oldContent, input);

                // Auto-advance to next line
                if (replaceCurrentLine < lines.size()) {
                    replaceCurrentLine++;
                    System.out.printf("📍 Advanced to line %d\n", replaceCurrentLine);
                }
            }
        }
    }

    // Navigation helper methods
    private void navigateDown(List<String> lines) {
        if (currentLine < lines.size()) {
            currentLine++;
            System.out.printf("📍 Line %d/%d\n", currentLine, lines.size());
        } else {
            System.out.println("📍 Already at last line");
        }
    }

    private void navigateUp(List<String> lines) {
        if (currentLine > 1) {
            currentLine--;
            System.out.printf("📍 Line %d/%d\n", currentLine, lines.size());
        } else {
            System.out.println("📍 Already at first line");
        }
    }

    private void editCurrentLine(List<String> lines) {
        if (currentLine >= 1 && currentLine <= lines.size()) {
            int index = currentLine - 1;
            System.out.printf("📝 Editing line %d:\n", currentLine);
            System.out.printf("Current: %s\n", lines.get(index));
            System.out.print("New content: ");
            String newContent = scanner.nextLine();
            lines.set(index, newContent);
            System.out.printf("✅ Line %d updated\n", currentLine);
        }
    }

    private void insertNewLineBelow(List<String> lines) {
        System.out.printf("📝 Insert new line below line %d:\n", currentLine);
        System.out.print("Content: ");
        String content = scanner.nextLine();
        lines.add(currentLine, content);
        currentLine++; // Move to the newly inserted line
        System.out.printf("✅ New line inserted at position %d\n", currentLine);
    }

    private void insertNewLineAbove(List<String> lines) {
        System.out.printf("📝 Insert new line above line %d:\n", currentLine);
        System.out.print("Content: ");
        String content = scanner.nextLine();
        lines.add(currentLine - 1, content);
        currentLine++; // Current line number increases due to insertion above
        System.out.printf("✅ New line inserted at position %d\n", currentLine - 1);
    }

    private void displayJsonWithLineNumbers(List<String> lines, int highlightLine) {
        System.out.println("\n📄 Current JSON:");
        System.out.println(repeatString("-", 60));
        for (int i = 0; i < lines.size(); i++) {
            String prefix = (i + 1 == highlightLine) ? "→" : " ";
            String lineMarker = (i + 1 == highlightLine) ? "◄" : "";
            System.out.printf("%s%3d│ %-50s %s\n", prefix, i + 1, lines.get(i), lineMarker);
        }
        System.out.println(repeatString("-", 60));
    }

    private void deleteLine(List<String> lines, int index) {
        if (index >= 0 && index < lines.size() && lines.size() > 1) {
            String deleted = lines.remove(index);
            System.out.printf("✅ Deleted line %d: %s\n", index + 1, deleted);
        } else if (lines.size() <= 1) {
            System.out.println("❌ Cannot delete the last remaining line");
        } else {
            System.out.printf("❌ Invalid line number. Valid range: 1-%d\n", lines.size());
        }
    }

    private void showDetailedHelp() {
        System.out.println("\n" + repeatString("=", 70));
        System.out.println("📚 VIM-LIKE JSON EDITOR HELP");
        System.out.println(repeatString("=", 70));
        System.out.println("🎯 NORMAL MODE COMMANDS:");
        System.out.println("  j/k             - Move down/up one line");
        System.out.println("  gg/G            - Go to first/last line");
        System.out.println("  <number>        - Jump to specific line");
        System.out.println("  e               - Edit current line");
        System.out.println("  o/O             - Insert line below/above");
        System.out.println("  dd              - Delete current line");
        System.out.println();
        System.out.println("🔤 INSERT MODE (press 'i'):");
        System.out.println("  • Navigate: j/k/gg/G/<number>");
        System.out.println("  • Edit line: e (edit current line)");
        System.out.println("  • Add line: o (below), O (above)");
        System.out.println("  • Delete: dd");
        System.out.println("  • Type content directly to replace current line");
        System.out.println("  • Format: :format");
        System.out.println("  • Exit: :x or :exit");
        System.out.println();
        System.out.println("🔄 REPLACE MODE (press 'r'):");
        System.out.println("  • Navigate: j/k/gg/G/<number>");
        System.out.println("  • Replace: type new content for current line");
        System.out.println("  • Add line: o (below current)");
        System.out.println("  • Delete: dd");
        System.out.println("  • Format: :format");
        System.out.println("  • Exit: :x or :exit");
        System.out.println();
        System.out.println("📝 CONTROL COMMANDS:");
        System.out.println("  :w              - Validate JSON");
        System.out.println("  :wq             - Save and quit");
        System.out.println("  :q              - Quit without saving");
        System.out.println("  :format         - Format JSON");
        System.out.println("  :compact        - Make single line");
        System.out.println();
        System.out.println("🎯 EXAMPLE VIM-LIKE WORKFLOW:");
        System.out.println("  1. Press 'i' to enter INSERT mode");
        System.out.println("  2. Navigate with j/k to different lines");
        System.out.println("  3. Type JSON content directly on each line");
        System.out.println("  4. Use :format to clean up");
        System.out.println("  5. Press :x to exit INSERT mode");
        System.out.println("  6. Press :wq to save and quit");
        System.out.println(repeatString("=", 70));
    }

    /**
     * Utility method to repeat a string (replaces String.repeat for Java 8
     * compatibility)
     */
    private String repeatString(String str, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
