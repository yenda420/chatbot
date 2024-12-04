package app.services;

import app.enums.QuestionTypeEnum;
import app.models.Test;

import java.util.*;

public class AIValidatorService extends AIService {
    private static final int MAX_ATTEMPTS = 5;
    private int attempts = 0; // Instance variable to keep track of attempts

    // Validates the assistant's response and handles corrections
    public String validateOutput(String assistantResponse, AITestGeneratorService ai, Test test) {
        System.out.println("[INFO] - Validating output...");

        while (attempts < MAX_ATTEMPTS) {
            List<String> issues = checkFormatIssues(assistantResponse, test);

            if (issues.isEmpty()) {
                return removeUnwantedLines(assistantResponse);
            } else {
                StringBuilder correctionPrompt = new StringBuilder();
                attempts++;

                correctionPrompt.append("Ve tvé předchozí odpovědi jsem našel následující problémy:\n");

                for (String issue : issues) {
                    correctionPrompt.append("- ").append(issue).append("\n");
                }

                correctionPrompt
                        .append("Prosím, oprav tyto problémy a znovu vytvoř test ve správném formátu. ")
                        .append("Ujisti se, že vypíšeš všechny otázky a odpovědi v plném znění, bez použití '... a tak dále ...' nebo jiných zkratek. ")
                        .append("Dodrž přesně formátování uvedené v původním zadání. ")
                        .append("Nepřidávej žádné nové informace a neměň obsah otázek a odpovědí, pokud to není nutné kvůli opravě formátu.");

                assistantResponse = ai.askAI(correctionPrompt.toString());

                if (assistantResponse == null) {
                    System.err.println("[ERROR] - Failed to get a response from the assistant.");
                    return null;
                }

                System.out.println("[INFO] - Attempt " + attempts + ": \n" + correctionPrompt + "\n");
                System.out.println("[INFO] - New response number " + attempts + ": \n" + assistantResponse);
            }
        }

        System.err.println("[WARNING] - Failed to get a valid response from the assistant after " + MAX_ATTEMPTS + " attempts.");
        return null;
    }

    // Checks for format issues in the assistant's response
    private List<String> checkFormatIssues(String response, Test test) {
        System.out.println("[INFO] - Looking for format issues...");

        QuestionTypeEnum questionType = test.getQuestionType();
        List<String> issues = new ArrayList<>();
        String[] lines = response.split("\n");

        // Remove any leading empty lines
        int startIndex = 0;
        while (startIndex < lines.length && lines[startIndex].trim().isEmpty()) {
            startIndex++;
        }

        // Remove any trailing empty lines
        int endIndex = lines.length - 1;
        while (endIndex >= 0 && lines[endIndex].trim().isEmpty()) {
            endIndex--;
        }
        if (startIndex > endIndex) {
            issues.add("Text testu je prázdný.");
            return issues;
        }
        lines = Arrays.copyOfRange(lines, startIndex, endIndex + 1);

        // Required sections
        String[] requiredSections = {
                "Název testu:",
                "Předmět:",
                "Témata:",
                "Obtížnost:",
                "Časový limit:"
        };
        Set<String> requiredSectionsSet = new HashSet<>(Arrays.asList(requiredSections));
        Set<String> foundSections = new HashSet<>();

        // Scan through lines to find required sections
        for (String line : lines) {
            String trimmedLine = line.trim();
            // Check if the line starts with any of the required sections
            for (String section : requiredSections) {
                if (trimmedLine.startsWith(section)) {
                    foundSections.add(section);
                    break;
                }
            }
        }

        // Check for missing sections
        for (String section : requiredSections) {
            if (!foundSections.contains(section)) {
                issues.add("Chybí část '" + section + "'");
            }
        }

        // Initialize state variables
        boolean inQuestions = false;
        boolean inAnswers = false;
        boolean foundMaxPoints = false;
        int questionCount = 0;
        int answerCount = 0;

        int index = 0;

        while (index < lines.length) {
            String line = lines[index].trim();

            if (line.equalsIgnoreCase("Správné odpovědi:")) {
                inQuestions = false;
                inAnswers = true;
                index++;
                continue;
            }

            if (line.startsWith("Maximální počet bodů:")) {
                foundMaxPoints = true;
                inAnswers = false; // Reset the inAnswers flag
                index++;
                continue;
            }

            if (requiredSectionsSet.contains(line.split(":")[0] + ":")) {
                index++;
                continue;
            }

            if (!inQuestions && !inAnswers) {
                // Before questions
                if (line.matches("^\\d+\\.\\s+.*")) {
                    inQuestions = true;
                    continue; // Do not increment index here
                } else {
                    // Accept any introductory lines or apologies
                    index++;
                    continue;
                }
            }

            if (inQuestions) {
                if (line.matches("^\\d+\\.\\s+.*")) {
                    questionCount++;
                    int questionNumber = questionCount;
                    index++;

                    // Expect 'Body: [number]'
                    if (index < lines.length && lines[index].trim().startsWith("Body:")) {
                        index++;
                    } else {
                        issues.add("Otázka " + questionNumber + " nemá zadané 'Body:'.");
                    }

                    if (questionType == QuestionTypeEnum.YES_NO) {
                        // Expect 'Ano / Ne' with flexible matching
                        if (index < lines.length) {
                            String optionLine = lines[index].trim();
                            if (optionLine.matches("(?i)^(ano\\s*/\\s*ne)$")) {
                                index++;
                            } else {
                                issues.add("Otázka " + questionNumber + " nemá správnou volbu 'Ano / Ne'. Nalezeno: '" + optionLine + "'");
                            }
                        } else {
                            issues.add("Otázka " + questionNumber + " chybí možnosti 'Ano / Ne'.");
                        }
                    }
                    // Skip empty lines
                    while (index < lines.length && lines[index].trim().isEmpty()) {
                        index++;
                    }
                } else {
                    index++;
                }
            } else if (inAnswers) {
                if (line.matches("^\\d+\\.\\s+.*")) {
                    answerCount++;
                    int answerNumber = answerCount;
                    index++;

                    // Expect 'Vysvětlení:'
                    if (index < lines.length && lines[index].trim().startsWith("Vysvětlení:")) {
                        index++;
                    } else {
                        issues.add("Odpověď " + answerNumber + " nemá 'Vysvětlení:'.");
                        index++; // Increment index to move to the next line
                    }

                    // Skip empty lines
                    while (index < lines.length && lines[index].trim().isEmpty()) {
                        index++;
                    }
                } else {
                    index++;
                }
            } else {
                index++;
            }
        }

        if (questionCount != test.getNumberOfQuestions()) {
            issues.add("Počet otázek (" + questionCount + ") neodpovídá očekávanému počtu (" + test.getNumberOfQuestions() + "). " +
                    "Otázek opravdu musí být všech " + test.getNumberOfQuestions() + ".");
        }

        if (answerCount != test.getNumberOfQuestions()) {
            issues.add("Počet odpovědí (" + answerCount + ") neodpovídá počtu otázek (" + test.getNumberOfQuestions() + "). " +
                    "Ujisti se, že každá otázka má právě jednu správnou odpověď níže v sekci 'Správné odpovědi'.");
        }

        if (!foundMaxPoints) {
            issues.add("Chybí 'Maximální počet bodů:'.");
        }

        // Check for incomplete content
        String responseLowerCase = response.toLowerCase(Locale.ROOT);
        if (responseLowerCase.contains("a tak dále") || responseLowerCase.contains("...") ||
                responseLowerCase.contains("a podobně") || responseLowerCase.contains("atd")) {
            issues.add("Zdá se, že test není kompletní. Prosím, vypiš všechny otázky a odpovědi v plném znění, " +
                    "bez použití zkratek jako '... a tak dále ...'. Ke každé otázce musí být právě jedna správná odpověď níže v sekci 'Správné odpovědi'.");
        }

        return issues;
    }

    public String factCheckTest(String testContent, AITestGeneratorService aiTestGeneratorService, Test test) {
        AIService ai = new AIService();

        System.out.println("[INFO] - Fact checking test...");

        String factCheckPrompt = "Důkladně zkontroluj a ověř fakta v následujícím testu." +
                " Test obsahuje název, předmět, temata, obtížnost, časový limit, otázky a jejich odpovědi a ke každé odpovědi vysvětlení." +
                " Převážně zkontroluj, zda-li jsou dané odpovědi ke každé otázce přiřazeny a vysvětleny správně." +
                " V případě testu, kde se vybírá z odpovědí se také důkladně ujisti, že se ve výběru pod otázkou nachází pouze jedna správná odpověď." +
                " V případě, že najdeš nějaké chyby, oprav je a vypiš mi pouze opravený test v přesně stejném formátu, jaký jsi dostal." +
                " V případě, že nenajdeš žádnou chybu vypiš mi pouze test, který jsi dostal v přesně stejném formátu." +
                " Opravuj pouze otázky, u kterých si jsi opravdu jistý, že jsou špatně." +
                " Zde je test pro kontrolu: " + testContent;

        testContent = ai.askAI(factCheckPrompt);

        if (testContent == null) {
            System.err.println("[ERROR] - Failed to get a response from the assistant during fact-checking.");
            return null;
        }

        // Re-validate format after fact-checking using the same validator instance
        testContent = validateOutput(testContent, aiTestGeneratorService, test);

        if (testContent != null) {
            System.out.println("[INFO] - Test fact-checked successfully.");
            return testContent;
        } else {
            System.err.println("[ERROR] - Failed to fact-check the test.");
            return null;
        }
    }

    // This method assumes the test is already correctly formatted
    // The testContent parameter has to contain "Název testu:" and "Maximální počet bodů:"
    private String removeUnwantedLines(String testContent) {
        String[] lines = testContent.split("\n");

        // Remove any leading empty lines
        int startIndex = 0;
        while (startIndex < lines.length && (lines[startIndex].trim().isEmpty() || lines[startIndex].contains("Název testu:"))) {
            //System.out.println("Start index line " + startIndex + ": " + lines[startIndex]);
            startIndex++;
        }

        startIndex--;

        // Remove any trailing empty lines
        int endIndex = lines.length - 1;
        while (endIndex >= 0 && (lines[startIndex].trim().isEmpty() || lines[startIndex].contains("Maximální počet bodů:"))) {
            //System.out.println("End index line " + endIndex + ": " + lines[endIndex]);
            endIndex--;
        }

        System.out.println("[INFO] - Removing lines between: " + lines[startIndex] + " \nand: " + lines[endIndex]);

        if (startIndex > endIndex) {
            System.err.println("[ERROR] - The testContent parametr wasnt formatted correctly.");
            return null;
        }

        // Remove all lines except the ones between startIndex and endIndex
        lines = Arrays.copyOfRange(lines, startIndex, endIndex + 1);

        return String.join("\n", lines);
    }
}