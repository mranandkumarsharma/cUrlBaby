package com.curlbaby;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class CommandHistoryDatabase {
    private final String dbPath;
    private Connection connection;
    private int currentIndex = -1;
    private List<String> cachedCommands;
    
    public CommandHistoryDatabase() {
        String userHome = System.getProperty("user.home");
        File dataDir = new File(userHome, ".curlbaby");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
        this.dbPath = new File(dataDir, "history.db").getAbsolutePath();
        this.cachedCommands = new ArrayList<>();
        initializeDatabase();
        loadCachedCommands();
    }
    
    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS command_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "command TEXT NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                );
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error initializing command history database: " + e.getMessage());
        }
    }
    
    private void loadCachedCommands() {
        cachedCommands.clear();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT command FROM command_history ORDER BY timestamp ASC")) {
            
            while (rs.next()) {
                cachedCommands.add(rs.getString("command"));
            }
            
            if (!cachedCommands.isEmpty()) {
                currentIndex = cachedCommands.size();
            }
        } catch (SQLException e) {
            System.err.println("Error loading command history: " + e.getMessage());
        }
    }
    
    public void addCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO command_history (command, timestamp) VALUES (?, ?)")) {
            pstmt.setString(1, command);
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
            
            cachedCommands.add(command);
            currentIndex = cachedCommands.size();
        } catch (SQLException e) {
            System.err.println("Error saving command to history: " + e.getMessage());
        }
    }
    
    public String getPreviousCommand() {
        if (cachedCommands.isEmpty() || currentIndex <= 0) {
            return "";
        }
        
        currentIndex--;
        return cachedCommands.get(currentIndex);
    }
    
    public String getNextCommand() {
        if (cachedCommands.isEmpty() || currentIndex >= cachedCommands.size() - 1) {
            if (currentIndex < cachedCommands.size()) {
                currentIndex = cachedCommands.size();
            }
            return "";
        }
        
        currentIndex++;
        return cachedCommands.get(currentIndex);
    }
    
    public void clearHistory() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM command_history");
            cachedCommands.clear();
            currentIndex = -1;
        } catch (SQLException e) {
            System.err.println("Error clearing command history: " + e.getMessage());
        }
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