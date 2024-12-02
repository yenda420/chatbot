package app.services;

import app.enums.QuestionTypeEnum;
import app.models.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AIValidatorService extends AIService {
    // Validates the assistant's response and handles corrections
    public static String validateOutput(String assistantResponse, AITestGeneratorService ai, Test test) {
        int attempts = 0;
        final int maxAttempts = 5;

        while (attempts < maxAttempts) {
            List<String> issues = checkFormatIssues(assistantResponse, test);

            if (issues.isEmpty()) {
                return assistantResponse;
            } else {
                StringBuilder correctionPrompt = new StringBuilder();
                attempts++;

                correctionPrompt.append("Ve tvé předchozí odpovědi jsem našel následující problémy:\n");

                for (String issue : issues) {
                    correctionPrompt.append("- ").append(issue).append("\n");
                }

                correctionPrompt.append("Prosím, oprav tyto problémy a znovu vytvoř test ve správném formátu.");

                assistantResponse = ai.askAI(correctionPrompt.toString());

                if (assistantResponse == null) {
                    System.err.println("[ERROR] - Failed to get a response from the assistant.");
                    return null;
                }

                System.out.println("[INFO] - Attempt " + attempts + ": " + correctionPrompt);
            }
        }

        return null;
    }


    // Checks for format issues in the assistant's response
    private static List<String> checkFormatIssues(String response, Test test) {
        QuestionTypeEnum questionType = test.getQuestionType();
        List<String> issues = new ArrayList<>();
        String[] lines = response.split("\n");
        int questionNumber = 1;
        int i;

        // Common checks for all question types
        if (!response.contains("Předmět:")) {
            issues.add("Chybí 'Předmět:'.");
        }
        if (!response.contains("Témata:")) {
            issues.add("Chybí 'Témata:'.");
        }
        if (!response.contains("Obtížnost:")) {
            issues.add("Chybí 'Obtížnost:'.");
        }
        if (!response.contains("Časový limit:")) {
            issues.add("Chybí 'Časový limit:'.");
        }
        if (!response.contains("Správné odpovědi:")) {
            issues.add("Chybí 'Správné odpovědi:'.");
        }
        if (!response.contains("Vysvětlení")) {
            issues.add("Chybí 'Vysvětlení:'.");
        }

        // Remove unwanted content
        if (!lines[0].contains("Název testu:")) {
            // Remove the first line by creating a new array starting from index 1
            lines = Arrays.copyOfRange(lines, 1, lines.length);

        }

        if (questionType == QuestionTypeEnum.YES_NO) {
            // Check that all questions have 'Ano / Ne'
            for (i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                // Check if the line starts with a question number followed by a period and a space
                if (line.matches("^\\d+\\. .*")) {
                    if (i + 1 >= lines.length || !lines[i + 1].trim().equals("Ano / Ne")) {
                        System.out.println(line);
                        System.out.println(lines[i + 1]);
                        System.out.println(lines[i + 1].trim());
                        issues.add("Otázka " + questionNumber + " nemá možnosti 'Ano / Ne'.");
                    }
                    questionNumber++;
                }

                if (questionNumber > test.getNumberOfQuestions()) break;
            }
        } else if (questionType == QuestionTypeEnum.MULTIPLE_CHOICE) {
            // Check that all questions have options a), b), c)
            for (i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                // Check if the line starts with a question number followed by a period and a space
                if (line.matches("^\\d+\\. .*")) {
                    boolean hasOptionA = false;
                    boolean hasOptionB = false;
                    boolean hasOptionC = false;

                    int j;

                    for (j = i + 1; j < lines.length; j++) {
                        String optionLine = lines[j].trim();

                        if (optionLine.startsWith("a)")) {
                            hasOptionA = true;
                        } else if (optionLine.startsWith("b)")) {
                            hasOptionB = true;
                        } else if (optionLine.startsWith("c)")) {
                            hasOptionC = true;
                        } else if (optionLine.isEmpty() || optionLine.matches("^\\d+\\. .*")) {
                            break;
                        }
                    }

                    if (!(hasOptionA && hasOptionB && hasOptionC)) {
                        issues.add("Otázka " + questionNumber + " nemá všechny možnosti 'a)', 'b)', 'c)'.");
                    }
                    questionNumber++;
                }

                if (questionNumber > test.getNumberOfQuestions()) break;
            }
        } else if (questionType == QuestionTypeEnum.OPEN_ENDED) {
            // For open-ended questions, make sure that no multiple-choice options are present
            for (i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                // Check if the line starts with a question number followed by a period and a space
                if (line.matches("^\\d+\\. .*")) {
                    if (i + 1 < lines.length) {
                        String nextLine = lines[i + 1].trim();
                        if (nextLine.startsWith("a)") || nextLine.startsWith("b)") || nextLine.startsWith("c)") || nextLine.equals("Ano / Ne")) {
                            issues.add("Otázka " + questionNumber + " by měla být otevřená, ale obsahuje možnosti.");
                        }
                    }
                    questionNumber++;
                }

                if (questionNumber > test.getNumberOfQuestions()) break;
            }
        }

        return issues;
    }

    // Fact-checks the assistant's response and handles corrections
    public static String factCheckTest(String testContent, AITestGeneratorService aiTestGeneratorService, Test test) {
        int attempts = 0;
        final int maxAttempts = 5;

        while (attempts < maxAttempts) {
            StringBuilder factCheckPrompt = new StringBuilder();

            factCheckPrompt.append("Důkladně zkontroluj a ověř fakta v následujícím testu.")
                    .append(" Test obsahuje název, předmět, temata, obtížnost, časový limit, otázky a jejich odpovědi a ke každé odpovědi vysvětlení.")
                    .append(" Převážně zkontroluj, zda-li jsou dané odpovědi ke každé otázce přiřazeny a vysvětleny správně.")
                    .append(" V případě testu, kde se vybírá z odpovědí se také důkladně ujisti, že se ve výběru pod otázkou nachází pouze jedna správná odpověď.")
                    .append(" V případě, že najdeš nějaké chyby, oprav je a vypiš mi pouze opravený test v přesně stejném formátu, jaký jsi dostal.")
                    .append(" V případě, že nenajdeš žádnou chybu vypiš mi pouze test, který jsi dostal v přesně stejném formátu.")
                    .append(" Opravuj pouze otázky, u kterých si jsi opravdu jistý, že jsou špatně.")
                    .append(" Zde je test pro kontrolu: ").append(testContent);

            testContent = aiTestGeneratorService.askAI(factCheckPrompt.toString());

            if (testContent == null) {
                System.err.println("[ERROR] - Failed to get a response from the assistant during fact-checking.");
                return null;
            }

            // Re-validate format after fact-checking
            List<String> issues = checkFormatIssues(testContent, test);

            if (issues.isEmpty()) {
                return testContent;
            } else {
                attempts++;
                StringBuilder correctionPrompt = new StringBuilder();
                correctionPrompt.append("Ve tvé opravené odpovědi jsem našel následující formátovací problémy:\n");

                for (String issue : issues) {
                    correctionPrompt.append("- ").append(issue).append("\n");
                }

                correctionPrompt.append("Prosím, oprav tyto formátovací problémy a opět mi pošli celý test ve správném formátu.");

                testContent = aiTestGeneratorService.askAI(correctionPrompt.toString());

                if (testContent == null) {
                    System.err.println("[ERROR] - Failed to get a response from the assistant during fact-checking.");
                    return null;
                }
            }
        }

        return null;
    }
}
