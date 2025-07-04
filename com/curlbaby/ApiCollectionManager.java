package com.curlbaby;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiCollectionManager {
    private final String dbPath;
    private Connection connection;
    private final UIManager uiManager;
    
    public ApiCollectionManager(UIManager uiManager) {
        this.uiManager = uiManager;
        String userHome = System.getProperty("user.home");
        File dataDir = new File(userHome, ".curlbaby");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
        this.dbPath = new File(dataDir, "api_collections.db").getAbsolutePath();
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            System.out.println("Connected to SQLite database: " + dbPath);
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS api_groups (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE NOT NULL, " +
                    "description TEXT)"
                );
                
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS api_requests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "group_id INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "method TEXT NOT NULL, " +
                    "url TEXT NOT NULL, " +
                    "headers TEXT, " +   
                    "body TEXT, " +     
                    "description TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(group_id) REFERENCES api_groups(id) ON DELETE CASCADE, " +
                    "UNIQUE(group_id, name))"
                );
                
                // Enable foreign keys
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error initializing API collection database: " + e.getMessage());
        }
    }
    
     
    
    public boolean createGroup(String name, String description) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO api_groups (name, description) VALUES (?, ?)")) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                uiManager.printError("Group name already exists: " + name);
            } else {
                uiManager.printError("Error creating group: " + e.getMessage());
            }
            return false;
        }
    }
    
    public boolean renameGroup(int groupId, String newName) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE api_groups SET name = ? WHERE id = ?")) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, groupId);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            uiManager.printError("Error renaming group: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateGroupDescription(int groupId, String description) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE api_groups SET description = ? WHERE id = ?")) {
            pstmt.setString(1, description);
            pstmt.setInt(2, groupId);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            uiManager.printError("Error updating group description: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteGroup(int groupId) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "DELETE FROM api_groups WHERE id = ?")) {
            pstmt.setInt(2, groupId);
            int deleted = pstmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            uiManager.printError("Error deleting group: " + e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, Object>> getAllGroups() {
        List<Map<String, Object>> groups = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, description FROM api_groups ORDER BY name")) {
            
            while (rs.next()) {
                Map<String, Object> group = new HashMap<>();
                group.put("id", rs.getInt("id"));
                group.put("name", rs.getString("name"));
                group.put("description", rs.getString("description"));
                groups.add(group);
            }
            
        } catch (SQLException e) {
            uiManager.printError("Error retrieving groups: " + e.getMessage());
        }
        return groups;
    }
    
    public Map<String, Object> getGroupById(int groupId) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT id, name, description FROM api_groups WHERE id = ?")) {
            pstmt.setInt(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> group = new HashMap<>();
                    group.put("id", rs.getInt("id"));
                    group.put("name", rs.getString("name"));
                    group.put("description", rs.getString("description"));
                    return group;
                }
            }
        } catch (SQLException e) {
            uiManager.printError("Error retrieving group: " + e.getMessage());
        }
        return null;
    }
    
    public Integer getGroupIdByName(String groupName) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT id FROM api_groups WHERE name = ?")) {
            pstmt.setString(1, groupName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            uiManager.printError("Error retrieving group ID: " + e.getMessage());
        }
        return null;
    }
    
    // API request management methods
    
    public boolean saveRequest(int groupId, String name, String method, String url, 
                               String headers, String body, String description) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO api_requests (group_id, name, method, url, headers, body, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
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
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                uiManager.printError("Request name already exists in this group: " + name);
            } else {
                uiManager.printError("Error saving request: " + e.getMessage());
            }
            return false;
        }
    }
    
    public boolean updateRequest(int requestId, String name, String method, String url,
                                String headers, String body, String description) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE api_requests SET name = ?, method = ?, url = ?, headers = ?, " +
                "body = ?, description = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?")) {
            pstmt.setString(1, name);
            pstmt.setString(2, method);
            pstmt.setString(3, url);
            pstmt.setString(4, headers);
            pstmt.setString(5, body);
            pstmt.setString(6, description);
            pstmt.setInt(7, requestId);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            uiManager.printError("Error updating request: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteRequest(int requestId) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "DELETE FROM api_requests WHERE id = ?")) {
            pstmt.setInt(1, requestId);
            int deleted = pstmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            uiManager.printError("Error deleting request: " + e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, Object>> getRequestsByGroupId(int groupId) {
        List<Map<String, Object>> requests = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT id, name, method, url, description FROM api_requests " +
                "WHERE group_id = ? ORDER BY name")) {
            pstmt.setInt(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> request = new HashMap<>();
                    request.put("id", rs.getInt("id"));
                    request.put("name", rs.getString("name"));
                    request.put("method", rs.getString("method"));
                    request.put("url", rs.getString("url"));
                    request.put("description", rs.getString("description"));
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            uiManager.printError("Error retrieving requests: " + e.getMessage());
        }
        return requests;
    }
    
    public Map<String, Object> getRequestById(int requestId) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT id, group_id, name, method, url, headers, body, description " +
                "FROM api_requests WHERE id = ?")) {
            pstmt.setInt(1, requestId);
            try (ResultSet rs = pstmt.executeQuery()) {
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
                    return request;
                }
            }
        } catch (SQLException e) {
            uiManager.printError("Error retrieving request: " + e.getMessage());
        }
        return null;
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}