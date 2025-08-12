package com.curlbaby;

import com.curlbaby.HttpRequestHandler.Request;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                uiManager.displayError("Unknown API collection command: " + command);
                uiManager.displayInfo("Type 'help' for available commands");
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
                uiManager.displayError("Unknown group command: " + subCommand);
                printGroupHelp();
        }
    }

    private void handleApiCommand(String argument) {
        if (argument.isEmpty()) {
            uiManager.displayError("Missing API command arguments");
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
                uiManager.displayError("Unknown API command: " + subCommand);
                printApiHelp();
        }
    }

    private void printGroupHelp() {
        uiManager.displayInfo("Group Commands:");
        uiManager.displayInfo("  group create <name> - Create a new API group");
        uiManager.displayInfo("  group list - List all API groups");
        uiManager.displayInfo("  group show <id|name> - Show details of a specific group");
        uiManager.displayInfo("  group rename <id> <new_name> - Rename a group");
        uiManager.displayInfo("  group delete <id> - Delete a group");
    }

    private void printApiHelp() {
        uiManager.displayInfo("API Commands:");
        uiManager.displayInfo("  api save <group_id|group_name> <name> - Save current or new API request to a group");
        uiManager.displayInfo("  api list <group_id|group_name> - List all APIs in a group");
        uiManager.displayInfo("  api show <id> - Show details of a specific API");
        uiManager.displayInfo("  api delete <id> - Delete an API request");
        uiManager.displayInfo("  run <id> - Execute a saved API request");
    }

    private void createGroup(String argument) {
        if (argument.isEmpty()) {
            uiManager.displayError("Group name is required");
            return;
        }

        String[] parts = argument.split("\\s+", 2);
        String name = parts[0];
        String description = parts.length > 1 ? parts[1] : "";

        if (description.isEmpty()) {
            System.out.print("Enter group description (optional): ");
            description = scanner.nextLine().trim();
        }

        if (collectionManager.createGroup(name, description)) {
            uiManager.displaySuccess("Group created: " + name);
        }
    }

    private void listGroups() {
        List<Map<String, Object>> groups = collectionManager.getAllGroups();

        if (groups.isEmpty()) {
            uiManager.displayInfo("No API groups found. Create one using 'group create <name>'");
            return;
        }

        uiManager.displayInfo("API Groups:");
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
            uiManager.displayError("Group ID or name is required");
            return;
        }

        Map<String, Object> group;
        try {
            int groupId = Integer.parseInt(argument);
            group = collectionManager.getGroupById(groupId);
        } catch (NumberFormatException e) {
            Integer groupId = collectionManager.getGroupIdByName(argument);
            if (groupId == null) {
                uiManager.displayError("Group not found: " + argument);
                return;
            }
            group = collectionManager.getGroupById(groupId);
        }

        if (group == null) {
            uiManager.displayError("Group not found");
            return;
        }

        int groupId = (int) group.get("id");
        String name = (String) group.get("name");
        String description = (String) group.get("description");

        uiManager.displayInfo("Group Details:");
        System.out.println("  ID: " + groupId);
        System.out.println("  Name: " + name);
        if (description != null && !description.isEmpty()) {
            System.out.println("  Description: " + description);
        }

        // List APIs in this group
        List<Map<String, Object>> requests = collectionManager.getRequestsByGroupId(groupId);
        if (requests.isEmpty()) {
            uiManager.displayInfo("No API requests in this group");
        } else {
            uiManager.displayInfo("API Requests in this group:");
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
            uiManager.displayError("Usage: group rename <id> <new_name>");
            return;
        }

        int groupId = Integer.parseInt(matcher.group(1));
        String newName = matcher.group(2);

        if (collectionManager.renameGroup(groupId, newName)) {
            uiManager.displaySuccess("Group renamed to: " + newName);
        } else {
            uiManager.displayError("Failed to rename group. Group might not exist.");
        }
    }

    private void deleteGroup(String argument) {
        if (argument.isEmpty()) {
            uiManager.displayError("Group ID is required");
            return;
        }

        try {
            int groupId = Integer.parseInt(argument);

            System.out.println("⚠️ This will delete the group and all its API requests.");
            System.out.print("Are you sure? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (confirm.equals("y") || confirm.equals("yes")) {
                if (collectionManager.deleteGroup(groupId)) {
                    uiManager.displaySuccess("Group deleted");
                } else {
                    uiManager.displayError("Failed to delete group. Group might not exist.");
                }
            } else {
                uiManager.displayInfo("Deletion cancelled");
            }
        } catch (NumberFormatException e) {
            uiManager.displayError("Invalid group ID: " + argument);
        }
    }

    private void saveApi(String argument) {
        Pattern pattern = Pattern.compile("([^\\s]+)\\s+(.+)");
        Matcher matcher = pattern.matcher(argument);

        if (!matcher.matches()) {
            uiManager.displayError("Usage: api save <group_id|group_name> <name>");
            return;
        }

        String groupIdentifier = matcher.group(1);
        String requestName = matcher.group(2);

        int groupId;
        try {
            groupId = Integer.parseInt(groupIdentifier);

            if (collectionManager.getGroupById(groupId) == null) {
                uiManager.displayError("Group not found with ID: " + groupId);
                return;
            }
        } catch (NumberFormatException e) {
            Integer id = collectionManager.getGroupIdByName(groupIdentifier);
            if (id == null) {
                uiManager.displayError("Group not found: " + groupIdentifier);
                return;
            }
            groupId = id;
        }

        System.out.print("HTTP Method (GET, POST, PUT, DELETE): ");
        String method = scanner.nextLine().trim().toUpperCase();
        if (!method.matches("GET|POST|PUT|DELETE")) {
            uiManager.displayError("Invalid HTTP method: " + method);
            return;
        }

        System.out.print("URL: ");
        String url = scanner.nextLine().trim();
        if (url.isEmpty()) {
            uiManager.displayError("URL cannot be empty");
            return;
        }

        Map<String, String> headers = new HashMap<>();
        boolean addingHeaders = true;
        while (addingHeaders) {
            System.out.print("Add header? (y/n): ");
            String addHeader = scanner.nextLine().trim().toLowerCase();
            if (addHeader.equals("y")) {
                System.out.print("Header name: ");
                String headerName = scanner.nextLine().trim();
                System.out.print("Header value: ");
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
            System.out.print("Request body (enter 'json' for JSON editor, or type directly): ");
            String bodyInput = scanner.nextLine().trim();

            if (bodyInput.equalsIgnoreCase("json")) {
                SimpleJsonEditor editor = new SimpleJsonEditor();
                body = editor.editJson("{}");
            } else if (!bodyInput.isEmpty()) {
                body = bodyInput;
            }
        }

        System.out.print("Description (optional): ");
        String description = scanner.nextLine().trim();

        if (collectionManager.saveRequest(groupId, requestName, method, url,
                headersJson.toString(), body, description)) {
            uiManager.displaySuccess("API request saved: " + requestName);
        }
    }

    private void listApis(String argument) {
        if (argument.isEmpty()) {
            uiManager.displayError("Group ID or name is required");
            return;
        }

        int groupId;
        try {
            groupId = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            Integer id = collectionManager.getGroupIdByName(argument);
            if (id == null) {
                uiManager.displayError("Group not found: " + argument);
                return;
            }
            groupId = id;
        }

        Map<String, Object> group = collectionManager.getGroupById(groupId);
        if (group == null) {
            uiManager.displayError("Group not found with ID: " + groupId);
            return;
        }

        List<Map<String, Object>> requests = collectionManager.getRequestsByGroupId(groupId);

        if (requests.isEmpty()) {
            uiManager.displayInfo("No API requests in group: " + group.get("name"));
            return;
        }

        uiManager.displayInfo("API Requests in " + group.get("name") + ":");
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
            uiManager.displayError("API request ID is required");
            return;
        }

        try {
            int requestId = Integer.parseInt(argument);
            Map<String, Object> request = collectionManager.getRequestById(requestId);

            if (request == null) {
                uiManager.displayError("API request not found with ID: " + requestId);
                return;
            }

            uiManager.displayInfo("API Request Details:");
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
            uiManager.displayError("Invalid request ID: " + argument);
        }
    }

    private void deleteApi(String argument) {
        if (argument.isEmpty()) {
            uiManager.displayError("API request ID is required");
            return;
        }

        try {
            int requestId = Integer.parseInt(argument);

            System.out.println("⚠️ Are you sure you want to delete this API request?");
            System.out.print("Confirm (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (confirm.equals("y") || confirm.equals("yes")) {
                if (collectionManager.deleteRequest(requestId)) {
                    uiManager.displaySuccess("API request deleted");
                } else {
                    uiManager.displayError("Failed to delete API request. Request might not exist.");
                }
            } else {
                uiManager.displayInfo("Deletion cancelled");
            }

        } catch (NumberFormatException e) {
            uiManager.displayError("Invalid request ID: " + argument);
        }
    }

    private void runSavedRequest(String argument) {
        if (argument.isEmpty()) {
            uiManager.displayError("API request ID is required");
            return;
        }

        try {
            int requestId = Integer.parseInt(argument);
            Map<String, Object> request = collectionManager.getRequestById(requestId);

            if (request == null) {
                uiManager.displayError("API request not found with ID: " + requestId);
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
                        String[] keyValue = pair.split(":", 2);
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim();
                            String value = keyValue[1].trim();
                            httpRequest.addHeader(key, value);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Error parsing headers: " + e.getMessage());
                }
            }

            if ((method.equals("POST") || method.equals("PUT")) && body != null && !body.isEmpty()) {
                httpRequest.setBody(body);
            }

            uiManager.displayInfo("Executing saved request: [" + method + "] " + request.get("name"));
            requestHandler.executeRequest(httpRequest);

        } catch (NumberFormatException e) {
            uiManager.displayError("Invalid request ID: " + argument);
        }
    }
}
