package app.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class AIService {
    private static final Dotenv dotenv = Dotenv.load();

    protected static final String OPENAI_API_KEY = dotenv.get("OPENAI_API_KEY");
    protected static final String OPENAI_API_URL = dotenv.get("OPENAI_API_URL");
    protected static final String OPENAI_MODEL = dotenv.get("OPENAI_MODEL");
    protected static final double OPENAI_TEMPERATURE = Double.parseDouble(dotenv.get("OPENAI_TEMPERATURE"));

    protected static URL url;
    private JsonArray conversationHistory;

    public AIService() {
        try {
            // Set up the connection URL
            url = new URL(OPENAI_API_URL);
            // Initialize the conversation history
            conversationHistory = new JsonArray();
        } catch (IOException e) {
            System.err.println("[ERROR] - Failed to set up the connection to the OpenAI API");
            e.printStackTrace();
        }
    }

    // Method to send a single prompt
    public String askAI(String userPrompt) {
        if (userPrompt == null) {
            System.err.println("[ERROR] - User prompt is null or empty.");
            return null;
        }

        // Add the user's message to the conversation history
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userPrompt);
        conversationHistory.add(userMessage);

        // Call the helper method with the entire conversation history
        return sendRequestToAI(conversationHistory);
    }

    // Method to send a list of messages
    public String askAI(ArrayList<JsonObject> messagesToSend) {
        if (messagesToSend == null || messagesToSend.isEmpty()) {
            System.err.println("[ERROR] - Messages to send are null or empty.");
            return null;
        }

        // Add the messages to the conversation history
        for (JsonObject message : messagesToSend) {
            conversationHistory.add(message);
        }

        // Call the helper method with the updated conversation history
        return sendRequestToAI(conversationHistory);
    }

    // Private helper method to handle the request
    private String sendRequestToAI(JsonArray messagesArray) {
        try {
            // Prepare the payload
            JsonObject payload = new JsonObject();
            payload.addProperty("model", OPENAI_MODEL);
            payload.addProperty("temperature", OPENAI_TEMPERATURE);
            payload.addProperty("max_tokens", 3500);
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
            if (statusCode == 200) {
                // Success response
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

                    // Add the assistant's message to the conversation history
                    JsonObject assistantMessage = new JsonObject();
                    assistantMessage.addProperty("role", "assistant");
                    assistantMessage.addProperty("content", assistantResponse);
                    conversationHistory.add(assistantMessage);

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