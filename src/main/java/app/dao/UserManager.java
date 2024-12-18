package app.dao;

import app.models.User;
import app.services.DatabaseService;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;

public class UserManager {
    private static DatabaseService db;

    public UserManager() {
        db = new DatabaseService();
    }

    public static boolean save(User user) throws SQLException {
        if (db.getConn() != null) {
            if (!DatabaseService.instanceInDatabase("user", "email", user.getEmail())) {
                String sql = "INSERT INTO users(firstName, lastName, email, passwordHash, role) " +
                            "VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                    pstmt.setString(1, user.getFirstName());
                    pstmt.setString(2, user.getLastName());
                    pstmt.setString(3, user.getEmail());
                    pstmt.setString(4, user.getPasswordHash());
                    pstmt.setString(5, user.getRole().getName());

                    pstmt.executeUpdate();

                    System.out.println("[INFO] - User " + user.getFirstName() + " " + user.getLastName() + " inserted into database.");
                    return true;
                } catch (SQLException e) {
                    System.err.println("[ERROR] - Failed to save user " + user.getFirstName() + " " + user.getLastName() + " into database.");
                    e.printStackTrace();
                    return false;
                }
            } else {
                System.out.println("[INFO] - User " + user.getFirstName() + " " + user.getLastName() + " already exists in database.");
                return true;
            }
        }
        return false;
    }

    public static int getId(String email) throws SQLException {
        if (db.getConn() != null) {
            String sql = "SELECT userId FROM users WHERE email = ?";

            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                pstmt.setString(1, email);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("userId");
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to get the user ID.");
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static boolean save(ObservableList<String> subjects, String userEmail) {
        if (db.getConn() != null) {
            String sql = "INSERT INTO users_subjects(userId, subjectId) VALUES (?, ?)";

            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                for (String subject : subjects) {
                    int subjectId = SubjectManager.getId(subject);
                    int userId = UserManager.getId(userEmail);

                    pstmt.setInt(1, subjectId);
                    pstmt.setInt(2, userId);

                    pstmt.executeUpdate();
                }
                System.out.println("[INFO] - Subjects inserted into database.");
                return true;
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to save subjects into database.");
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}