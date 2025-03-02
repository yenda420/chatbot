package app.dao;

import app.enums.DifficultyEnum;
import app.enums.QuestionTypeEnum;
import app.models.Question;

import app.services.DatabaseService;
import app.services.LogService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class QuestionManager {
    private static DatabaseService db;

    public QuestionManager() {
        db = new DatabaseService();
    }

    public static ArrayList<String> getQuestionDifficulties() {
        ArrayList<String> difficulties = new ArrayList<>();
        String sql = "SELECT COLUMN_TYPE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = ? " +
                "AND COLUMN_NAME = ?";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setString(1, "questions");
            pstmt.setString(2, "difficulty");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String columnType = rs.getString("COLUMN_TYPE");
                    // Extract the enum values from the COLUMN_TYPE string
                    String enumValues = columnType.substring(columnType.indexOf("(") + 1, columnType.lastIndexOf(")"));
                    // Split the values by comma
                    String[] values = enumValues.split(",");

                    for (String value : values) {
                        // Remove single quotes and trim whitespace
                        value = value.trim().replace("'", "");
                        difficulties.add(value);
                    }
                }
            }
        } catch (Exception e) {
            LogService.logError("Failed to get question difficulties");
            e.printStackTrace();
        }

        return difficulties;
    }

    public static ArrayList<String> getQuestionTypes() {
        ArrayList<String> types = new ArrayList<>();
        String sql = "SELECT COLUMN_TYPE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = ? " +
                "AND COLUMN_NAME = ?";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setString(1, "questions");
            pstmt.setString(2, "type");

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String columnType = rs.getString("COLUMN_TYPE");
                // Extract the enum values from the COLUMN_TYPE string
                String enumValues = columnType.substring(columnType.indexOf("(") + 1, columnType.lastIndexOf(")"));
                // Split the values by comma
                String[] values = enumValues.split(",");

                for (String value : values) {
                    // Remove single quotes and trim whitespace
                    value = value.trim().replace("'", "");
                    types.add(value);
                }
            }
        } catch (Exception e) {
            LogService.logError("Failed to get question types");
            e.printStackTrace();
        }

        return types;
    }

    // Inserts the question to the database and links it to its test
    public static boolean save(Question question, int testId) throws SQLException {
        if (db.getConn() != null) {
            if (!questionInDatabase(question)) {
                String sql = "INSERT INTO questions (text, type, difficulty, points) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                    pstmt.setString(1, question.getText());
                    pstmt.setString(2, question.getType().getName());
                    pstmt.setString(3, question.getDifficulty().getName());
                    pstmt.setInt(4, question.getPoints());

                    pstmt.executeUpdate();

                    LogService.logInfo("Question " + question + " inserted into database.");
                } catch (SQLException e) {
                    LogService.logError("Failed to save question " + question + " into database.");
                    e.printStackTrace();
                    return false;
                }
            }

            int questionId = question.getId();

            String sql = "INSERT INTO questions_tests (testId, questionId) VALUES (?, ?)";
            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                if (!DatabaseService.instanceInDatabase("questions_tests", testId, questionId, "testId", "questionId")) {
                    pstmt.setInt(1, testId);
                    pstmt.setInt(2, questionId);
                }

                pstmt.executeUpdate();

                LogService.logInfo("Question " + question + " linked to tests in 'questions_tests' table.");
                return true;
            } catch (SQLException e) {
                LogService.logError("Falied to link question " + question + " to tests in 'questions_tests' table.");
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static int getId(Question question) {
        if (db.getConn() != null) {
            String sql = "SELECT questionId FROM questions WHERE text = ? AND type = ? AND difficulty = ? AND points = ?";
            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                pstmt.setString(1, question.getText());
                pstmt.setString(2, question.getType().getName());
                pstmt.setString(3, question.getDifficulty().getName());
                pstmt.setInt(4, question.getPoints());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("questionId");
                    }
                }
            } catch (SQLException e) {
                LogService.logError("Failed to get question ID.");
            }
        }
        return 0;
    }

    public static boolean linkQuestionToTest(Question question, int testId) throws SQLException {
        if (db.getConn() != null) {
            boolean linkExists = DatabaseService.instanceInDatabase("questions_tests", testId, question.getId(), "testId", "questionId");

            if (!linkExists) {
                String sql = "INSERT INTO questions_tests (questionId, testId) VALUES (?, ?)";

                try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                    if (!DatabaseService.instanceInDatabase("questions_tests", testId, question.getId(), "testId", "questionId")) {
                        pstmt.setInt(1, question.getId());
                        pstmt.setInt(2, testId);

                        pstmt.executeUpdate();
                    }

                    LogService.logInfo("Question " + question + " linked to test with id " + testId + ".");
                    return true;
                } catch (SQLException e) {
                    LogService.logError("Failed to link question " + question + " to test with id " + testId + ".");
                    e.printStackTrace();
                    return false;
                }
            } else {
                LogService.logInfo("Question " + question + " already linked to test with id " + testId + ".");
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Question> getQuestionsForTest(int testId) throws SQLException {
        ArrayList<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions " +
                      "WHERE questionId IN (SELECT questionId FROM questions_tests WHERE testId = ?)";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setInt(1, testId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String text = rs.getString("text");
                    String type = rs.getString("type");
                    String difficulty = rs.getString("difficulty");
                    int points = rs.getInt("points");

                    Question question = new Question(text, QuestionTypeEnum.fromString(type), DifficultyEnum.fromString(difficulty), points);
                    questions.add(question);
                }

            }

            return questions;
        }
    }

    private static boolean questionInDatabase(Question question) throws SQLException {
        if (db.getConn() != null) {
            if (!DatabaseService.instanceInDatabase("questions", "text", question.getText())) {
                return false;
            }

            String sql = "SELECT * FROM questions WHERE text = ? AND type = ? AND difficulty = ? AND points = ?";

            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                pstmt.setString(1, question.getText());
                pstmt.setString(2, question.getType().getName());
                pstmt.setString(3, question.getDifficulty().getName());
                pstmt.setInt(4, question.getPoints());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
