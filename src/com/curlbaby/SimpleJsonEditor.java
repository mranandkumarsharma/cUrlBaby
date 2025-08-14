package com.curlbaby;

import java.nio.file.Files;
import java.nio.file.Path;

public class SimpleJsonEditor {

    public String editJson(String json) {
        try {
            // 1. Write JSON to a temp file
            Path tempFile = Files.createTempFile("curlbaby_json_edit", ".json");
            Files.write(tempFile, json.getBytes());

            // 2. Launch vim editor
            ProcessBuilder pb = new ProcessBuilder("vim", tempFile.toString());
            pb.inheritIO(); // Attach to current terminal
            Process process = pb.start();
            process.waitFor();

            // 3. Read edited JSON back
            String editedJson = new String(Files.readAllBytes(tempFile));
            Files.deleteIfExists(tempFile);

            return editedJson;
        } catch (Exception e) {
            System.err.println("❌ Error launching vim editor: " + e.getMessage());
            System.out.println("Falling back to original JSON.");
            return json;
        }
    }
}
