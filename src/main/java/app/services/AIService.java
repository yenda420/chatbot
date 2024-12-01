package app.services;

import app.dao.SubjectManager;
import app.models.Prompt;
import app.models.Subject;
import app.models.Test;
import app.models.Topic;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AIService {
    private URL url;
    private List<JsonObject> messages;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = "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";
    private static final String OPENAI_MODEL = "gpt-3.5-turbo";
    private static final double OPENAI_TEMPERATURE = 0.7;

    public AIService() {
        try {
            // Set up the connection URL
            url = new URL(OPENAI_API_URL);
            // Initialize the messages list
            messages = new ArrayList<>();
        } catch (IOException e) {
            System.err.println("[ERROR] - Failed to set up the connection to the OpenAI API");
            e.printStackTrace();
        }
    }

    // Set the initial context for the AI
    public void setContext() {
        StringBuilder systemPromptBuilder = new StringBuilder();
        systemPromptBuilder.append("Jsi AI asistent pověřený generováním testů na základě poskytnutých parametrů.")
                .append(" Ujisti se, že ve svém testu nemáš žádné chyby a že máš správnou gramatiku.")
                .append(" Ujisti se taky, že zahrneš vždy vysvětlení každé správné odpovědi.")
                .append(" Prosím, vytvoř test v následujícím formátu:\n\n")
                .append("NÁZEV TESTU\n")
                .append("Předmět: ...\n")
                .append("Témata: ...\n\n")
                .append("Obtížnost: ...\n")
                .append("Časový limit: ...\n\n")

                .append("1. (text 1. otázky)\n")
                .append("... bodů (počet bodů této otázky) \n")
                .append("a) ...\n")
                .append("b) ...\n")
                .append("c) ...\n\n")

                .append("2. (text 2. otázky)\n")
                .append("... bodů (počet bodů této otázky) \n")
                .append("a) ...\n")
                .append("b) ...\n")
                .append("c) ...\n\n")

                .append("... a tak dále ...\n\n")

                .append("Správné odpovědi:\n\n")

                .append("1. (text 1. odpovědi)\n")
                .append("Vysvětlení (1. odpovědi): ...\n\n")

                .append("2. (text 2. odpovědi)\n")
                .append("Vysvětlení (2. odpovědi): ...\n\n")

                .append("... a tak dále ...\n\n")

                .append("Maximální počet bodů: ... (součet bodů ze všech otázek)\n");

        // Add the system prompt to the messages list
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", systemPromptBuilder.toString());
        messages.add(systemMessage);

        // Optionally, let the AI confirm that it understands
        String assistantResponse = askAI("");
        if (assistantResponse != null) {
            System.out.println("[INFO] - AI Assistant Context Confirmation:\n" + assistantResponse);
        }
    }

    public String generateTest(Test test) throws SQLException {
        // Extract attributes from the Test object
        Prompt prompt = test.getFromPrompt();
        if (prompt == null) {
            System.err.println("[ERROR] - Missing prompt information in the test.");
            return null;
        }

        ArrayList<Topic> topicsList = prompt.getTopics();
        if (topicsList == null || topicsList.isEmpty()) {
            System.err.println("[ERROR] - Missing topics in the Prompt object.");
            return null;
        }

        // Assume that the first topic determines the subject
        String firstTopicName = topicsList.get(0).getName();
        Subject subject = SubjectManager.getTopicSubject(firstTopicName);
        if (subject == null) {
            System.err.println("[ERROR] - Subject not found for topic: " + firstTopicName);
            return null;
        }

        String subjectName = subject.getName();
        String testName = test.getName();
        int numberOfQuestions = test.getNumberOfQuestions();
        int timeLimit = test.getTimeLimitInMinutes();
        String difficulty = test.getDifficulty().getName();
        String questionType = test.getQuestionType().getName();

        // Information from the Prompt object
        String messageContent = prompt.getMessage();
        File attachedFile = prompt.getAttachedFile();

        // List of topics
        List<String> topics = new ArrayList<>();
        for (Topic topic : topicsList) {
            topics.add(topic.getName());
        }

        // Set the context
        setContext();

        // Build the user prompt with the test attributes in Czech
        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder.append("Vygenerujte test s následujícími parametry. ")
                .append(" Dodrž stejné formátování, které jsi použil u předchozího testu.\n\n")
                .append("Název testu: ").append(testName).append("\n")
                .append("Předmět: ").append(subjectName).append("\n")
                .append("Témata: ").append(String.join(", ", topics)).append("\n")
                .append("Obtížnost: ").append(difficulty).append("\n")
                .append("Počet otázek: ").append(numberOfQuestions).append("\n")
                .append("Typ otázek: ").append(questionType).append("\n")
                .append("Časový limit: ").append(timeLimit).append(" minut\n");

        // Include additional message content if provided
        if (messageContent != null && !messageContent.isEmpty()) {
            userPromptBuilder.append("Dodatečné instrukce: ").append(messageContent).append("\n");
        }

        // Include attached file content if provided
        if (attachedFile != null) {
            try {
                String fileContent = readFileContent(attachedFile);
                userPromptBuilder.append("Obsah přiloženého souboru:\n").append(fileContent).append("\n");
            } catch (IOException e) {
                System.err.println("[ERROR] - Failed to read the attached file.");
                e.printStackTrace();
            }
        }

        // Send the prompt to the AI and get the response
        String assistantResponse = askAI(userPromptBuilder.toString());

        return assistantResponse;
    }

    private String readFileContent(File file) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        // Assuming the file is text-based
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        }
        return contentBuilder.toString();
    }

    // Sends a prompt to the AI assistant and returns the assistant's response.
    private String askAI(String userPrompt) {
        try {
            if (userPrompt != null && !userPrompt.isEmpty()) {
                // Add the user's message to the conversation
                JsonObject userMessage = new JsonObject();
                userMessage.addProperty("role", "user");
                userMessage.addProperty("content", userPrompt);
                messages.add(userMessage);
            }

            // Prepare the payload with the accumulated messages
            JsonArray messagesArray = new JsonArray();
            for (JsonObject message : messages) {
                messagesArray.add(message);
            }
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

                    // Add the assistant's response to the conversation
                    JsonObject assistantMessage = new JsonObject();
                    assistantMessage.addProperty("role", "assistant");
                    assistantMessage.addProperty("content", assistantResponse);
                    messages.add(assistantMessage);

                    // Return the assistant's response
                    return assistantResponse;
                }
            } else {
                System.err.println("[ERROR] - HTTP " + statusCode);
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorBuilder = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorBuilder.append(errorLine.trim());
                    }
                    System.err.println("[ERROR] - Details: " + errorBuilder);
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] - Failed to send prompt to the OpenAI API");
            e.printStackTrace();
        }
        return null;
    }

    public boolean writeTestToFile(String testContent, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            writer.write(testContent);
            System.out.println("[INFO] - Test written to file: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("[ERROR] - Failed to write test to file.");
            e.printStackTrace();
        }
        return false;
    }
}