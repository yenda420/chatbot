package app.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class AIService {
    protected static URL url;

    protected static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    protected static final String OPENAI_API_KEY = "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";
    protected static final String OPENAI_MODEL = "gpt-3.5-turbo";
    protected static final double OPENAI_TEMPERATURE = 0.7;

    public AIService() {
        try {
            // Set up the connection URL
            url = new URL(OPENAI_API_URL);
        } catch (IOException e) {
            System.err.println("[ERROR] - Failed to set up the connection to the OpenAI API");
            e.printStackTrace();
        }
    }

    public String askAI(String userPrompt) {
        if (userPrompt == null || userPrompt.isEmpty() || userPrompt.isBlank()) {
            System.err.println("[ERROR] - User prompt is null or empty.");
            return null;
        }

        // Prepare the messages array
        JsonArray messagesArray = new JsonArray();

        // Add the user's message
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userPrompt);
        messagesArray.add(userMessage);

        // Call the helper method
        return sendRequestToAI(messagesArray);
    }

    public String askAI(ArrayList<JsonObject> messagesToSend) {
        if (messagesToSend == null || messagesToSend.isEmpty()) {
            System.err.println("[ERROR] - Messages to send are null or empty.");
            return null;
        }

        // Prepare the messages array
        JsonArray messagesArray = new JsonArray();
        for (JsonObject message : messagesToSend) {
            messagesArray.add(message);
        }

        // Call the helper method
        return sendRequestToAI(messagesArray);
    }

    // Private helper method to handle the request
    private String sendRequestToAI(JsonArray messagesArray) {
        try {
            // Prepare the payload
            JsonObject payload = new JsonObject();
            payload.addProperty("model", OPENAI_MODEL);
            payload.addProperty("temperature", OPENAI_TEMPERATURE);
            payload.add("messages", messagesArray);

            // Open the connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
            conn.setDoOutput(true);

            // Send the request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read the response
            int statusCode = conn.getResponseCode();
            // Success response
            if (statusCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                    StringBuilder responseBuilder = new StringBuilder();
                    String responseLine;

                    while ((responseLine = br.readLine()) != null) {
                        responseBuilder.append(responseLine.trim());
                    }

                    // Parse the response JSON
                    JsonObject responseJson = JsonParser.parseString(responseBuilder.toString()).getAsJsonObject();
                    String assistantResponse = responseJson
                            .getAsJsonArray("choices")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content")
                            .getAsString();

                    // Return the assistant's response
                    return assistantResponse;
                }
            } else {
                // Error response
                System.err.println("[ERROR] - HTTP " + statusCode);
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorBuilder = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorBuilder.append(errorLine.trim());
                    }
                    System.err.println("[ERROR] - Details: " + errorBuilder.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] - Failed to send prompt to the OpenAI API");
            e.printStackTrace();
        }
        return null;
    }
}
