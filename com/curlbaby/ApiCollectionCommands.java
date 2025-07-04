package com.curlbaby;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.curlbaby.HttpRequestHandler.Request;

public class ApiCollectionCommands {
    private final ApiCollectionManager collectionManager;
    private final UIManager uiManager;
    private final Scanner scanner;
    private final HttpRequestHandler requestHandler;
    private final JsonFormatter jsonFormatter;
    
    public ApiCollectionCommands(ApiCollectionManager collectionManager, UIManager uiManager, 
                                HttpRequestHandler requestHandler) {
        this.collectionManager = collectionManager;
        this.uiManager = uiManager;
        this.scanner = new Scanner(System.in);
        this.requestHandler = requestHandler;
        this.jsonFormatter = new JsonFormatter();
    }
    
    public void handleCommand(String command, String argument) {
        switch (command) {
            case "group":
                handleGroupCommand(argument);
                break;
            case "api":
                handleApiCommand(argument);
                break;
            case "run":
                runSavedRequest(argument);
                break;
            default:
                uiManager.printError("Unknown API collection command: " + command);
                uiManager.printInfo("Type 'help' for available commands");
        }
    }
    
    private void handleGroupCommand(String argument) {
        if (argument.isEmpty()) {
            listGroups();
            return;
        }
        
        String[] parts = argument.split("\\s+", 2);
        String subCommand = parts[0].toLowerCase();
        String subArgument = parts.length > 1 ? parts[1] : "";
        
        switch (subCommand) {
            case "create":
                createGroup(subArgument);
                break;
            case "list":
                listGroups();
                break;
            case "show":
                showGroup(subArgument);
                break;
            case "rename":
                renameGroup(subArgument);
                break;
            case "delete":
                deleteGroup(subArgument);
                break;
            default:
                uiManager.printError("Unknown group command: " + subCommand);
                printGroupHelp();
        }
    }
    
    private void handleApiCommand(String argument) {
        if (argument.isEmpty()) {
            uiManager.printError("Missing API command arguments");
            printApiHelp();
            return;
        }
        
        String[] parts = argument.split("\\s+", 2);
        String subCommand = parts[0].toLowerCase();
        String subArgument = parts.length > 1 ? parts[1] : "";
        
        switch (subCommand) {
            case "save":
                saveApi(subArgument);
                break;
            case "list":
                listApis(subArgument);
                break;
            case "show":
                showApi(subArgument);
                break;
            case "delete":
                deleteApi(subArgument);
                break;
            default:
                uiManager.printError("Unknown API command: " + subCommand);
                printApiHelp();
        }
    }
    
    private void printGroupHelp() {
        uiManager.printInfo("Group Commands:");
        uiManager.printInfo("  group create <name> - Create a new API group");
        uiManager.printInfo("  group list - List all API groups");
        uiManager.printInfo("  group show <id|name> - Show details of a specific group");
        uiManager.printInfo("  group rename <id> <new_name> - Rename a group");
        uiManager.printInfo("  group delete <id> - Delete a group");
    }
    
    private void printApiHelp() {
        uiManager.printInfo("API Commands:");
        uiManager.printInfo("  api save <group_id|group_name> <name> - Save current or new API request to a group");
        uiManager.printInfo("  api list <group_id|group_name> - List all APIs in a group");
        uiManager.printInfo("  api show <id> - Show details of a specific API");
        uiManager.printInfo("  api delete <id> - Delete an API request");
        uiManager.printInfo("  run <id> - Execute a saved API request");
    }
     
    
    private void createGroup(String argument) {
        if (argument.isEmpty()) {
            uiManager.printError("Group name is required");
            return;
        }
        
        String[] parts = argument.split("\\s+", 2);
        String name = parts[0];
        String description = parts.length > 1 ? parts[1] : "";
        
        if (description.isEmpty()) {
            uiManager.printInputPrompt("Enter group description (optional):");
            description = scanner.nextLine().trim();
        }
        
        if (collectionManager.createGroup(name, description)) {
            uiManager.printSuccess("Group created: " + name);
        }
    }
    
    private void listGroups() {
        List<Map<String, Object>> groups = collectionManager.getAllGroups();
        
        if (groups.isEmpty()) {
            uiManager.printInfo("No API groups found. Create one using 'group create <name>'");
            return;
        }
        
        uiManager.printInfo("API Groups:");
        for (Map<String, Object> group : groups) {
            int id = (int) group.get("id");
            String name = (String) group.get("name");
            String description = (String) group.get("description");
            
            System.out.printf("  %d. %s%s\n", 
                    id, 
                    name, 
                    (description != null && !description.isEmpty() ? " - " + description : ""));
        }
    }
    
    private void showGroup(String argument) {
        if (argument.isEmpty()) {
            uiManager.printError("Group ID or name is required");
            return;
        }
        
        Map<String, Object> group;
        try {
            int groupId = Integer.parseInt(argument);
            group = collectionManager.getGroupById(groupId);
        } catch (NumberFormatException e) { 
            Integer groupId = collectionManager.getGroupIdByName(argument);
            if (groupId == null) {
                uiManager.printError("Group not found: " + argument);
                return;
            }
            group = collectionManager.getGroupById(groupId);
        }
        
        if (group == null) {
            uiManager.printError("Group not found");
            return;
        }
        
        int groupId = (int) group.get("id");
        String name = (String) group.get("name");
        String description = (String) group.get("description");
        
        uiManager.printInfo("Group Details:");
        System.out.println("  ID: " + groupId);
        System.out.println("  Name: " + name);
        if (description != null && !description.isEmpty()) {
            System.out.println("  Description: " + description);
        }
        
        // List APIs in this group
        List<Map<String, Object>> requests = collectionManager.getRequestsByGroupId(groupId);
        if (requests.isEmpty()) {
            uiManager.printInfo("No API requests in this group");
        } else {
            uiManager.printInfo("API Requests in this group:");
            for (Map<String, Object> request : requests) {
                System.out.printf("  %d. [%s] %s - %s\n", 
                        (int) request.get("id"), 
                        ((String) request.get("method")).toUpperCase(),
                        (String) request.get("name"),
                        (String) request.get("url"));
            }
        }
    }
    
    private void renameGroup(String argument) {
        Pattern pattern = Pattern.compile("(\\d+)\\s+(.+)");
        Matcher matcher = pattern.matcher(argument);
        
        if (!matcher.matches()) {
            uiManager.printError("Usage: group rename <id> <new_name>");
            return;
        }
        
        int groupId = Integer.parseInt(matcher.group(1));
        String newName = matcher.group(2);
        
        if (collectionManager.renameGroup(groupId, newName)) {
            uiManager.printSuccess("Group renamed to: " + newName);
        } else {
            uiManager.printError("Failed to rename group. Group might not exist.");
        }
    }
    
    private void deleteGroup(String argument) {
        if (argument.isEmpty()) {
            uiManager.printError("Group ID is required");
            return;
        }
        
        try {
            int groupId = Integer.parseInt(argument);
     
            uiManager.printWarning("This will delete the group and all its API requests.");
            uiManager.printInputPrompt("Are you sure? (y/n):");
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                if (collectionManager.deleteGroup(groupId)) {
                    uiManager.printSuccess("Group deleted");
                } else {
                    uiManager.printError("Failed to delete group. Group might not exist.");
                }
            } else {
                uiManager.printInfo("Deletion cancelled");
            }
        } catch (NumberFormatException e) {
            uiManager.printError("Invalid group ID: " + argument);
        }
    }
     
    
    private void saveApi(String argument) {
        Pattern pattern = Pattern.compile("([^\\s]+)\\s+(.+)");
        Matcher matcher = pattern.matcher(argument);
        
        if (!matcher.matches()) {
            uiManager.printError("Usage: api save <group_id|group_name> <name>");
            return;
        }
        
        String groupIdentifier = matcher.group(1);
        String requestName = matcher.group(2);
         
        int groupId;
        try {
            groupId = Integer.parseInt(groupIdentifier);
           
            if (collectionManager.getGroupById(groupId) == null) {
                uiManager.printError("Group not found with ID: " + groupId);
                return;
            }
        } catch (NumberFormatException e) {
             
            Integer id = collectionManager.getGroupIdByName(groupIdentifier);
            if (id == null) {
                uiManager.printError("Group not found: " + groupIdentifier);
                return;
            }
            groupId = id;
        }
        
        
        uiManager.printInputPrompt("HTTP Method (GET, POST, PUT, DELETE):");
        String method = scanner.nextLine().trim().toUpperCase();
        if (!method.matches("GET|POST|PUT|DELETE")) {
            uiManager.printError("Invalid HTTP method: " + method);
            return;
        }
        
        uiManager.printInputPrompt("URL:");
        String url = scanner.nextLine().trim();
        if (url.isEmpty()) {
            uiManager.printError("URL cannot be empty");
            return;
        }
        
         
        Map<String, String> headers = new HashMap<>();
        boolean addingHeaders = true;
        while (addingHeaders) {
            uiManager.printInputPrompt("Add header? (y/n):");
            String addHeader = scanner.nextLine().trim().toLowerCase();
            if (addHeader.equals("y")) {
                uiManager.printInputPrompt("Header name:");
                String headerName = scanner.nextLine().trim();
                uiManager.printInputPrompt("Header value:");
                String headerValue = scanner.nextLine().trim();
                headers.put(headerName, headerValue);
            } else {
                addingHeaders = false;
            }
        }
        
         
        StringBuilder headersJson = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (!first) {
                headersJson.append(", ");
            }
            headersJson.append("\"").append(entry.getKey()).append("\": \"")
                      .append(entry.getValue()).append("\"");
            first = false;
        }
        headersJson.append("}");
        
        
        String body = "";
        if (method.equals("POST") || method.equals("PUT")) {
            uiManager.printInputPrompt("Request body (enter 'json' for JSON editor, or type directly):");
            String bodyInput = scanner.nextLine().trim();
            
            if (bodyInput.equalsIgnoreCase("json")) {
                SimpleJsonEditor editor = new SimpleJsonEditor(uiManager, scanner, jsonFormatter);
                body = editor.edit();
            } else if (!bodyInput.isEmpty()) {
                body = bodyInput;
            }
        }
        
        uiManager.printInputPrompt("Description (optional):");
        String description = scanner.nextLine().trim();
        
        
        if (collectionManager.saveRequest(groupId, requestName, method, url, 
                                     headersJson.toString(), body, description)) {
            uiManager.printSuccess("API request saved: " + requestName);
        }
    }
    
    private void listApis(String argument) {
        if (argument.isEmpty()) {
            uiManager.printError("Group ID or name is required");
            return;
        }
        
        int groupId;
        try {
            groupId = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
             
            Integer id = collectionManager.getGroupIdByName(argument);
            if (id == null) {
                uiManager.printError("Group not found: " + argument);
                return;
            }
            groupId = id;
        }
        
        Map<String, Object> group = collectionManager.getGroupById(groupId);
        if (group == null) {
            uiManager.printError("Group not found with ID: " + groupId);
            return;
        }
        
        List<Map<String, Object>> requests = collectionManager.getRequestsByGroupId(groupId);
        
        if (requests.isEmpty()) {
            uiManager.printInfo("No API requests in group: " + group.get("name"));
            return;
        }
        
        uiManager.printInfo("API Requests in " + group.get("name") + ":");
        for (Map<String, Object> request : requests) {
            System.out.printf("  %d. [%s] %s - %s\n", 
                    (int) request.get("id"), 
                    ((String) request.get("method")).toUpperCase(),
                    (String) request.get("name"),
                    (String) request.get("url"));
            
            String description = (String) request.get("description");
            if (description != null && !description.isEmpty()) {
                System.out.println("     " + description);
            }
        }
    }
    
    private void showApi(String argument) {
        if (argument.isEmpty()) {
            uiManager.printError("API request ID is required");
            return;
        }
        
        try {
            int requestId = Integer.parseInt(argument);
            Map<String, Object> request = collectionManager.getRequestById(requestId);
            
            if (request == null) {
                uiManager.printError("API request not found with ID: " + requestId);
                return;
            }
            
            uiManager.printInfo("API Request Details:");
            System.out.println("  ID: " + request.get("id"));
            System.out.println("  Name: " + request.get("name"));
            System.out.println("  Method: " + request.get("method"));
            System.out.println("  URL: " + request.get("url"));
            
            String description = (String) request.get("description");
            if (description != null && !description.isEmpty()) {
                System.out.println("  Description: " + description);
            }
            
            String headers = (String) request.get("headers");
            if (headers != null && !headers.equals("{}")) {
                System.out.println("  Headers: ");
                try {
                    System.out.println(jsonFormatter.formatJson(headers));
                } catch (Exception e) {
                    System.out.println("    " + headers);
                }
            }
            
            String body = (String) request.get("body");
            if (body != null && !body.isEmpty()) {
                System.out.println("  Body: ");
                if (body.trim().startsWith("{") || body.trim().startsWith("[")) {
                    try {
                        System.out.println(jsonFormatter.formatJson(body));
                    } catch (Exception e) {
                        System.out.println("    " + body);
                    }
                } else {
                    System.out.println("    " + body);
                }
            }
            
        } catch (NumberFormatException e) {
            uiManager.printError("Invalid request ID: " + argument);
        }
    }
    
    private void deleteApi(String argument) {
        if (argument.isEmpty()) {
            uiManager.printError("API request ID is required");
            return;
        }
        
        try {
            int requestId = Integer.parseInt(argument);
            
             
            uiManager.printWarning("Are you sure you want to delete this API request?");
            uiManager.printInputPrompt("Confirm (y/n):");
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                if (collectionManager.deleteRequest(requestId)) {
                    uiManager.printSuccess("API request deleted");
                } else {
                    uiManager.printError("Failed to delete API request. Request might not exist.");
                }
            } else {
                uiManager.printInfo("Deletion cancelled");
            }
            
        } catch (NumberFormatException e) {
            uiManager.printError("Invalid request ID: " + argument);
        }
    }
    
    private void runSavedRequest(String argument) {
        if (argument.isEmpty()) {
            uiManager.printError("API request ID is required");
            return;
        }
        
        try {
            int requestId = Integer.parseInt(argument);
            Map<String, Object> request = collectionManager.getRequestById(requestId);
            
            if (request == null) {
                uiManager.printError("API request not found with ID: " + requestId);
                return;
            }
            
            String method = (String) request.get("method");
            String url = (String) request.get("url");
            String headersJson = (String) request.get("headers");
            String body = (String) request.get("body");
            
             
            Request httpRequest = new Request(method, url);
            
             
            if (headersJson != null && !headersJson.equals("{}")) {
                try {
                    
                    headersJson = headersJson.replaceAll("[{}\"]", "");
                    String[] headerPairs = headersJson.split(",");
                    for (String pair : headerPairs) {
                        String[] keyValue = pair.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim();
                            String value = keyValue[1].trim();
                            httpRequest.addHeader(key, value);
                        }
                    }
                } catch (Exception e) {
                    uiManager.printWarning("Error parsing headers: " + e.getMessage());
                }
            }
            
    
            if ((method.equals("POST") || method.equals("PUT")) && body != null && !body.isEmpty()) {
                httpRequest.setBody(body);
            }
            
           
            uiManager.printInfo("Executing saved request: [" + method + "] " + request.get("name"));
            switch (method) {
                case "GET":
                    requestHandler.executeRequest(httpRequest);
                    break;
                case "POST":
                    requestHandler.executeRequest(httpRequest);
                    break;
                case "PUT":
                    requestHandler.executeRequest(httpRequest);
                    break;
                case "DELETE":
                    requestHandler.executeRequest(httpRequest);
                    break;
                default:
                    uiManager.printError("Unsupported method: " + method);
            }
            
        } catch (NumberFormatException e) {
            uiManager.printError("Invalid request ID: " + argument);
        }
    }
}