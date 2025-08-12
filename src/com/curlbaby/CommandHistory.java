package com.curlbaby;

import java.util.*;

public class CommandHistory {

    private List<String> commands;
    private static final int MAX_HISTORY = 100;

    public CommandHistory() {
        this.commands = new ArrayList<>();
    }

    public void addCommand(String command) {
        if (command != null && !command.trim().isEmpty()) {
            commands.add(command);

            // Keep only recent commands
            if (commands.size() > MAX_HISTORY) {
                commands.remove(0);
            }
        }
    }

    public List<String> getRecentCommands(int count) {
        int size = commands.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(commands.subList(fromIndex, size));
    }

    public int getCommandCount() {
        return commands.size();
    }

    public List<String> getAllCommands() {
        return new ArrayList<>(commands);
    }

    public void clearHistory() {
        commands.clear();
    }
}
