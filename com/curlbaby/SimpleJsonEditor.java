package com.curlbaby;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleJsonEditor {
    private final UIManager uiManager;
    private final Scanner scanner;
    private final JsonFormatter jsonFormatter;
    private List<String> lines = new ArrayList<>();
    
    public SimpleJsonEditor(UIManager uiManager, Scanner scanner, JsonFormatter jsonFormatter) {
        this.uiManager = uiManager;
        this.scanner = scanner;
        this.jsonFormatter = jsonFormatter;
    }
    
    public String edit() {
        uiManager.printInfo("JSON Editor - Commands:");
        uiManager.printInfo(":h - Help");
        uiManager.printInfo(":p - Preview current JSON");
        uiManager.printInfo(":l - List all lines with numbers");
        uiManager.printInfo(":e <line> - Edit specific line number");
        uiManager.printInfo(":d <line> - Delete specific line number");
        uiManager.printInfo(":i <line> - Insert at specific line number");
        uiManager.printInfo(":c - Clear all content");
        uiManager.printInfo(":f - Format JSON");
        uiManager.printInfo(":s - Save and exit");
        uiManager.printInfo(":q - Quit without saving");
        uiManager.printInfo(":paste - Enter paste mode (end with a line containing only '.')\n");
        uiManager.printInfo("QuickTip: Type just a line number to edit that line directly");
        uiManager.printInfo("Start typing your JSON content, or use :paste to paste multiple lines at once\n");
        
        boolean editing = true;
        
        while (editing) {
            uiManager.printInputPrompt("JSON>");
            String input = scanner.nextLine();
            
            if (input.matches("^\\d+$")) {
                int lineNumber = Integer.parseInt(input);
                if (lineNumber > 0 && lineNumber <= lines.size()) {
                    editLine(lineNumber);
                } else {
                    uiManager.printError("Invalid line number: " + lineNumber);
                    listLines();
                }
            } else if (input.startsWith(":")) {
                Pattern editPattern = Pattern.compile("^:e\\s+(\\d+)$");
                Pattern deletePattern = Pattern.compile("^:d\\s+(\\d+)$");
                Pattern insertPattern = Pattern.compile("^:i\\s+(\\d+)$");
                
                Matcher editMatcher = editPattern.matcher(input);
                Matcher deleteMatcher = deletePattern.matcher(input);
                Matcher insertMatcher = insertPattern.matcher(input);
                
                if (input.equals(":h")) {
                    showHelp();
                } else if (input.equals(":p")) {
                    previewJson();
                } else if (input.equals(":l")) {
                    listLines();
                } else if (input.equals(":c")) {
                    clearJson();
                } else if (input.equals(":f")) {
                    formatJson();
                } else if (input.equals(":s")) {
                    editing = false;
                } else if (input.equals(":q")) {
                    lines.clear();
                    editing = false;
                } else if (input.equals(":paste")) {
                    enterPasteMode();
                } else if (editMatcher.matches()) {
                    int lineNumber = Integer.parseInt(editMatcher.group(1));
                    editLine(lineNumber);
                } else if (deleteMatcher.matches()) {
                    int lineNumber = Integer.parseInt(deleteMatcher.group(1));
                    deleteLine(lineNumber);
                } else if (insertMatcher.matches()) {
                    int lineNumber = Integer.parseInt(insertMatcher.group(1));
                    insertLine(lineNumber);
                } else {
                    uiManager.printError("Unknown command: " + input);
                    uiManager.printInfo("Type :h for help");
                }
            } else {
                lines.add(input);
            }
        }
        
        String result = String.join("\n", lines).trim();
        
        if (!result.isEmpty()) {
            try {
                if (result.startsWith("{") || result.startsWith("[")) {
                    return jsonFormatter.formatJson(result);
                }
            } catch (Exception e) {
                uiManager.printWarning("JSON formatting failed, returning raw input.");
            }
        }
        
        return result;
    }
    
    private void showHelp() {
        uiManager.printInfo("JSON Editor Commands:");
        uiManager.printInfo(":h - Show this help message");
        uiManager.printInfo(":p - Preview the current JSON content");
        uiManager.printInfo(":l - List all lines with line numbers");
        uiManager.printInfo(":e <line> - Edit specific line number");
        uiManager.printInfo(":d <line> - Delete specific line number");
        uiManager.printInfo(":i <line> - Insert at specific line number");
        uiManager.printInfo(":c - Clear all content");
        uiManager.printInfo(":f - Format JSON (prettify)");
        uiManager.printInfo(":s - Save the content and exit the editor");
        uiManager.printInfo(":q - Quit without saving");
        uiManager.printInfo(":paste - Enter paste mode for multiple lines");
        uiManager.printInfo("Quick navigation: Just type a line number (e.g. '5') to edit that line");
        uiManager.printInfo("Any other input will be added as a new line");
    }
    
    private void listLines() {
        if (lines.isEmpty()) {
            uiManager.printInfo("Editor is empty. Start adding content.");
            return;
        }
        
        for (int i = 0; i < lines.size(); i++) {
            System.out.printf("%3d: %s\n", i + 1, lines.get(i));
        }
    }
    
    private void editLine(int lineNumber) {
        if (lineNumber < 1 || lineNumber > lines.size()) {
            uiManager.printError("Invalid line number. Use :l to see available lines.");
            return;
        }
        
        uiManager.printInfo("Current line " + lineNumber + ": " + lines.get(lineNumber - 1));
        uiManager.printInputPrompt("New content:");
        String newContent = scanner.nextLine();
        lines.set(lineNumber - 1, newContent);
        uiManager.printInfo("Line " + lineNumber + " updated.");
    }
    
    private void deleteLine(int lineNumber) {
        if (lineNumber < 1 || lineNumber > lines.size()) {
            uiManager.printError("Invalid line number. Use :l to see available lines.");
            return;
        }
        
        uiManager.printInfo("Removing line " + lineNumber + ": " + lines.get(lineNumber - 1));
        lines.remove(lineNumber - 1);
        uiManager.printInfo("Line deleted. Use :l to see updated content.");
    }
    
    private void insertLine(int lineNumber) {
        if (lineNumber < 1 || lineNumber > lines.size() + 1) {
            uiManager.printError("Invalid position. Choose between 1 and " + (lines.size() + 1));
            return;
        }
        
        uiManager.printInputPrompt("Enter new line content:");
        String newContent = scanner.nextLine();
        lines.add(lineNumber - 1, newContent);
        uiManager.printInfo("Line inserted at position " + lineNumber);
    }
    
    private void enterPasteMode() {
        uiManager.printInfo("Paste Mode: Enter or paste your content. Type a single '.' on a new line to finish.");
        String line;
        while (!(line = scanner.nextLine()).equals(".")) {
            lines.add(line);
        }
        uiManager.printInfo("Paste mode ended. " + lines.size() + " lines in editor.");
    }
    
    private void previewJson() {
        String content = String.join("\n", lines).trim();
        if (content.isEmpty()) {
            uiManager.printInfo("No content yet.");
            return;
        }
        
        uiManager.printInfo("Current JSON content:");
        try {
            if (content.startsWith("{") || content.startsWith("[")) {
                System.out.println(jsonFormatter.formatJson(content));
            } else {
                System.out.println(content);
            }
        } catch (Exception e) {
            uiManager.printWarning("Invalid JSON format. Showing raw content:");
            System.out.println(content);
        }
    }
    
    private void formatJson() {
        String content = String.join("\n", lines).trim();
        if (content.isEmpty()) {
            uiManager.printInfo("No content to format.");
            return;
        }
        
        try {
            if (content.startsWith("{") || content.startsWith("[")) {
                String formatted = jsonFormatter.formatJson(content);
                lines.clear();
                for (String line : formatted.split("\n")) {
                    lines.add(line);
                }
                uiManager.printInfo("JSON formatted successfully. Use :l to view.");
            } else {
                uiManager.printWarning("Content doesn't appear to be valid JSON (should start with { or [)");
            }
        } catch (Exception e) {
            uiManager.printError("Failed to format JSON: " + e.getMessage());
        }
    }
    
    private void clearJson() {
        lines.clear();
        uiManager.printInfo("JSON content cleared.");
    }
}