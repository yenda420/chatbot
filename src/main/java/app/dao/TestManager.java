package app.dao;

import app.models.Test;
import app.services.AIService;
import app.services.DatabaseService;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;

public class TestManager {
    private static DatabaseService db;

    public TestManager() {
        db = new DatabaseService();
    }

    public static boolean insert(Test test, int promptId) {
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

                    System.out.println("[INFO] - Test " + test + "  inserted into database.");
                    return true;
                } catch (SQLException e) {
                    System.err.println("[ERROR] - Failed to insert test " + test + " into database.");
                    e.printStackTrace();
                }
            } else {
                System.err.println("[ERROR] - promptId can't be 0.");
            }
        }
        return false;
    }
}