package app.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AIService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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";

    public static String askChatGPT(String prompt) {
        try {
            // Set up the connection
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setDoOutput(true);

            // Create the JSON payload
            JsonObject payload = new JsonObject();
            payload.addProperty("model", "gpt-3.5-turbo");
            payload.addProperty("temperature", 0.7);
            payload.add("messages", JsonParser.parseString("[{\"role\":\"user\", \"content\":\"" + prompt + "\"}]"));

            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read the response
            int statusCode = connection.getResponseCode();
            if (statusCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder responseBuilder = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        responseBuilder.append(responseLine.trim());
                    }

                    // Parse the response JSON
                    JsonObject responseJson = JsonParser.parseString(responseBuilder.toString()).getAsJsonObject();
                    return responseJson
                            .getAsJsonArray("choices")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content")
                            .getAsString();
                }
            } else {
                System.err.println("Error: HTTP " + statusCode);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorBuilder = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorBuilder.append(errorLine.trim());
                    }
                    System.err.println("Error Details: " + errorBuilder);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}