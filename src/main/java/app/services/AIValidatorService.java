package app.services;

import app.dao.TestManager;
import app.enums.QuestionTypeEnum;
import app.models.Test;

import java.util.*;
import java.util.regex.Pattern;

public class AIValidatorService extends AIService {
    private static final int MAX_ATTEMPTS = 5;
    private int attempts = 0;

    // Validates the assistant's response and handles corrections
    public String validateOutput(String assistantResponse, AITestGeneratorService ai, Test test) {
        LogService.logInfo("Validating output...");

        while (attempts < MAX_ATTEMPTS) {
            List<String> issues = checkFormatIssues(assistantResponse, test);

            if (issues == null) {
                LogService.logError("Failed to check format issues.");
                return null;
            }

            if (issues.isEmpty()) {
                AIValidatorService validator = new AIValidatorService();

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
                    .append("Ujisti se, že test obsahuje všech ").append(test.getNumberOfQuestions())
                    .append(" kompletních otázek, každá s jednou správnou odpovědí a vysvětlením. ")
                    .append("Nepoužívej placeholdery ani zkratky. ")
                    .append("Dodrž přesně formátování uvedené v původním zadání.");

            assistantResponse = ai.askAI(correctionPrompt.toString());

            if (assistantResponse == null) {
                LogService.logError("Failed to get a response from the assistant.");
                return null;
            }

            LogService.logInfo("Attempt " + attempts + ": \n" + correctionPrompt + "\n");
            LogService.logInfo("New response number " + attempts + ": \n" + assistantResponse);
        }

        LogService.logError("Failed to get a valid response from the assistant after " + MAX_ATTEMPTS + " attempts.");
        return null;
    }

    // Checks for format issues in the assistant's response
    private List<String> checkFormatIssues(String response, Test test) {
        LogService.logInfo("Looking for format issues...");

        QuestionTypeEnum questionType = test.getQuestionType();
        List<String> issues = new ArrayList<>();
        String[] lines;

        try {
            lines = response.split("\n");
        } catch (Exception e) {
            LogService.logError("Failed to split response into lines.");
            return null;
        }

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
        String[] requiredSections = TestManager.requiredSections;

        // Using HashSet for effective lookup
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
            Pattern questionPattern = Pattern.compile("^\\d+\\.\\s+.*");

            String line = lines[index].trim();

            // Does line match a question or answer placeholder
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
                inAnswers = false;
                index++;
                continue;
            }

            // Skip required sections
            if (requiredSectionsSet.contains(line.split(":")[0] + ":")) {
                index++;
                continue;
            }

            // Before questions
            if (!inQuestions && !inAnswers) {
                if (questionPattern.matcher(line).matches()) {
                    // First questions line, do not increment index here
                    inQuestions = true;
                    continue;
                } else {
                    // Above first questions line
                    index++;
                    continue;
                }
            }

            if (inQuestions) {
                // Pattern example: "1. Otazka"
                if (questionPattern.matcher(line).matches()) {
                    index++;
                    questionCount++;
                    int questionNumber = questionCount;
                    boolean pointsFound = false;

                    while (index < lines.length) {
                        if (lines[index].startsWith("Body:")) {
                            pointsFound = true;
                            index++;
                            break;
                        }

                        if (questionPattern.matcher(lines[index]).matches()) {
                            index++;
                            break;
                        }

                        index++;
                    }

                    // Start with 'Body: [number]'
                    if (!pointsFound) {
                        issues.add("Otázka " + questionNumber + " nemá zadané 'Body:'.");
                    }

                    if (questionType == QuestionTypeEnum.YES_NO) {
                        if (index < lines.length) {
                            String optionLine = lines[index].trim();
                            // Does optionLine exactly match "ano / ne" (case-insensitive)
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
                // Pattern example: "1. Odpověď"
                if (questionPattern.matcher(line).matches()) {
                    index++;
                    answerCount++;
                    int answerNumber = answerCount;

                    boolean foundExplanation = false;

                    // Read lines until explanation is found or next answer starts
                    while (index < lines.length) {
                        String currentLine = lines[index].trim();

                        if (currentLine.startsWith("Vysvětlení:")) {
                            foundExplanation = true;
                            index++;

                            // Read explanation lines until next answer or end of answers
                            while (index < lines.length) {
                                String explanationLine = lines[index].trim();

                                // Does the explanation end
                                if (questionPattern.matcher(explanationLine).matches() || explanationLine.startsWith("Maximální počet bodů:")) {
                                    // Reached next answer or end of answers
                                    break;
                                }

                                index++;
                            }
                            break;
                        } else if (questionPattern.matcher(currentLine).matches() || currentLine.startsWith("Maximální počet bodů:")) {
                            // No explanation provided before next answer or end
                            break;
                        } else {
                            index++;
                        }
                    }

                    // Add issue only if question type is not open-ended
                    if (!foundExplanation && test.getQuestionType() != QuestionTypeEnum.OPEN_ENDED) {
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

        // Locale.ROOT ensures lower case conversion independent to the language rules
        String responseLowerCase = response.toLowerCase(Locale.ROOT);
        if (responseLowerCase.contains("a tak dále") || responseLowerCase.contains("...")) {
            issues.add("Zdá se, že test není kompletní. Prosím, vypiš všechny otázky a odpovědi v plném znění, " +
                    "bez použití zkratek jako '... a tak dále ...', nebo '...' a podobně. " +
                    "Ke každé otázce **musí** být právě jedna správná odpověď níže v sekci 'Správné odpovědi'.");
        }

        return issues;
    }

    public boolean isFactuallyCorrect(String testContent) {
        AIService ai = new AIService();

        LogService.logInfo("Fact-checking the test...");

        String factCheckPrompt = "Prosím, proveď kontrolu faktických chyb v následujícím testu. "
                + "Jsou v testu nějaké faktické chyby v odpovědích nebo vysvětleních? "
                + "Odpověz pouze 'Ano' nebo 'Ne'. V případě, že ano, napiš 'Ano, [číslo otázky]'. ";

        factCheckPrompt += "\n\nTest:\n" + testContent;

        String assistantResponse = ai.askAI(factCheckPrompt);

        if (assistantResponse == null || assistantResponse.isEmpty()) {
            LogService.logError("Failed to get a response from the assistant during fact-checking.");
            return false;
        }

        // Locale.ROOT ensures lower case conversion independent to the language rules
        String responseLowerCase = assistantResponse.trim().toLowerCase(Locale.ROOT);
        if (responseLowerCase.startsWith("ne")) {
            // Assistant indicates no factual errors
            LogService.logInfo("Test fact-checked successfully with no errors.");
            return true;
        } else if (responseLowerCase.startsWith("ano")) {
            // Assistant indicates there are factual errors
            LogService.logWarning("Test contains factual errors.");
            return false;
        } else {
            // Unclear response, defaulting to false
            LogService.logWarning("Assistant response unclear. Assuming test has factual errors.");
            return false;
        }
    }
}