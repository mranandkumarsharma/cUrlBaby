package com.curlbaby;

import java.io.File;
import java.sql.*;
import java.util.*;

public class ApiCollectionManager {

    private Map<String, List<String>> collections;
    private Connection connection;
    private static final String DB_NAME = "curlbaby.db";

    public ApiCollectionManager() {
        this.collections = new HashMap<>();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Create database file in user directory if it doesn't exist
            String userHome = System.getProperty("user.home");
            String dbPath = userHome + File.separator + DB_NAME;

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTables();
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            // Fallback to in-memory operations
        }
    }

    private void createTables() throws SQLException {
        String createGroupsTable = """
            CREATE TABLE IF NOT EXISTS api_groups (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                description TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createRequestsTable = """
            CREATE TABLE IF NOT EXISTS api_requests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                group_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                method TEXT NOT NULL,
                url TEXT NOT NULL,
                headers TEXT,
                body TEXT,
                description TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (group_id) REFERENCES api_groups(id) ON DELETE CASCADE
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createGroupsTable);
            stmt.execute(createRequestsTable);
        }
    }

    // Legacy collection methods (for backward compatibility)
    public void addCollection(String name) {
        collections.put(name, new ArrayList<>());
    }

    public void listCollections() {
        if (collections.isEmpty()) {
            System.out.println("No collections available");
            return;
        }

        for (String name : collections.keySet()) {
            List<String> requests = collections.get(name);
            System.out.printf("📁 %s (%d requests)\n", name, requests.size());
        }
    }

    public void runCollection(String name) {
        List<String> requests = collections.get(name);
        if (requests == null) {
            System.out.println("❌ Collection not found: " + name);
            return;
        }

        System.out.println("🚀 Running collection: " + name);
        for (String request : requests) {
            System.out.println("Executing: " + request);
            // Execute each request
        }
    }

    public void deleteCollection(String name) {
        if (collections.remove(name) == null) {
            System.out.println("❌ Collection not found: " + name);
        }
    }

    public int getCollectionCount() {
        return collections.size() + getAllGroups().size();
    }

    public void addRequestToCollection(String collectionName, String request) {
        List<String> requests = collections.get(collectionName);
        if (requests != null) {
            requests.add(request);
        }
    }

    // API Group methods
    public boolean createGroup(String name, String description) {
        if (connection == null) {
            return false;
        }

        String sql = "INSERT INTO api_groups (name, description) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating group: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getAllGroups() {
        List<Map<String, Object>> groups = new ArrayList<>();
        if (connection == null) {
            return groups;
        }

        String sql = "SELECT id, name, description, created_at FROM api_groups ORDER BY name";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> group = new HashMap<>();
                group.put("id", rs.getInt("id"));
                group.put("name", rs.getString("name"));
                group.put("description", rs.getString("description"));
                group.put("created_at", rs.getString("created_at"));
                groups.add(group);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching groups: " + e.getMessage());
        }

        return groups;
    }

    public Map<String, Object> getGroupById(int id) {
        if (connection == null) {
            return null;
        }

        String sql = "SELECT id, name, description, created_at FROM api_groups WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> group = new HashMap<>();
                group.put("id", rs.getInt("id"));
                group.put("name", rs.getString("name"));
                group.put("description", rs.getString("description"));
                group.put("created_at", rs.getString("created_at"));
                return group;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching group: " + e.getMessage());
        }

        return null;
    }

    public Integer getGroupIdByName(String name) {
        if (connection == null) {
            return null;
        }

        String sql = "SELECT id FROM api_groups WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching group ID: " + e.getMessage());
        }

        return null;
    }

    public boolean renameGroup(int id, String newName) {
        if (connection == null) {
            return false;
        }

        String sql = "UPDATE api_groups SET name = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error renaming group: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteGroup(int id) {
        if (connection == null) {
            return false;
        }

        String sql = "DELETE FROM api_groups WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting group: " + e.getMessage());
            return false;
        }
    }

    // API Request methods
    public boolean saveRequest(int groupId, String name, String method, String url,
            String headers, String body, String description) {
        if (connection == null) {
            return false;
        }

        String sql = "INSERT INTO api_requests (group_id, name, method, url, headers, body, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            pstmt.setString(2, name);
            pstmt.setString(3, method);
            pstmt.setString(4, url);
            pstmt.setString(5, headers);
            pstmt.setString(6, body);
            pstmt.setString(7, description);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving request: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getRequestsByGroupId(int groupId) {
        List<Map<String, Object>> requests = new ArrayList<>();
        if (connection == null) {
            return requests;
        }

        String sql = "SELECT id, name, method, url, headers, body, description, created_at FROM api_requests WHERE group_id = ? ORDER BY name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> request = new HashMap<>();
                request.put("id", rs.getInt("id"));
                request.put("group_id", groupId);
                request.put("name", rs.getString("name"));
                request.put("method", rs.getString("method"));
                request.put("url", rs.getString("url"));
                request.put("headers", rs.getString("headers"));
                request.put("body", rs.getString("body"));
                request.put("description", rs.getString("description"));
                request.put("created_at", rs.getString("created_at"));
                requests.add(request);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching requests: " + e.getMessage());
        }

        return requests;
    }

    public Map<String, Object> getRequestById(int id) {
        if (connection == null) {
            return null;
        }

        String sql = "SELECT id, group_id, name, method, url, headers, body, description, created_at FROM api_requests WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> request = new HashMap<>();
                request.put("id", rs.getInt("id"));
                request.put("group_id", rs.getInt("group_id"));
                request.put("name", rs.getString("name"));
                request.put("method", rs.getString("method"));
                request.put("url", rs.getString("url"));
                request.put("headers", rs.getString("headers"));
                request.put("body", rs.getString("body"));
                request.put("description", rs.getString("description"));
                request.put("created_at", rs.getString("created_at"));
                return request;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching request: " + e.getMessage());
        }

        return null;
    }

    public boolean deleteRequest(int id) {
        if (connection == null) {
            return false;
        }

        String sql = "DELETE FROM api_requests WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting request: " + e.getMessage());
            return false;
        }
    }

    // Cleanup method
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database: " + e.getMessage());
            }
        }
    }
}
