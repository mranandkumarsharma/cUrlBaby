package com.curlbaby;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.*;
import java.util.Scanner;

public class JsonFormatter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectWriter PRETTY_PRINTER = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
    private static final ObjectWriter COMPACT_WRITER = OBJECT_MAPPER.writer();

    public String formatJson(String json) {
        try {
            Object jsonObj = OBJECT_MAPPER.readValue(json, Object.class);
            return PRETTY_PRINTER.writeValueAsString(jsonObj);
        } catch (Exception e) {
            return "Invalid JSON: " + e.getMessage();
        }
    }

    public String compactJson(String json) {
        try {
            Object jsonObj = OBJECT_MAPPER.readValue(json, Object.class);
            return COMPACT_WRITER.writeValueAsString(jsonObj);
        } catch (Exception e) {
            return "Invalid JSON: " + e.getMessage();
        }
    }

    public boolean isValidJson(String json) {
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Opens JSON in external editor for interactive editing
     */
    public String editJsonInteractively(String json) {
        try {
            String formattedJson = formatJson(json);

            File tempFile = File.createTempFile("curlbaby_json_", ".json");
            tempFile.deleteOnExit();

            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(formattedJson);
            }

            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                // Try VS Code first, then Notepad++, then Notepad
                try {
                    pb = new ProcessBuilder("code", "-w", tempFile.getAbsolutePath());
                    pb.start().waitFor();
                } catch (Exception e1) {
                    try {
                        pb = new ProcessBuilder("notepad++.exe", tempFile.getAbsolutePath());
                        pb.start().waitFor();
                    } catch (Exception e2) {
                        pb = new ProcessBuilder("notepad.exe", tempFile.getAbsolutePath());
                        pb.start().waitFor();
                    }
                }
            } else {
                // Linux/Mac - try vim, then nano
                try {
                    pb = new ProcessBuilder("vim", tempFile.getAbsolutePath());
                    pb.start().waitFor();
                } catch (Exception e) {
                    pb = new ProcessBuilder("nano", tempFile.getAbsolutePath());
                    pb.start().waitFor();
                }
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            String editedJson = content.toString().trim();
            return isValidJson(editedJson) ? editedJson : json;

        } catch (Exception e) {
            System.err.println("Error opening editor: " + e.getMessage());
            return json;
        }
    }
}
