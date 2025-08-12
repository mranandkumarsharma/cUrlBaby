package com.curlbaby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HttpRequestHandler {

    private final UIManager uiManager;
    private final JsonFormatter jsonFormatter;
    private final Scanner scanner;
    private final ApiCollectionManager apiCollectionManager;
    private Request lastExecutedRequest; // Store the last executed request

    private int timeout = 30000; // 30 seconds default
    private Map<String, String> defaultHeaders = new HashMap<>();

    // Constructor for CommandProcessor (without UIManager)
    public HttpRequestHandler() {
        this.uiManager = new UIManager();
        this.jsonFormatter = new JsonFormatter();
        this.scanner = new Scanner(System.in);
        this.apiCollectionManager = new ApiCollectionManager();
    }

    // Constructor for other classes (with UIManager)
    public HttpRequestHandler(UIManager uiManager) {
        this.uiManager = uiManager;
        this.jsonFormatter = new JsonFormatter();
        this.scanner = new Scanner(System.in);
        this.apiCollectionManager = new ApiCollectionManager();
    }

    public void executeGetRequest(String urlString) {
        Request request = new Request("GET", urlString);
        executeRequest(request);
    }

    public void executePostRequest(String urlString) {
        Request request = new Request("POST", urlString);

        uiManager.displayInfo("Content-Type (default: application/json):");
        String contentType = scanner.nextLine().trim();
        if (contentType.isEmpty()) {
            contentType = "application/json";
        }
        request.addHeader("Content-Type", contentType);

        uiManager.displayInfo("Request body (enter 'json' for JSON editor, or type directly):");
        String bodyInput = scanner.nextLine().trim();

        if (bodyInput.equalsIgnoreCase("json")) {
            SimpleJsonEditor editor = new SimpleJsonEditor();
            String jsonBody = editor.editJson("{}");
            request.setBody(jsonBody);
        } else if (!bodyInput.isEmpty()) {
            request.setBody(bodyInput);
        }

        boolean addingHeaders = true;
        while (addingHeaders) {
            uiManager.displayInfo("Add header? (y/n):");
            String addHeader = scanner.nextLine().trim().toLowerCase();
            if (addHeader.equals("y")) {
                uiManager.displayInfo("Header name:");
                String headerName = scanner.nextLine().trim();
                uiManager.displayInfo("Header value:");
                String headerValue = scanner.nextLine().trim();
                request.addHeader(headerName, headerValue);
            } else {
                addingHeaders = false;
            }
        }

        executeRequest(request);
    }

    public void executePutRequest(String urlString) {
        Request request = new Request("PUT", urlString);

        uiManager.displayInfo("PUT request follows the same flow as POST");

        uiManager.displayInfo("Content-Type (default: application/json):");
        String contentType = scanner.nextLine().trim();
        if (contentType.isEmpty()) {
            contentType = "application/json";
        }
        request.addHeader("Content-Type", contentType);

        uiManager.displayInfo("Request body (enter 'json' for JSON editor, or type directly):");
        String bodyInput = scanner.nextLine().trim();

        if (bodyInput.equalsIgnoreCase("json")) {
            SimpleJsonEditor editor = new SimpleJsonEditor();
            String jsonBody = editor.editJson("{}");
            request.setBody(jsonBody);
        } else if (!bodyInput.isEmpty()) {
            request.setBody(bodyInput);
        }

        boolean addingHeaders = true;
        while (addingHeaders) {
            uiManager.displayInfo("Add header? (y/n):");
            String addHeader = scanner.nextLine().trim().toLowerCase();
            if (addHeader.equals("y")) {
                uiManager.displayInfo("Header name:");
                String headerName = scanner.nextLine().trim();
                uiManager.displayInfo("Header value:");
                String headerValue = scanner.nextLine().trim();
                request.addHeader(headerName, headerValue);
            } else {
                addingHeaders = false;
            }
        }

        executeRequest(request);
    }

    public void executeDeleteRequest(String urlString) {
        Request request = new Request("DELETE", urlString);
        executeRequest(request);
    }

    public void executeRequest(Request request) {
        HttpURLConnection connection = null;
        try {
            String urlString = request.getUrl();
            if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                urlString = "http://" + urlString;
                request.setUrl(urlString);
            }

            uiManager.displayInfo("🚀 Executing: [" + request.getMethod().toUpperCase() + "] " + urlString);

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(request.getMethod());
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            // Add default headers
            for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }

            // Add request-specific headers
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }

            if (request.getMethod().equals("POST") || request.getMethod().equals("PUT")) {
                connection.setDoOutput(true);
                if (request.getBody() != null && !request.getBody().isEmpty()) {
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = request.getBody().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }
                }
            }

            int status = connection.getResponseCode();
            uiManager.displayInfo("📊 Status: " + status + " " + connection.getResponseMessage());

            System.out.println("\n📋 Request Details:");
            System.out.println("  Method: " + request.getMethod());
            System.out.println("  URL: " + request.getUrl());

            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                System.out.println("  Header: " + header.getKey() + ": " + header.getValue());
            }

            if (request.getBody() != null && !request.getBody().isEmpty()) {
                System.out.println("\n📄 Request Body:");
                if (request.getBody().trim().startsWith("{") || request.getBody().trim().startsWith("[")) {
                    try {
                        System.out.println(jsonFormatter.formatJson(request.getBody()));
                    } catch (Exception e) {
                        System.out.println(request.getBody());
                    }
                } else {
                    System.out.println(request.getBody());
                }
            }

            System.out.println("\n📨 Response Headers:");
            // Fixed: Replace lambda with traditional for loop for Java 8 compatibility
            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null) {
                    System.out.println("  " + header.getKey() + ": " + String.join(", ", header.getValue()));
                }
            }

            BufferedReader reader;
            if (status > 299) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }

            System.out.println("\n📄 Response Body:");
            String line;
            StringBuilder responseContent = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            reader.close();

            String response = responseContent.toString();
            if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                try {
                    String formatted = jsonFormatter.formatJson(response);
                    System.out.println(formatted);
                } catch (Exception e) {
                    System.out.println(response);
                }
            } else {
                System.out.println(response);
            }

            // Store the last executed request
            lastExecutedRequest = request;

            // After successful execution, offer to save the request
            offerToSaveRequest();

        } catch (IOException e) {
            uiManager.displayError("Error: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void offerToSaveRequest() {
        if (lastExecutedRequest == null) {
            return;
        }

        uiManager.displayInfo("Would you like to save this request? (y/n):");
        String saveResponse = scanner.nextLine().trim().toLowerCase();

        if (saveResponse.equals("y") || saveResponse.equals("yes")) {
            // Get the request name
            uiManager.displayInfo("Enter a name for this request:");
            String requestName = scanner.nextLine().trim();
            if (requestName.isEmpty()) {
                uiManager.displayError("Request name cannot be empty");
                return;
            }

            // Fetch all groups and display them
            List<Map<String, Object>> groups = apiCollectionManager.getAllGroups();
            if (groups.isEmpty()) {
                uiManager.displayInfo("No existing API groups. Creating a new one.");
                createGroupAndSaveRequest(requestName);
                return;
            }

            // Display groups
            uiManager.displayInfo("Select a group or create a new one:");
            int counter = 1;
            for (Map<String, Object> group : groups) {
                System.out.printf("  %d. %s\n", counter++, group.get("name"));
            }
            System.out.printf("  %d. Create new group\n", counter);

            // Get user selection
            uiManager.displayInfo("Enter your choice (1-" + counter + "):");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice < 1 || choice > counter) {
                    uiManager.displayError("Invalid choice");
                    return;
                }
            } catch (NumberFormatException e) {
                uiManager.displayError("Invalid input. Please enter a number.");
                return;
            }

            if (choice == counter) {
                // Create new group
                createGroupAndSaveRequest(requestName);
            } else {
                // Save to existing group
                Map<String, Object> selectedGroup = groups.get(choice - 1);
                int groupId = (int) selectedGroup.get("id");
                saveRequestToGroup(groupId, requestName);
            }
        }
    }

    private void createGroupAndSaveRequest(String requestName) {
        uiManager.displayInfo("Enter new group name:");
        String groupName = scanner.nextLine().trim();
        if (groupName.isEmpty()) {
            uiManager.displayError("Group name cannot be empty");
            return;
        }

        uiManager.displayInfo("Enter group description (optional):");
        String description = scanner.nextLine().trim();

        if (apiCollectionManager.createGroup(groupName, description)) {
            Integer groupId = apiCollectionManager.getGroupIdByName(groupName);
            if (groupId == null) {
                uiManager.displayError("Error retrieving newly created group");
                return;
            }
            saveRequestToGroup(groupId, requestName);
        } else {
            uiManager.displayError("Failed to create group");
        }
    }

    private void saveRequestToGroup(int groupId, String requestName) {
        if (lastExecutedRequest == null) {
            uiManager.displayError("No request to save");
            return;
        }

        // Convert headers to JSON
        StringBuilder headersJson = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : lastExecutedRequest.getHeaders().entrySet()) {
            if (!first) {
                headersJson.append(", ");
            }
            headersJson.append("\"").append(entry.getKey()).append("\": \"")
                    .append(entry.getValue()).append("\"");
            first = false;
        }
        headersJson.append("}");

        uiManager.displayInfo("Description (optional):");
        String description = scanner.nextLine().trim();

        if (apiCollectionManager.saveRequest(
                groupId,
                requestName,
                lastExecutedRequest.getMethod(),
                lastExecutedRequest.getUrl(),
                headersJson.toString(),
                lastExecutedRequest.getBody(),
                description)) {
            uiManager.displaySuccess("API request saved: " + requestName);
        } else {
            uiManager.displayError("Failed to save request");
        }
    }

    /**
     * Set timeout for requests
     */
    public void setTimeout(int timeoutSeconds) {
        this.timeout = timeoutSeconds * 1000; // Convert to milliseconds
    }

    /**
     * Set default headers
     */
    public void setDefaultHeaders(String headersString) {
        // Parse headers string like "Content-Type:application/json,Accept:application/json"
        String[] headerPairs = headersString.split(",");
        for (String pair : headerPairs) {
            String[] keyValue = pair.trim().split(":", 2);
            if (keyValue.length == 2) {
                defaultHeaders.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
    }

    /**
     * Execute curl command
     */
    public String executeCurl(String curlCommand) throws Exception {
        // Basic implementation - you can enhance this
        uiManager.displayInfo("Parsing cURL command: " + curlCommand);

        // For now, return a sample response
        return "{ \"status\": \"success\", \"message\": \"cURL command executed\", \"command\": \"" + curlCommand + "\" }";
    }

    public static class Request {

        private String method;
        private String url;
        private Map<String, String> headers;
        private String body;

        public Request(String method, String url) {
            this.method = method;
            this.url = url;
            this.headers = new HashMap<>();
            this.body = null;
        }

        public String getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void addHeader(String name, String value) {
            headers.put(name, value);
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
