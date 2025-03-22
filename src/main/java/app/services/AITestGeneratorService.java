package app.services;

import app.dao.SubjectManager;
import app.models.Prompt;
import app.models.Subject;
import app.models.Test;
import app.models.Topic;
import app.enums.QuestionTypeEnum;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

public class AITestGeneratorService extends AIService {
    private ArrayList<JsonObject> messages;

    public AITestGeneratorService() {
        messages = new ArrayList<>();
    }

    // Gets the initial context for the AI Test Generator
    public static String getContextFor(Test test) {
        StringBuilder setContextPrompt = new StringBuilder();

        setContextPrompt
                .append("Jsi AI asistent pověřený generováním testů na základě poskytnutých parametrů.")
                .append(" Ujisti se, že ve svém testu nemáš žádné chyby a že používáš správnou gramatiku.")
                .append(" Ujisti se, že vypíšeš všechny otázky a odpovědi v plném rozsahu, bez použití zkratek jako '... a tak dále ...' nebo 'atd.'.")
                .append(" Ujisti se také, že vždy zahrneš vysvětlení každé správné odpovědi.")
                .append(" Důkladně ověř fakta a informace, které uvádíš, aby byla zajištěna jejich správnost.")
                .append(" Pokud si nejsi jistý některými informacemi, raději je neuváděj.")
                .append(" Testy jsou pro žáky středních škol, ujisti se tedy, že tomu odpovídá obtížnost testů.")
                .append(" Pokud je test z cizího jazyka (například z angličtiny), ujisti se, že otázky i odpovědi jsou právě v tomto cizím jazyce.");

        // Add specific instructions based on the question type
        if (test.getQuestionType() == QuestionTypeEnum.YES_NO) {
            setContextPrompt
                    .append(" Vytvářej test pouze s otázkami typu Ano/Ne.")
                    .append(" Ujisti se, že ke každé otázce uvedeš možnost 'Ano / Ne'.");
        } else if (test.getQuestionType() == QuestionTypeEnum.MULTIPLE_CHOICE) {
            setContextPrompt
                    .append(" Vytvářej test pouze s otázkami, kde se vybírá z možností.")
                    .append(" Ke každé otázce vždy uveď tři možnosti označené 'a)', 'b)', 'c)'.")
                    .append(" Ujisti se, že pouze jedna z možností je správná.");
        } else if (test.getQuestionType() == QuestionTypeEnum.OPEN_ENDED) {
            setContextPrompt
                    .append(" Vytvářej test pouze s otevřenými otázkami.")
                    .append(" Otevřené otázky jsou takové, kde student sám vypracuje odpověď.");
        }

        setContextPrompt
                .append(" Prosím, vytvoř test v následujícím formátu:\n\n")

                .append("Název testu: [Název testu]\n")
                .append("Předmět: [Název předmětu]\n")
                .append("Témata: [Názvy témat]\n\n")

                .append("Obtížnost: [Obtížnost]\n")
                .append("Časový limit: [Časový limit]\n\n");

        // Add the specific format based on question type
        if (test.getQuestionType() == QuestionTypeEnum.YES_NO) {
            setContextPrompt
                    .append("1. [Text 1. otázky]\n")
                    .append("Body: [Body 1. otázky]\n")
                    .append("Ano / Ne\n\n")

                    .append("2. [Text 2. otázky]\n")
                    .append("Body: [Body 2. otázky]\n")
                    .append("Ano / Ne\n\n")

                    .append("... a tak dále ...\n\n")

                    .append("Správné odpovědi:\n")
                    .append("1. [Správná odpověď 1]\n")
                    .append("Vysvětlení: [Vysvětlení 1. otázky]\n\n")

                    .append("2. [Správná odpověď 2]\n")
                    .append("Vysvětlení: [Vysvětlení 2. otázky]\n\n")

                    .append("... a tak dále ...\n\n");
        } else if (test.getQuestionType() == QuestionTypeEnum.MULTIPLE_CHOICE) {
            setContextPrompt
                    .append("1. [Text 1. otázky]\n")
                    .append("Body: [Body 1. otázky]\n")
                    .append("a) [Odpověď 1.1]\n")
                    .append("b) [Odpověď 1.2]\n")
                    .append("c) [Odpověď 1.3]\n\n")

                    .append("2. [Text 2. otázky]\n")
                    .append("Body: [Body 2. otázky]\n")
                    .append("a) [Odpověď 2.1]\n")
                    .append("b) [Odpověď 2.2]\n")
                    .append("c) [Odpověď 2.3]\n\n")

                    .append("... a tak dále ...\n\n")

                    .append("Správné odpovědi:\n")
                    .append("1. [Správná odpověď 1]\n")
                    .append("Vysvětlení: [Vysvětlení 1. otázky]\n\n")

                    .append("2. [Správná odpověď 2]\n")
                    .append("Vysvětlení: [Vysvětlení 2. otázky]\n\n")

                    .append("... a tak dále ...\n\n");
        } else if (test.getQuestionType() == QuestionTypeEnum.OPEN_ENDED) {
            setContextPrompt
                    .append("1. [Text 1. otázky]\n")
                    .append("Body: [Body 1. otázky]\n\n")

                    .append("2. [Text 2. otázky]\n")
                    .append("Body: [Body 2. otázky]\n\n")

                    .append("... a tak dále ...\n\n")

                    .append("Správné odpovědi:\n")
                    .append("1. [Správná odpověď 1]\n\n")

                    .append("2. [Správná odpověď 2]\n\n")

                    .append("... a tak dále ...\n\n");
        }

        setContextPrompt.append("Maximální počet bodů: [Maximální počet bodů]\n");

        return setContextPrompt.toString();
    }

    public String generateTest(Test test) throws SQLException {
        // Extract attributes from the Test object
        Prompt prompt = test.getPrompt();

        if (prompt == null) {
            LogService.logError("Missing prompt information in the test.");
            return null;
        }

        ArrayList<Topic> topicsList = prompt.getTopics();

        if (topicsList == null || topicsList.isEmpty()) {
            LogService.logError("Missing topics in the Prompt object.");
            return null;
        }

        String firstTopicName = topicsList.get(0).getName();
        Subject subject = SubjectManager.getTopicsSubject(firstTopicName);

        if (subject == null) {
            LogService.logError("Subject not found for topic: " + firstTopicName);
            return null;
        }

        String subjectName = subject.getName();
        String testName = test.getName();
        String difficulty = test.getDifficulty().getName();
        String questionType = test.getQuestionType().getName();
        int numberOfQuestions = test.getNumberOfQuestions();
        int timeLimit = test.getTimeLimitInMinutes();

        // Information from the Prompt object
        String messageContent = prompt.getMessage();
        File attachedFile = prompt.getAttachedFile();

        List<String> topics = new ArrayList<>();
        for (Topic topicObj : topicsList) {
            topics.add(topicObj.getName());
        }

        // Build the user prompt with the test attributes
        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder
                .append(getContextFor(test))
                .append("\nVygenerujte test s následujícími parametry.")
                .append(" Dodrž stejné formátování, které jsi použil u předchozího testu.")
                .append(" Důkladně ověř správnost všech informací a faktů ve tvém testu.\n\n")

                .append("Název testu: ").append(testName).append("\n")
                .append("Předmět: ").append(subjectName).append("\n")
                .append("Témata: ").append(String.join(", ", topics)).append("\n")
                .append("Obtížnost: ").append(difficulty).append("\n")
                .append("Počet otázek: ").append(numberOfQuestions).append("\n")
                .append("Typ otázek: ").append(questionType).append("\n")
                .append("Časový limit: ").append(timeLimit).append(" minut\n");

        // Include additional message content if provided
        if (messageContent != null && !messageContent.isEmpty() && !messageContent.isBlank()) {
            userPromptBuilder.append("Dodatečné instrukce: ").append(messageContent).append("\n");
        }

        // Include attached file content if provided
        if (attachedFile != null) {
            try {
                String fileContent = FileService.readFileContent(attachedFile);

                if (!fileContent.isEmpty() && !fileContent.isBlank()) {
                    userPromptBuilder.append("Obsah přiloženého souboru s názvem '").append(attachedFile.getName()).append("':\n").append(fileContent).append("\n");
                }
            } catch (IOException e) {
                LogService.logError("Failed to read the attached file. Error: " + e.getMessage());
            }
        }

        JsonObject userPromptJson = stringPromptToJsonObject(userPromptBuilder.toString());
        messages.add(userPromptJson);

        LogService.logInfo("Prompting the AI...\n");

        // Send the prompt to the AI and get the response
        String response = askAI(messages);

        LogService.logInfo("Assistant response:\n" + response + "\n");

        AIValidatorService validator = new AIValidatorService();

        // Validate the output and handle corrections
        return validator.validateOutput(response, this, test);
    }

    private JsonObject stringPromptToJsonObject(String prompt) {
        if (prompt == null || prompt.isEmpty() || prompt.isBlank()) {
            return null;
        }

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);

        return userMessage;
    }
}