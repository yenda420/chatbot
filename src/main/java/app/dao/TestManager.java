package app.dao;

import app.enums.DifficultyEnum;
import app.enums.QuestionTypeEnum;
import app.models.Answer;
import app.models.Question;
import app.models.Test;
import app.services.DatabaseService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class TestManager {
    private static DatabaseService db;

    public static final String[] requiredSections = {
            "Název testu:",
            "Předmět:",
            "Témata:",
            "Obtížnost:",
            "Časový limit:"
    };

    public TestManager() {
        db = new DatabaseService();
    }

    public static int getId(int promptId) throws SQLException {
        String sql = "SELECT testId FROM tests WHERE promptId = ?";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setInt(1, promptId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("testId");
                }
            }
        }

        System.err.println("[ERROR] - Test with promptId " + promptId + " not found in database[");
        return -1;
    }

    public static boolean save(Test test, int promptId) {
        if (db.getConn() != null) {
            if (promptId > 0) {
                String sql = "INSERT INTO tests (name, difficulty, numberOfQuestions, timeLimit, promptId) VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                    pstmt.setString(1, test.getName());
                    pstmt.setString(2, test.getDifficulty().getName());
                    pstmt.setInt(3, test.getNumberOfQuestions());
                    pstmt.setInt(4, test.getTimeLimitInMinutes());
                    pstmt.setInt(5, promptId);
                    pstmt.executeUpdate();

                    System.out.println("[INFO] - Test " + test + " inserted into database.");
                    return true;
                } catch (SQLException e) {
                    System.err.println("[ERROR] - Failed to save test " + test + " into database.");
                    e.printStackTrace();
                }
            } else {
                System.err.println("[ERROR] - promptId can't be 0.");
            }
        }
        return false;
    }

    public static boolean saveTestData(String testContent, Test test, int promptId) {
        try {
            Pattern questionPattern = Pattern.compile("^\\d+\\.\\s+.*");

            String[] lines = testContent.split("\n");

            QuestionTypeEnum questionType = test.getQuestionType();
            DifficultyEnum difficulty = test.getDifficulty();

            // Remove any leading unwanted lines
            int startIndex = 0;
            int endIndex = lines.length;

            while (startIndex < lines.length && (lineContainsAnyOf(requiredSections, lines[startIndex]) || !containsText(lines[startIndex]))) {
                startIndex++;
            }

            // Remove any unwanted lines
            lines = Arrays.copyOfRange(lines, startIndex, endIndex);

            // Current state variables
            boolean inAnswers;
            boolean inQuestions;

            int questionCount = 0;
            int answerCount = 0;
            int index = 0;

            List<Question> questions = new ArrayList<>();

            // Skip lines until we reach the first question
            while (index < lines.length && !questionPattern.matcher(lines[index]).matches()) {
                index++;
            }

            inQuestions = true;
            inAnswers = false;

            while (index < lines.length) {
                String line = lines[index].trim();

                if (line.equalsIgnoreCase("Správné odpovědi:")) {
                    // Switch to answers
                    inQuestions = false;
                    inAnswers = true;
                    index++;
                    continue;
                }

                if (line.startsWith("Maximální počet bodů:")) {
                    // End of test
                    break;
                }

                if (inQuestions) {
                    if (questionPattern.matcher(line).matches()) {
                        // Get the text after the question number
                        String questionText = line.substring(line.indexOf('.') + 1).trim();

                        // Check if next line exists
                        if (index + 1 >= lines.length) {
                            System.err.println("[ERROR] - Unexpected end of content when reading points for question " + (questionCount + 1));
                            return false;
                        }

                        // Get the number of points
                        String[] pointParts = lines[index + 1].split(":");
                        if (pointParts.length < 2) {
                            System.err.println("[ERROR] - Invalid format for points on line " + (index + 2));
                            return false;
                        }

                        String pointsString = pointParts[1].trim().split(" ")[0]; // Handle cases like '5 bodů'
                        int points = Integer.parseInt(pointsString);

                        Question question = new Question(questionText, questionType, difficulty, points);

                        index += 2; // Move index past 'Body:' line

                        List<Answer> answersForQuestion = new ArrayList<>();

                        if (questionType == QuestionTypeEnum.MULTIPLE_CHOICE) {
                            if (index + 2 >= lines.length) {
                                System.err.println("[ERROR] - Not enough lines for answers for question " + (questionCount + 1));
                                return false;
                            }

                            answersForQuestion.add(new Answer(lines[index].replace("a) ", "").trim()));
                            answersForQuestion.add(new Answer(lines[index + 1].replace("b) ", "").trim()));
                            answersForQuestion.add(new Answer(lines[index + 2].replace("c) ", "").trim()));

                            index += 3; // Move index past the options

                        } else if (questionType == QuestionTypeEnum.YES_NO) {
                            if (index >= lines.length) {
                                System.err.println("[ERROR] - Missing 'Ano / Ne' line for question " + (questionCount + 1));
                                return false;
                            }

                            String optionLine = lines[index].trim();

                            if (!optionLine.equalsIgnoreCase("Ano / Ne")) {
                                System.err.println("[ERROR] - Expected 'Ano / Ne' line for question " + (questionCount + 1) + ", but got: '" + optionLine + "'");
                                return false;
                            }

                            answersForQuestion.add(new Answer("Ano"));
                            answersForQuestion.add(new Answer("Ne"));

                            index += 1; // Move index past 'Ano / Ne' line

                        } else if (questionType == QuestionTypeEnum.OPEN_ENDED) {
                            Answer correctAnswer = findCorrectAnswerFor(questionCount + 1, testContent);

                            if (correctAnswer != null) {
                                // Correct answer is the same as explanation in open-ended questions
                                correctAnswer.setExplanation(correctAnswer.getText());
                                correctAnswer.setCorrect(true);
                            } else {
                                System.err.println("[ERROR] - No explanation found for question " + (questionCount + 1));
                            }

                            answersForQuestion.add(correctAnswer);
                        } else {
                            System.err.println("[ERROR] - Unknown question type for question " + (questionCount + 1));
                            return false;
                        }

                        question.setAnswers(answersForQuestion);
                        questions.add(question);
                        questionCount++;

                    } else {
                        index++;
                    }
                } else if (inAnswers) {
                    if (questionPattern.matcher(line).matches()) {
                        // Get question number
                        String answerLine = lines[index].trim();
                        String questionNumberStr = answerLine.substring(0, answerLine.indexOf('.')).trim();
                        int questionNumber = Integer.parseInt(questionNumberStr);

                        // Find the corresponding Question object
                        Question question = questions.get(questionNumber - 1);
                        List<Answer> answersForQuestion = question.getAnswers();

                        // Get the correct answer text
                        String correctAnswerText = answerLine.substring(answerLine.indexOf('.') + 1).trim();

                        index++; // Move to the line after the answer line

                        StringBuilder explanationBuilder = new StringBuilder();

                        // Loop to collect the full explanation, which may span multiple lines
                        while (index < lines.length && !questionPattern.matcher(lines[index]).matches() && !lines[index].startsWith("Maximální počet bodů:")) {
                            String explanationLine = lines[index].trim();
                            if (explanationLine.startsWith("Vysvětlení:")) {
                                explanationLine = explanationLine.substring("Vysvětlení:".length()).trim();
                            }
                            explanationBuilder.append(explanationLine).append("\n");
                            index++;
                        }

                        String explanation = explanationBuilder.toString().trim();

                        if (questionType == QuestionTypeEnum.MULTIPLE_CHOICE) {
                            // For multiple-choice questions, find the correct option and explanation and set it
                            char correctOptionLetter = correctAnswerText.charAt(0); // 'a', 'b', or 'c'

                            // Set the correct answer and explanation
                            switch (correctOptionLetter) {
                                case 'a':
                                    answersForQuestion.get(0).setCorrect(true);
                                    answersForQuestion.get(0).setExplanation(explanation);
                                    break;
                                case 'b':
                                    answersForQuestion.get(1).setCorrect(true);
                                    answersForQuestion.get(1).setExplanation(explanation);
                                    break;
                                case 'c':
                                    answersForQuestion.get(2).setCorrect(true);
                                    answersForQuestion.get(2).setExplanation(explanation);
                                    break;
                                default:
                                    System.err.println("[ERROR] - Invalid correct option '" + correctOptionLetter + "' for question " + questionNumber);
                                    return false;
                            }
                        } else if (questionType == QuestionTypeEnum.YES_NO) {
                            // For Yes / No questions set the correct option and explanation
                            boolean found = false;

                            // Set isCorrect and explanation if the correct answer is found
                            for (Answer answer : answersForQuestion) {
                                if (answer.getText().equalsIgnoreCase(correctAnswerText)) {
                                    answer.setCorrect(true);
                                    answer.setExplanation(explanation);
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                System.err.println("[ERROR] - Correct answer '" + correctAnswerText + "' not found for question " + questionNumber);
                                return false;
                            }
                        } else if (questionType == QuestionTypeEnum.OPEN_ENDED) {
                            // For Open-ended questions, store the correct answer and explanation in the question
                            question.setCorrectAnswer(correctAnswerText.replace(questionNumber + ". ", ""));
                        } else {
                            System.err.println("[ERROR] - Unknown question type in inAnswers section");
                            return false;
                        }

                        answerCount++;

                    } else {
                        index++;
                    }
                }
            }

            // Insert all questions
            int testId = TestManager.getId(promptId);

            if (testId > 0) {
                for (Question question : questions) {
                    if (!QuestionManager.save(question, testId)) {
                        System.err.println("[ERROR] - Failed to save question: " + question);
                        return false;
                    }

                    // Insert answers for the question
                    List<Answer> answersForQuestion = question.getAnswers();

                    if (answersForQuestion != null && !answersForQuestion.isEmpty()) {
                        for (Answer answer : answersForQuestion) {
                            if (!AnswerManager.save(answer)) {
                                System.err.println("[ERROR] - Failed to save answer: " + answer);
                                return false;
                            }

                            // Relate answer with the question
                            if (!answer.relate(question)) {
                                System.err.println("[ERROR] - Failed to relate answer with question: " + answer);
                                return false;
                            }
                        }
                    } else {
                        System.err.println("[ERROR] - No answers found for question: " + question);
                        return false;
                    }

                    // Relate question with the test
                    if (!question.relate(testId)) {
                        System.err.println("[ERROR] - Failed to relate question with test: " + question);
                        return false;
                    }
                }
            } else {
                System.out.println("[ERROR] - Test ID not found for prompt ID: " + promptId);
                return false;
            }

            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] - An exception occurred while parsing test content: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean lineContainsAnyOf(String[] array, String line) {
        if (array == null) {
            return false;
        }

        for (String string : array) {
            if (line.contains(string)) {
                return true;
            }
        }

        return false;
    }

    private static Answer findCorrectAnswerFor(int questionNumber, String testContent) {
        String[] lines = testContent.split("\n");
        int index = 0;

        // Skip lines above the first answer
        while (index < lines.length && !lines[index].contains("Správné odpovědi:")) {
            index++;
        }

        if (index >= lines.length) {
            System.err.println("[ERROR] - 'Správné odpovědi:' section not found in the test content.");
            return null;
        }

        // Then skip lines above the correct answer
        String questionNumberStr = questionNumber + ".";
        while (index < lines.length && !lines[index].startsWith(questionNumberStr)) {
            index++;
        }

        if (index >= lines.length) {
            System.err.println("[ERROR] - Correct answer for question " + questionNumber + " not found.");
            return null;
        }

        // Extract correct answer (e.g., 'c) New York City')
        String answerText = lines[index].trim().replace(questionNumber + ". ", "").trim();

        // Remove 'a) ', 'b) ', 'c) ' if present
        if (answerText.startsWith("a) ") || answerText.startsWith("b) ") || answerText.startsWith("c) ")) {
            answerText = answerText.substring(3).trim();
        }

        return new Answer(answerText);
    }

    private static boolean containsText(String text) {
        return !text.isEmpty() && !text.isBlank();
    }
}