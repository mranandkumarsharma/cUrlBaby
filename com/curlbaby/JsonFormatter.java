package com.curlbaby;

public class JsonFormatter {
    
    public String formatJson(String json) {
        int indentLevel = 0;
        StringBuilder result = new StringBuilder();
        boolean inQuotes = false;
        
        for (char c : json.toCharArray()) {
            if (c == '"' && (result.length() == 0 || result.charAt(result.length() - 1) != '\\')) {
                inQuotes = !inQuotes;
                result.append(c);
            } else if (!inQuotes && (c == '{' || c == '[')) {
                indentLevel++;
                result.append(c).append("\n").append("  ".repeat(indentLevel));
            } else if (!inQuotes && (c == '}' || c == ']')) {
                indentLevel--;
                result.append("\n").append("  ".repeat(indentLevel)).append(c);
            } else if (!inQuotes && c == ',') {
                result.append(c).append("\n").append("  ".repeat(indentLevel));
            } else if (!inQuotes && c == ':') {
                result.append(c).append(" ");
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
}