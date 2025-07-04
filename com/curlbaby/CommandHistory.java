package com.curlbaby;

import java.util.ArrayList;
import java.util.List;

public class CommandHistory {
    private final List<String> history;
    private int currentIndex;
    
    public CommandHistory() {
        this.history = new ArrayList<>();
        this.currentIndex = 0;
    }
    
    public void addCommand(String command) {
        if (!command.trim().isEmpty()) { 
            if (history.isEmpty() || !history.get(history.size() - 1).equals(command)) {
                history.add(command);
            } 
            currentIndex = history.size();
        }
    }
    
    public String getPreviousCommand() {
        if (history.isEmpty()) {
            return "";
        }
         
        if (currentIndex == history.size()) {
            currentIndex--;
        } else if (currentIndex > 0) { 
            currentIndex--;
        }
         
        return history.get(currentIndex);
    }
    
    public String getNextCommand() {
        if (history.isEmpty() || currentIndex >= history.size() - 1) { 
            currentIndex = history.size();
            return "";
        }
        
        
        currentIndex++;
        return history.get(currentIndex);
    }
    
     
    public void resetNavigation() {
        currentIndex = history.size();
    }
    
    public void clear() {
        history.clear();
        currentIndex = 0;
    }
    
    public int size() {
        return history.size();
    }
    
    public List<String> getAll() {
        return new ArrayList<>(history);
    }
}