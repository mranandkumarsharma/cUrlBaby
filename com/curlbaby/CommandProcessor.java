package com.curlbaby;

public class CommandProcessor {
    private final UIManager uiManager;
    private final HttpRequestHandler requestHandler;
    private final ApiCollectionManager apiCollectionManager;
    private final ApiCollectionCommands apiCollectionCommands;
    
    public CommandProcessor(UIManager uiManager, HttpRequestHandler requestHandler) {
        this.uiManager = uiManager;
        this.requestHandler = requestHandler;
        this.apiCollectionManager = new ApiCollectionManager(uiManager);
        this.apiCollectionCommands = new ApiCollectionCommands(apiCollectionManager, uiManager, requestHandler);
    }
    
    public void processCommand(String command, String argument) {
        
        switch (command) {
            case "exit":
                uiManager.printExitMessage();
                apiCollectionManager.close();
                System.exit(0);
                break;
            case "help":
                printHelp();
                break;
            case "get":
                if (argument.isEmpty()) {
                    uiManager.printError("Usage: get <url>");
                } else {
                    requestHandler.executeGetRequest(argument);
                }
                break;
            case "post": 
                if (argument.isEmpty()) {
                    uiManager.printError("Usage: post <url>");
                } else {
                    requestHandler.executePostRequest(argument);
                }
                break;
            case "put":
                if (argument.isEmpty()) {
                    uiManager.printError("Usage: put <url>");
                } else {
                    requestHandler.executePutRequest(argument);
                }
                break;
            case "delete":
                if (argument.isEmpty()) {
                    uiManager.printError("Usage: delete <url>");
                } else {
                    requestHandler.executeDeleteRequest(argument);
                }
                break;
            case "group":
            case "api":
            case "run":
                apiCollectionCommands.handleCommand(command, argument);
                break;
            default:
                uiManager.printError("Unknown command: " + command);
                System.out.println("Type 'help' for available commands");
        }
    }
    
    private void printHelp() {
        uiManager.printInfo("\nBasic Commands:");
        uiManager.printInfo("  help - Display this help message");
        uiManager.printInfo("  exit - Exit the application");
        
        uiManager.printInfo("\nRequest Commands:");
        uiManager.printInfo("  get <url> - Execute a GET request to the specified URL");
        uiManager.printInfo("  post <url> - Execute a POST request to the specified URL");
        uiManager.printInfo("  put <url> - Execute a PUT request to the specified URL");
        uiManager.printInfo("  delete <url> - Execute a DELETE request to the specified URL");
        
        uiManager.printInfo("\nAPI Collection Commands:");
        uiManager.printInfo("  group - Manage API groups");
        uiManager.printInfo("    group create <name> - Create a new API group");
        uiManager.printInfo("    group list - List all API groups");
        uiManager.printInfo("    group show <id|name> - Show details of a specific group");
        uiManager.printInfo("    group rename <id> <new_name> - Rename a group");
        uiManager.printInfo("    group delete <id> - Delete a group");
        
        uiManager.printInfo("\n  api - Manage API requests");
        uiManager.printInfo("    api save <group_id|group_name> <name> - Save current or new API request to a group");
        uiManager.printInfo("    api list <group_id|group_name> - List all APIs in a group");
        uiManager.printInfo("    api show <id> - Show details of a specific API request");
        uiManager.printInfo("    api delete <id> - Delete an API request");
        
        uiManager.printInfo("\n  run <id> - Execute a saved API request");
        
        uiManager.printInfo("\nHistory Commands:");
        uiManager.printInfo("  history - Display command history");
        uiManager.printInfo("  history clear - Clear command history");
    }
}