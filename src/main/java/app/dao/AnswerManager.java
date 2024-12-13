package app.dao;

import app.models.Answer;
import app.models.Question;
import app.services.DatabaseService;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AnswerManager {
    private static DatabaseService db;

    public AnswerManager() {
        db = new DatabaseService();
    }

    public static boolean save(Answer answer) throws SQLException {
        if (db.getConn() != null) {
            if (!DatabaseService.instanceInDatabase("answers", "text", answer.getText())) {
                String sql = "INSERT INTO answers (text) VALUES (?)";
                try (PreparedStatement pstmt = db.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, answer.getText());

                    int affectedRows = pstmt.executeUpdate();

                    if (affectedRows == 0) {
                        throw new SQLException("Creating answer failed, no rows affected.");
                    }

                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int answerId = generatedKeys.getInt(1);
                            answer.setId(answerId);
                        } else {
                            throw new SQLException("Creating answer failed, no ID obtained.");
                        }
                    }

                    System.out.println("[INFO] - Answer " + answer + " inserted into database.");
                    return true;
                } catch (SQLException e) {
                    System.err.println("[ERROR] - Failed to save answer " + answer + " into database.");
                    e.printStackTrace();
                    return false;
                }
            } else {
                // If the answer already exists, retrieve its ID
                int answerId = getId(answer);
                if (answerId > 0) {
                    answer.setId(answerId);
                    System.out.println("[INFO] - Answer " + answer + " already exists in database with ID " + answerId + ".");
                    return true;
                } else {
                    System.err.println("[ERROR] - Failed to retrieve existing answer ID for " + answer);
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean linkAnswerToQuestion(Answer answer, Question question) throws SQLException {
        if (db.getConn() != null) {
            boolean linkExists = DatabaseService.instanceInDatabase("questions_answers", question.getId(), answer.getId(), "questionId", "answerId");

            if (!linkExists) {
                boolean isExplanation = answer.getExplanation() != null;

                String sql = isExplanation ?
                        "INSERT INTO questions_answers (questionId, answerId, isCorrect, explanation) VALUES (?, ?, ?, ?)" :
                        "INSERT INTO questions_answers (questionId, answerId, isCorrect) VALUES (?, ?, ?)";

                try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                    pstmt.setInt(1, question.getId());
                    pstmt.setInt(2, answer.getId());
                    pstmt.setBoolean(3, answer.isCorrect());
                    if (isExplanation) pstmt.setString(4, answer.getExplanation());

                    pstmt.executeUpdate();

                    System.out.println("[INFO] - Answer " + answer + " linked to question " + question + ".");
                    return true;
                } catch (SQLException e) {
                    System.err.println("[ERROR] - Failed to link answer " + answer + " to question " + question + ".");
                    e.printStackTrace();
                    return false;
                }
            } else {
                System.out.println("[INFO] - Answer " + answer + " is already linked to question " + question + ".");
                return true;
            }
        }
        return false;
    }

    public static int getId(Answer answer) throws SQLException {
        String sql = "SELECT answerId FROM answers WHERE text = ?";
        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setString(1, answer.getText());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("answerId");
                }
            }
        }
        return 0;
    }
}
