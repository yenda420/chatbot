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
                AIValidatorService validator = new AIValidatorService();
                assistantResponse = removeUnwantedLines(assistantResponse);

                if (validator.isFactuallyCorrect(assistantResponse)) {
                    return assistantResponse;
                } else {
                    issues.add("Test obsahuje faktické chyby, či misinformace.");
                    attempts++;
                    continue;
                }
            }

            StringBuilder correctionPrompt = new StringBuilder();
            attempts++;

            correctionPrompt.append("Ve tvé předchozí odpovědi jsem našel následující problémy:\n");

            for (String issue : issues) {
                correctionPrompt.append("- ").append(issue).append("\n");
            }

            correctionPrompt
                    .append("Prosím, oprav tyto problémy a znovu vytvoř celý test ve správném formátu. ")
                    .append("Ujisti se, že test obsahuje všech ").append(test.getNumberOfQuestions()).append(" kompletních otázek, každá s jednou správnou odpovědí a vysvětlením. ")
                    .append("Nepoužívej placeholdery ani zkratky. ")
                    .append("Dodrž přesně formátování uvedené v původním zadání.");

            assistantResponse = ai.askAI(correctionPrompt.toString());

            if (assistantResponse == null) {
                System.err.println("[ERROR] - Failed to get a response from the assistant.");
                return null;
            }

            System.out.println("[INFO] - Attempt " + attempts + ": \n" + correctionPrompt + "\n");
            System.out.println("[INFO] - New response number " + attempts + ": \n" + assistantResponse);
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

            if (line.matches(".*\\[Otázka \\d+\\].*") || line.matches(".*\\[Odpověď \\d+\\].*")) {
                issues.add("Používáš placeholdery jako '[Otázka " + questionCount + "]' nebo '[Odpověď " + answerCount + "]'. Prosím, nahraď je skutečnými otázkami a odpověďmi.");
            }

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

                    boolean foundVysvetleni = false;

                    // Read lines until 'Vysvětlení:' is found or next answer starts
                    while (index < lines.length) {
                        String currentLine = lines[index].trim();

                        if (currentLine.startsWith("Vysvětlení:")) {
                            foundVysvetleni = true;
                            index++;
                            // Read explanation lines until next answer or end of answers
                            while (index < lines.length) {
                                String explanationLine = lines[index].trim();

                                if (explanationLine.matches("^\\d+\\.\\s+.*") || explanationLine.startsWith("Maximální počet bodů:")) {
                                    // Reached next answer or end of answers
                                    break;
                                }

                                index++;
                            }
                            break;
                        } else if (currentLine.matches("^\\d+\\.\\s+.*") || currentLine.startsWith("Maximální počet bodů:")) {
                            // No 'Vysvětlení:' provided before next answer or end
                            break;
                        } else {
                            index++;
                        }
                    }

                    if (!foundVysvetleni && test.getQuestionType() != QuestionTypeEnum.OPEN_ENDED) {
                        issues.add("Odpověď " + answerNumber + " nemá 'Vysvětlení:'.");
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
            if (questionCount == test.getNumberOfQuestions()) {
                issues.add("Napsal jsi mi " + questionCount + " otázek, ale " + answerCount + " odpovědí. " +
                        "Ujisti se, že do sekce 'Správné odpovědi' jsi zapsal všech " + questionCount + " odpovědí.");
            } else {
                issues.add("Počet odpovědí (" + answerCount + ") neodpovídá počtu otázek (" + test.getNumberOfQuestions() + "). " +
                        "Ujisti se, že každá otázka má právě jednu správnou odpověď níže v sekci 'Správné odpovědi'.");
            }

        }

        if (!foundMaxPoints) {
            issues.add("Chybí 'Maximální počet bodů:'.");
        }

        // Check for incomplete content
        String responseLowerCase = response.toLowerCase(Locale.ROOT);
        if (responseLowerCase.contains("a tak dále") || responseLowerCase.contains("...") ||
                responseLowerCase.contains("a podobně") || responseLowerCase.contains("atd")) {
            issues.add("Zdá se, že test není kompletní. Prosím, vypiš všechny otázky a odpovědi v plném znění, " +
                    "bez použití zkratek jako '... a tak dále ...', nebo '...' a podobně. " +
                    "Ke každé otázce **musí** být právě jedna správná odpověď níže v sekci 'Správné odpovědi'.");
        }

        return issues;
    }

    public boolean isFactuallyCorrect(String testContent) {
        AIService ai = new AIService();

        System.out.println("[INFO] - Fact-checking the test.");

        String factCheckPrompt = "Prosím, proveď kontrolu faktických chyb v následujícím testu. "
                + "Jsou v testu nějaké faktické chyby v odpovědích nebo vysvětleních? "
                + "Odpověz pouze 'Ano' nebo 'Ne'. V případě, že ano, napiš 'Ano, [číslo otázky]'. ";

        factCheckPrompt += "\n\nTest:\n" + testContent;

        String assistantResponse = ai.askAI(factCheckPrompt);

        if (assistantResponse == null || assistantResponse.isEmpty()) {
            System.err.println("[ERROR] - Failed to get a response from the assistant during fact-checking.");
            return false; // Assume test has errors if AI does not respond
        }

        System.out.println("[INFO] - Assistant response during fact-checking: " + assistantResponse);

        // Parse the assistant's response
        String responseLower = assistantResponse.trim().toLowerCase(Locale.ROOT);
        if (responseLower.startsWith("ne")) {
            // Assistant indicates no factual errors
            System.out.println("[INFO] - Test fact-checked successfully with no errors.");
            return true;
        } else if (responseLower.startsWith("ano")) {
            // Assistant indicates there are factual errors
            System.out.println("[INFO] - Test contains factual errors.");
            return false;
        } else {
            // Unclear response, defaulting to false
            System.out.println("[WARNING] - Assistant response unclear. Assuming test has factual errors.");
            return false;
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

        if (startIndex > endIndex) {
            System.err.println("[ERROR] - The testContent parametr wasnt formatted correctly.");
            return null;
        }

        // Remove all lines except the ones between startIndex and endIndex
        lines = Arrays.copyOfRange(lines, startIndex, endIndex + 1);

        return String.join("\n", lines);
    }
}