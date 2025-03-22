package app.dao;

import app.enums.DifficultyEnum;
import app.enums.QuestionTypeEnum;
import app.models.*;
import app.services.DatabaseService;
import app.services.LogService;

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

        LogService.logError("Test with promptId " + promptId + " not found in database.");
        return -1;
    }

    public static ArrayList<Test> getTests(User user) throws SQLException {
        ArrayList<String> subjects = SubjectManager.getSubjects(user);
        ArrayList<Test> tests = new ArrayList<>();
        ArrayList<Integer> testIds = new ArrayList<>();

        for (String subject : subjects) {
            int subjectId = SubjectManager.getId(subject);
            String sql = "SELECT t.name, t.testId " +
                        "FROM tests t " +
                        "JOIN prompts p USING (promptId) " +
                        "JOIN topics_prompts tp USING (promptId) " +
                        "JOIN topics top USING (topicId) " +
                        "WHERE top.subjectId = ? " +
                        "ORDER BY t.testId ASC";

            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                pstmt.setInt(1, subjectId);

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int testId = rs.getInt("testId");
                    Test test = new Test(rs.getString("name"), testId);

                    if (!testIds.contains(testId)) {
                        tests.add(test);
                        testIds.add(testId);
                    }
                }
            }
        }

        if (tests.isEmpty()) {
            LogService.logWarning("There are no tests for user " + user.getEmail() + "'s subjects.");
            return null;
        }

        return tests;
    }

    public static int calculateMaxPoints(int testId) throws SQLException {
        String sql = "SELECT SUM(q.points) as totalPoints FROM questions q " +
                    "JOIN questions_tests qt USING (questionId) " +
                    "WHERE qt.testId = ?";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setInt(1, testId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("totalPoints");
                }
            }
        }

        return 0;
    }

    public static Test getTestById(int testId) throws SQLException {
        String sql = "SELECT t.name, t.numberOfQuestions, t.timeLimit, t.difficulty, p.*, q.type " +
                "FROM tests t " +
                "JOIN prompts p USING (promptId) " +
                "JOIN questions_tests qt USING (testId) " +
                "JOIN questions q USING (questionId) " +
                "WHERE t.testId = ? LIMIT 1";  // Use type from the first question

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setInt(1, testId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    DifficultyEnum difficulty = DifficultyEnum.fromString(rs.getString("difficulty"));
                    QuestionTypeEnum questionType = QuestionTypeEnum.fromString(rs.getString("type"));
                    Prompt prompt = new Prompt(rs.getString("message"), null, TopicManager.getTopicsByPromptId(rs.getInt("promptId")));

                    return new Test(rs.getString("name"), rs.getInt("numberOfQuestions"), rs.getInt("timeLimit"), difficulty, questionType, prompt, null);
                }
            }
        }
        return null;
    }

    public static boolean save(Test test, User user, int promptId) {
        if (db.getConn() != null) {
            if (promptId > 0) {
                String sql = "INSERT INTO tests (name, difficulty, numberOfQuestions, timeLimit, promptId, userId) VALUES (?, ?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                    pstmt.setString(1, test.getName());
                    pstmt.setString(2, test.getDifficulty().getName());
                    pstmt.setInt(3, test.getNumberOfQuestions());
                    pstmt.setInt(4, test.getTimeLimitInMinutes());
                    pstmt.setInt(5, promptId);
                    pstmt.setInt(6, UserManager.getId(user.getEmail()));
                    pstmt.executeUpdate();

                    LogService.logInfo("Test " + test + " inserted into database.");
                    return true;
                } catch (SQLException e) {
                    LogService.logError("Failed to save test " + test + " into database.");
                    e.printStackTrace();
                }
            } else {
                LogService.logError("promptId can't be 0.");
            }
        }
        return false;
    }

    public static boolean saveTestData(String testContent, Test test, int promptId) {
        try {
            Pattern questionPattern = Pattern.compile("^\\d+\\.\\s+.*");

            String[] lines = testContent.split("\n");

            LogService.logDebug("Number of lines: " + lines.length);

            QuestionTypeEnum questionType = test.getQuestionType();
            DifficultyEnum difficulty = test.getDifficulty();

            int startIndex = 0;
            int endIndex = lines.length;

            // Log initial lines for debugging potential errors when parsing the test
            for (int i = 0; i < lines.length; i++) {
                LogService.logDebug("Line " + i + ": " + lines[i]);
            }

            // Remove any leading unwanted lines
            while (startIndex < lines.length &&
                    (lineContainsAnyOf(requiredSections, lines[startIndex]) || !containsText(lines[startIndex]))) {
                startIndex++;
            }

            // Remove any unwanted lines
            lines = Arrays.copyOfRange(lines, startIndex, endIndex);

            boolean inAnswers = false;
            boolean inQuestions = true;

            int questionCount = 0;
            int answerCount = 0;
            int index = 0;

            List<Question> questions = new ArrayList<>();

            // Skip lines until we reach the first question
            while (index < lines.length && !questionPattern.matcher(lines[index]).matches()) {
                index++;
            }

            while (index < lines.length) {
                String line = lines[index].trim();
                LogService.logDebug("Processing line: " + line);

                if (line.equalsIgnoreCase("Správné odpovědi:")) {
                    inQuestions = false;
                    inAnswers = true;
                    index++;
                    continue;
                }

                if (line.startsWith("Maximální počet bodů:")) {
                    break; // End of test
                }

                if (inQuestions) {
                    if (questionPattern.matcher(line).matches()) {
                        StringBuilder questionText = new StringBuilder();
                        questionText.append(line.substring(line.indexOf('.') + 1).trim()).append("\n");

                        while (!lines[index + 1].startsWith("Body:")) {
                            if (index + 2 >= lines.length) {
                                LogService.logError("Unexpected end of content when reading points for question " + (questionCount + 2) + "(Line: " + lines[index + 1] + ")");
                                return false;
                            }

                            if (!lines[index + 1].contains("```")) {
                                questionText.append(lines[index + 1]).append("\n");
                            }

                            index++;
                        }

                        index++;

                        String pointsLine = lines[index].trim();
                        String[] pointParts = pointsLine.split(":");

                        if (pointParts.length < 2) {
                            LogService.logError("Invalid format for points on line " + (index + 1) + ". Line content: " + pointParts[0]);
                            return false;
                        }

                        String pointsString = pointParts[1].trim().split(" ")[0];
                        int points = Integer.parseInt(pointsString);

                        Question question = new Question(questionText.toString(), questionType, difficulty, points);
                        LogService.logDebug("Question: " + questionText);

                        index += 1; // Move index past 'Body:' line
                        List<Answer> answersForQuestion = new ArrayList<>();

                        if (questionType == QuestionTypeEnum.MULTIPLE_CHOICE) {
                            if (index + 2 >= lines.length) {
                                LogService.logError("Not enough lines for answers for question " + (questionCount + 1));
                                return false;
                            }

                            answersForQuestion.add(new Answer(lines[index].replace("a) ", "").trim()));
                            answersForQuestion.add(new Answer(lines[index + 1].replace("b) ", "").trim()));
                            answersForQuestion.add(new Answer(lines[index + 2].replace("c) ", "").trim()));

                            index += 3; // Move index past the options
                        } else if (questionType == QuestionTypeEnum.YES_NO) {
                            if (index >= lines.length) {
                                LogService.logError("Missing 'Ano / Ne' line for question " + (questionCount + 1));
                                return false;
                            }

                            String optionLine = lines[index].trim();
                            LogService.logDebug("Expected 'Ano / Ne' line, got: " + optionLine);

                            if (!optionLine.equalsIgnoreCase("Ano / Ne")) {
                                LogService.logError("Expected 'Ano / Ne' line for question " + (questionCount + 1) + ", but got: '" + optionLine + "'");
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
                                LogService.logError("No explanation found for question " + (questionCount + 1));
                            }

                            answersForQuestion.add(correctAnswer);
                        } else {
                            LogService.logError("Unknown question type for question " + (questionCount + 1));
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
                            char correctOptionLetter = correctAnswerText.charAt(0); // 'a', 'b', or 'c'

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
                                    LogService.logError("Invalid correct option '" + correctOptionLetter + "' for question " + questionNumber);
                                    return false;
                            }
                        } else if (questionType == QuestionTypeEnum.YES_NO) {
                            boolean found = false;

                            for (Answer answer : answersForQuestion) {
                                if (answer.getText().equalsIgnoreCase(correctAnswerText)) {
                                    answer.setCorrect(true);
                                    answer.setExplanation(explanation);
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                LogService.logError("Correct answer '" + correctAnswerText + "' not found for question " + questionNumber);
                                return false;
                            }
                        } else if (questionType == QuestionTypeEnum.OPEN_ENDED) {
                            question.setCorrectAnswer(correctAnswerText.replace(questionNumber + ". ", ""));
                        } else {
                            LogService.logError("Unknown question type in inAnswers section");
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
                        LogService.logError("Failed to save question: " + question);
                        return false;
                    }

                    List<Answer> answersForQuestion = question.getAnswers();

                    if (answersForQuestion != null && !answersForQuestion.isEmpty()) {
                        for (Answer answer : answersForQuestion) {
                            if (!AnswerManager.save(answer)) {
                                LogService.logError("Failed to save answer: " + answer);
                                return false;
                            }

                            if (!answer.relate(question)) {
                                LogService.logError("Failed to relate answer with question: " + answer);
                                return false;
                            }
                        }
                    } else {
                        LogService.logError("No answers found for question: " + question);
                        return false;
                    }

                    if (!question.relate(testId)) {
                        LogService.logError("Failed to relate question with test: " + question);
                        return false;
                    }
                }
            } else {
                LogService.logError("Test ID not found for prompt ID: " + promptId);
                return false;
            }

            return true;
        } catch (Exception e) {
            LogService.logError("An exception occurred while parsing test content: " + e.getMessage());
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
            LogService.logError("'Správné odpovědi:' section not found in the test content.");
            return null;
        }

        // Then skip lines above the correct answer
        String questionNumberStr = questionNumber + ".";
        while (index < lines.length && !lines[index].startsWith(questionNumberStr)) {
            index++;
        }

        if (index >= lines.length) {
            LogService.logError("Correct answer for question " + questionNumber + " not found.");
            return null;
        }

        // Extract correct answer
        String answerText = lines[index].trim().replace(questionNumber + ". ", "").trim();

        // Remove 'a) ', 'b) ', 'c) ' if present
        if (answerText.startsWith("a) ") || answerText.startsWith("b) ") || answerText.startsWith("c) ")) {
            answerText = answerText.substring(3).trim();
        }

        return new Answer(answerText);
    }

    public static boolean deleteTestData(int testId) throws SQLException {
        String deleteQuestionsTestsSql = "DELETE FROM questions_tests WHERE testId = ?";
        String deletePromptsSql = "DELETE FROM prompts WHERE promptId = (SELECT promptId FROM tests WHERE testId = ?)";
        String deleteTopicsPromptsSql = "DELETE FROM topics_prompts WHERE promptId = (SELECT p.promptId FROM prompts p JOIN tests t USING (promptId) WHERE t.testId = ?)";
        String deleteTestsSql = "DELETE FROM tests WHERE testId = ?";

        try {
            // Start transaction
            db.getConn().setAutoCommit(false);

            // Delete records in questions_tests
            try (PreparedStatement pstmt = db.getConn().prepareStatement(deleteQuestionsTestsSql)) {
                pstmt.setInt(1, testId);
                pstmt.executeUpdate();
            }

            // Delete records in topics_prompts
            try (PreparedStatement pstmt = db.getConn().prepareStatement(deleteTopicsPromptsSql)) {
                pstmt.setInt(1, testId);
                pstmt.executeUpdate();
            }

            // Delete prompt
            try (PreparedStatement pstmt = db.getConn().prepareStatement(deletePromptsSql)) {
                pstmt.setInt(1, testId);
                pstmt.executeUpdate();
            }

            // Delete test
            try (PreparedStatement pstmt = db.getConn().prepareStatement(deleteTestsSql)) {
                pstmt.setInt(1, testId);
                pstmt.executeUpdate();
            }

            // Commit transaction
            db.getConn().commit();
            return true;
        } catch (SQLException e) {
            // Rollback transaction
            db.getConn().rollback();
            LogService.logError("Failed to delete test data for testId: " + testId);
            e.printStackTrace();
        } finally {
            // Ensure connection is returned to default state
            db.getConn().setAutoCommit(true);
        }
        return false;
    }

    private static boolean containsText(String text) {
        return !text.isEmpty() && !text.isBlank();
    }
}