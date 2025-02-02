package app.dao;

import app.enums.UserRoleEnum;
import app.models.User;
import app.services.DatabaseService;

import com.google.common.hash.Hashing;
import io.github.cdimascio.dotenv.Dotenv;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.charset.StandardCharsets;
import java.sql.*;

import static app.services.DatabaseService.instanceInDatabase;

public class UserManager {
    private static DatabaseService db;

    public UserManager() {
        db = new DatabaseService();
    }

    public static boolean save(User user) throws SQLException {
        if (db.getConn() != null) {
            if (!instanceInDatabase("users", "email", user.getEmail())) {
                String sql = "INSERT INTO users(firstName, lastName, email, passwordHash, role) " +
                            "VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                    pstmt.setString(1, user.getFirstName());
                    pstmt.setString(2, user.getLastName());
                    pstmt.setString(3, user.getEmail());
                    pstmt.setString(4, user.getPasswordHash());
                    pstmt.setString(5, user.getRole().getName());

                    pstmt.executeUpdate();

                    System.out.println("[INFO] - User " + user.getEmail() + " inserted into database.");
                    return true;
                } catch (SQLException e) {
                    System.err.println("[ERROR] - Failed to save user " + user.getEmail() + " into database.");
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

    public static boolean unlinkUserFromSubjects(ObservableList<String> subjects, User user) {
        if (db.getConn() != null) {
            String sql = "DELETE FROM users_subjects WHERE userId = ? AND subjectId = ?";

            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                int userId = UserManager.getId(user.getEmail());

                for (String subject : subjects) {
                    if (DatabaseService.instanceInDatabase("users_subjects", userId, SubjectManager.getId(subject), "userId", "subjectId")) {
                        int subjectId = SubjectManager.getId(subject);

                        pstmt.setInt(1, userId);
                        pstmt.setInt(2, subjectId);

                        pstmt.executeUpdate();

                        System.out.println("[INFO] - User with ID " + userId + " unlinked from subject with ID " + subjectId + ".");
                    }
                }

                return true;
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to unlink user from subjects.");
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static boolean linkUserToSubjects(ObservableList<String> subjects, User user) {
        if (db.getConn() != null) {
            String sql = "INSERT INTO users_subjects(userId, subjectId) VALUES (?, ?)";

            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                for (String subject : subjects) {
                    int subjectId = SubjectManager.getId(subject);
                    int userId = UserManager.getId(user.getEmail());

                    if (!DatabaseService.instanceInDatabase("users_subjects", userId, subjectId, "userId", "subjectId")) {
                        pstmt.setInt(1, userId);
                        pstmt.setInt(2, subjectId);

                        pstmt.executeUpdate();
                    }

                    System.out.println("[INFO] - User with ID " + userId + " linked to subject with ID " + subjectId + ".");
                }

                return true;
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to save subjects into database.");
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static String getPasswordHash(String email) {
        if (db.getConn() != null) {
            String sql = "SELECT passwordHash FROM users WHERE email = ?";

            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                pstmt.setString(1, email);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return rs.getString("passwordHash");
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to get password hash.");
                e.printStackTrace();
            }
        }

        return null;
    }

    public static User getUser(String email) {
        if (db.getConn() != null) {
            String sql = "SELECT * FROM users WHERE email = ?";

            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                pstmt.setString(1, email);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return new User(
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rs.getString("email"),
                            rs.getString("passwordHash"),
                            UserRoleEnum.fromString(rs.getString("role"))
                    );
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to get the user.");
                e.printStackTrace();
            }
        }

        return null;
    }

    public static boolean update(User user, String oldEmail) {
        if (db.getConn() != null) {
            String sql = "UPDATE users SET " +
                        "firstName = ?, lastName = ?, " +
                        "email = ?, passwordHash = ?, " +
                        "role = ? WHERE email = ?";

            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                pstmt.setString(1, user.getFirstName());
                pstmt.setString(2, user.getLastName());
                pstmt.setString(3, user.getEmail());
                pstmt.setString(4, user.getPasswordHash());
                pstmt.setString(5, user.getRole().getName());
                pstmt.setString(6, oldEmail);

                pstmt.executeUpdate();

                System.out.println("[INFO] - User " + user.getEmail() + ".");
                return true;
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to update user " + user.getEmail() + ".");
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static void saveDefaultUsers() {
        Dotenv dotenv = Dotenv.load();

        String adminEmail = dotenv.get("ADMIN_EMAIL");
        String adminPassword = dotenv.get("ADMIN_PASSWORD");

        String userPasswordHash = Hashing.sha256()
                .hashString("user", StandardCharsets.UTF_8)
                .toString();

        String adminPasswordHash = Hashing.sha256()
                .hashString(adminPassword, StandardCharsets.UTF_8)
                .toString();

        // Test user
        User user = new User("user", userPasswordHash, UserRoleEnum.TEACHER);
        // Default admin
        User admin = new User(adminEmail, adminPasswordHash, UserRoleEnum.ADMIN);

        try {
            if (!instanceInDatabase("users", "email", "user")) {
                UserManager.save(user);

                // Test subjects
                ObservableList<String> subjects = FXCollections.observableArrayList("Programování", "Anglický jazyk");
                user.relate(subjects);
            }

            if (!instanceInDatabase("users", "email", adminEmail)) {
                UserManager.save(admin);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to save default users.");
            e.printStackTrace();
        }
    }
}