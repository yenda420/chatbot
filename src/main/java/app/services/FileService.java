package app.services;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileService {
    public static String readFileContent(File file) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;

            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        }
        return contentBuilder.toString();
    }

    public static boolean writeTestToFile(String content, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            writer.write(content);
            System.out.println("[INFO] - Test written to file: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("[ERROR] - Failed to write test to file.");
            e.printStackTrace();
            return false;
        }
    }
}
