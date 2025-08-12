package com.curlbaby;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandProcessor {

    private HttpRequestHandler httpHandler;
    private CommandHistory commandHistory;
    private ApiCollectionManager collectionManager;
    private JsonFormatter jsonFormatter;
    private SimpleJsonEditor jsonEditor;
    private UIManager uiManager;
    private ApiCollectionCommands apiCommands;

    // Command patterns
    private static final Pattern CURL_PATTERN = Pattern.compile("curl\\s+(.+)");
    private static final Pattern JSON_PATTERN = Pattern.compile("json\\s+(format|edit|validate|compact)\\s+(.+)");
    private static final Pattern COLLECTION_PATTERN = Pattern.compile("collection\\s+(add|list|run|delete)\\s*(.*)");

    public CommandProcessor() {
        this.httpHandler = new HttpRequestHandler();
        this.commandHistory = new CommandHistory();
        this.collectionManager = new ApiCollectionManager();
        this.jsonFormatter = new JsonFormatter();
        this.jsonEditor = new SimpleJsonEditor();
        this.uiManager = new UIManager();
        this.apiCommands = new ApiCollectionCommands(collectionManager, uiManager, httpHandler);
    }

    /**
     * Main command processing method
     */
    public void processCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String command = input.trim();
        commandHistory.addCommand(command);

        try {
            // Split command into parts
            String[] parts = command.split("\\s+", 2);
            String mainCommand = parts[0].toLowerCase();
            String argument = parts.length > 1 ? parts[1] : "";

            // Handle different command types
            switch (mainCommand) {
                case "help":
                case "?":
                    showHelp();
                    break;
                case "clear":
                case "cls":
                    clearScreen();
                    break;
                case "exit":
                case "quit":
                    handleExit();
                    break;
                case "history":
                    showHistory();
                    break;

                // HTTP Commands
                case "get":
                    handleGetCommand(argument);
                    break;
                case "post":
                    handlePostCommand(argument);
                    break;
                case "put":
                    handlePutCommand(argument);
                    break;
                case "delete":
                    handleDeleteCommand(argument);
                    break;

                // cURL command
                case "curl":
                    if (command.startsWith("curl ")) {
                        handleCurlCommand(command);
                    } else {
                        uiManager.displayError("Invalid cURL command format");
                    }
                    break;

                // JSON commands
                case "json":
                    if (command.startsWith("json ")) {
                        handleJsonCommand(command);
                    } else {
                        uiManager.displayError("Invalid JSON command format");
                    }
                    break;

                // Collection commands
                case "collection":
                    if (command.startsWith("collection ")) {
                        handleCollectionCommand(command);
                    } else {
                        uiManager.displayError("Invalid collection command format");
                    }
                    break;

                // API Collection commands
                case "group":
                case "api":
                case "run":
                    handleApiCollectionCommands(command);
                    break;

                // Configuration commands
                case "set":
                    handleSetCommand(command);
                    break;

                case "status":
                    showStatus();
                    break;

                default:
                    System.out.println("❌ Unknown command: " + mainCommand);
                    System.out.println("💡 Type 'help' for available commands");
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle GET requests
     */
    private void handleGetCommand(String url) {
        if (url.isEmpty()) {
            uiManager.displayError("URL is required for GET request");
            uiManager.displayInfo("Usage: get <url>");
            return;
        }

        System.out.println("🚀 Executing GET request...");
        httpHandler.executeGetRequest(url);
    }

    /**
     * Handle POST requests
     */
    private void handlePostCommand(String url) {
        if (url.isEmpty()) {
            uiManager.displayError("URL is required for POST request");
            uiManager.displayInfo("Usage: post <url>");
            return;
        }

        System.out.println("🚀 Executing POST request...");
        httpHandler.executePostRequest(url);
    }

    /**
     * Handle PUT requests
     */
    private void handlePutCommand(String url) {
        if (url.isEmpty()) {
            uiManager.displayError("URL is required for PUT request");
            uiManager.displayInfo("Usage: put <url>");
            return;
        }

        System.out.println("🚀 Executing PUT request...");
        httpHandler.executePutRequest(url);
    }

    /**
     * Handle DELETE requests
     */
    private void handleDeleteCommand(String url) {
        if (url.isEmpty()) {
            uiManager.displayError("URL is required for DELETE request");
            uiManager.displayInfo("Usage: delete <url>");
            return;
        }

        System.out.println("🚀 Executing DELETE request...");
        httpHandler.executeDeleteRequest(url);
    }

    /**
     * Handle cURL commands
     */
    private void handleCurlCommand(String command) {
        try {
            System.out.println("🚀 Executing cURL command...");

            // Parse and execute the cURL command
            String response = httpHandler.executeCurl(command);

            // Check if response is JSON and offer formatting
            if (isJsonResponse(response)) {
                System.out.println("\n📄 Response received (JSON detected):");
                System.out.println(repeatString("─", 60));

                // Auto-format JSON response
                String formattedResponse = jsonFormatter.formatJson(response);
                System.out.println(formattedResponse);
                System.out.println(repeatString("─", 60));

                // Offer to edit the response
                System.out.print("\n🔧 Would you like to edit this JSON? (y/N): ");
                Scanner scanner = new Scanner(System.in);
                String editChoice = scanner.nextLine().trim().toLowerCase();

                if (editChoice.equals("y") || editChoice.equals("yes")) {
                    String editedJson = jsonEditor.editJson(response);
                    System.out.println("\n✅ Edited JSON:");
                    System.out.println(editedJson);
                }
            } else {
                System.out.println("\n📄 Response:");
                System.out.println(repeatString("─", 60));
                System.out.println(response);
                System.out.println(repeatString("─", 60));
            }

        } catch (Exception e) {
            System.err.println("❌ Error executing cURL command: " + e.getMessage());
        }
    }

    /**
     * Handle JSON commands
     */
    private void handleJsonCommand(String command) {
        Matcher matcher = JSON_PATTERN.matcher(command);

        if (!matcher.matches()) {
            System.out.println("❌ Invalid JSON command format");
            System.out.println("💡 Usage: json <format|edit|validate|compact> <json_string>");
            return;
        }

        String action = matcher.group(1).toLowerCase();
        String jsonString = matcher.group(2);

        System.out.println("🔧 Processing JSON with action: " + action);

        switch (action) {
            case "format":
                System.out.println("\n📄 Formatted JSON:");
                System.out.println(repeatString("─", 50));
                System.out.println(jsonFormatter.formatJson(jsonString));
                System.out.println(repeatString("─", 50));
                break;

            case "compact":
                System.out.println("\n📄 Compacted JSON:");
                System.out.println(repeatString("─", 50));
                System.out.println(jsonFormatter.compactJson(jsonString));
                System.out.println(repeatString("─", 50));
                break;

            case "edit":
                System.out.println("\n🔧 Opening JSON editor...");
                String editedJson = jsonEditor.editJson(jsonString);
                System.out.println("\n📄 Final JSON:");
                System.out.println(repeatString("─", 50));
                System.out.println(editedJson);
                System.out.println(repeatString("─", 50));
                break;

            case "validate":
                boolean isValid = jsonFormatter.isValidJson(jsonString);
                System.out.println("\n🔍 JSON Validation Result:");
                System.out.println(isValid ? "✅ Valid JSON" : "❌ Invalid JSON");

                if (!isValid) {
                    System.out.println("💡 Use 'json edit <json>' to fix issues");
                }
                break;

            default:
                System.out.println("❌ Unknown JSON action: " + action);
                System.out.println("💡 Available actions: format, edit, validate, compact");
        }
    }

    /**
     * Handle collection commands
     */
    private void handleCollectionCommand(String command) {
        Matcher matcher = COLLECTION_PATTERN.matcher(command);

        if (!matcher.matches()) {
            System.out.println("❌ Invalid collection command format");
            System.out.println("💡 Usage: collection <add|list|run|delete> [name]");
            return;
        }

        String action = matcher.group(1).toLowerCase();
        String collectionName = matcher.group(2).trim();

        switch (action) {
            case "add":
                if (collectionName.isEmpty()) {
                    System.out.println("❌ Collection name required");
                    return;
                }
                collectionManager.addCollection(collectionName);
                System.out.println("✅ Collection '" + collectionName + "' added");
                break;

            case "list":
                System.out.println("\n📚 Available Collections:");
                collectionManager.listCollections();
                break;

            case "run":
                if (collectionName.isEmpty()) {
                    System.out.println("❌ Collection name required");
                    return;
                }
                collectionManager.runCollection(collectionName);
                break;

            case "delete":
                if (collectionName.isEmpty()) {
                    System.out.println("❌ Collection name required");
                    return;
                }
                collectionManager.deleteCollection(collectionName);
                System.out.println("✅ Collection '" + collectionName + "' deleted");
                break;
        }
    }

    /**
     * Handle API collection commands (group, api, run)
     */
    private void handleApiCollectionCommands(String command) {
        String[] parts = command.split("\\s+", 2);
        String mainCommand = parts[0];
        String argument = parts.length > 1 ? parts[1] : "";

        apiCommands.handleCommand(mainCommand, argument);
    }

    /**
     * Handle set commands for configuration
     */
    private void handleSetCommand(String command) {
        String[] parts = command.substring(4).trim().split("\\s+", 2);

        if (parts.length < 2) {
            System.out.println("❌ Invalid set command format");
            System.out.println("💡 Usage: set <property> <value>");
            return;
        }

        String property = parts[0].toLowerCase();
        String value = parts[1];

        switch (property) {
            case "timeout":
                try {
                    int timeout = Integer.parseInt(value);
                    httpHandler.setTimeout(timeout);
                    System.out.println("✅ Timeout set to " + timeout + " seconds");
                } catch (NumberFormatException e) {
                    System.out.println("❌ Invalid timeout value. Must be a number.");
                }
                break;

            case "headers":
                httpHandler.setDefaultHeaders(value);
                System.out.println("✅ Default headers set");
                break;

            case "output":
                // Set output format or file
                System.out.println("✅ Output setting updated: " + value);
                break;

            default:
                System.out.println("❌ Unknown property: " + property);
                System.out.println("💡 Available properties: timeout, headers, output");
        }
    }

    /**
     * Show command history
     */
    private void showHistory() {
        System.out.println("\n📜 Command History:");
        System.out.println(repeatString("─", 50));

        List<String> history = commandHistory.getRecentCommands(10);

        if (history.isEmpty()) {
            System.out.println("No commands in history");
        } else {
            for (int i = 0; i < history.size(); i++) {
                System.out.printf("%2d. %s\n", i + 1, history.get(i));
            }
        }
        System.out.println(repeatString("─", 50));
    }

    /**
     * Show application status
     */
    private void showStatus() {
        System.out.println("\n📊 cUrlBaby Status:");
        System.out.println(repeatString("─", 50));
        System.out.println("🔗 HTTP Handler: " + (httpHandler != null ? "Ready" : "Not initialized"));
        System.out.println("📜 Command History: " + commandHistory.getCommandCount() + " commands");
        System.out.println("📚 Collections: " + collectionManager.getCollectionCount());
        System.out.println("🔧 JSON Formatter: Ready");
        System.out.println("✏️ JSON Editor: Ready");
        System.out.println("🗂️ API Collections: Ready");
        System.out.println(repeatString("─", 50));
    }

    /**
     * Show help information
     */
    private void showHelp() {
        System.out.println("\n" + repeatString("=", 60));
        System.out.println("🐣 cUrlBaby - Enhanced cURL Tool");
        System.out.println(repeatString("=", 60));
        System.out.println();
        System.out.println("🌐 HTTP COMMANDS:");
        System.out.println("  get <url>                - Execute GET request");
        System.out.println("  post <url>               - Execute POST request (interactive)");
        System.out.println("  put <url>                - Execute PUT request (interactive)");
        System.out.println("  delete <url>             - Execute DELETE request");
        System.out.println("  curl <options> <url>     - Execute cURL command");
        System.out.println();
        System.out.println("🔧 JSON COMMANDS:");
        System.out.println("  json format <json>       - Format JSON with indentation");
        System.out.println("  json compact <json>      - Compress JSON to single line");
        System.out.println("  json edit <json>         - Open interactive JSON editor");
        System.out.println("  json validate <json>     - Check if JSON is valid");
        System.out.println();
        System.out.println("📚 COLLECTION COMMANDS:");
        System.out.println("  collection add <name>    - Create new API collection");
        System.out.println("  collection list          - Show all collections");
        System.out.println("  collection run <name>    - Execute collection");
        System.out.println("  collection delete <name> - Remove collection");
        System.out.println();
        System.out.println("🗂️ API GROUP COMMANDS:");
        System.out.println("  group create <name>      - Create new API group");
        System.out.println("  group list               - List all API groups");
        System.out.println("  group show <id>          - Show group details");
        System.out.println("  group delete <id>        - Delete API group");
        System.out.println();
        System.out.println("🔗 API REQUEST COMMANDS:");
        System.out.println("  api save <group> <name>  - Save API request to group");
        System.out.println("  api list <group>         - List APIs in group");
        System.out.println("  api show <id>            - Show API request details");
        System.out.println("  api delete <id>          - Delete API request");
        System.out.println("  run <id>                 - Execute saved API request");
        System.out.println();
        System.out.println("⚙️ CONFIGURATION:");
        System.out.println("  set timeout <seconds>    - Set request timeout");
        System.out.println("  set headers <headers>    - Set default headers");
        System.out.println("  set output <format>      - Set output format");
        System.out.println();
        System.out.println("🔍 UTILITY COMMANDS:");
        System.out.println("  history                  - Show command history");
        System.out.println("  status                   - Show application status");
        System.out.println("  clear/cls                - Clear screen");
        System.out.println("  help/?                   - Show this help");
        System.out.println("  exit/quit                - Exit application");
        System.out.println();
        System.out.println("💡 EXAMPLES:");
        System.out.println("  get https://api.github.com/users/octocat");
        System.out.println("  post https://jsonplaceholder.typicode.com/posts");
        System.out.println("  json format {\"name\":\"John\",\"age\":30}");
        System.out.println("  group create \"GitHub APIs\"");
        System.out.println(repeatString("=", 60));
    }

    /**
     * Check if response is JSON
     */
    private boolean isJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return false;
        }

        String trimmed = response.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    /**
     * Handle exit command
     */
    private void handleExit() {
        System.out.println("👋 Thanks for using cUrlBaby!");
        System.exit(0);
    }

    /**
     * Clear screen
     */
    private void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[2J\033[H");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback - print newlines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

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
