package app.dao;

import app.enums.SubjectEnum;
import app.models.Subject;
import app.services.DatabaseService;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class SubjectManager {
    private static DatabaseService db;
    private static ArrayList<Subject> defaultSubjects = new ArrayList<>();

    public SubjectManager() {
        db = new DatabaseService();

        for (SubjectEnum subjectEnum : SubjectEnum.values()) {
            defaultSubjects.add(subjectEnum.getSubject());
        }
    }

    public static DatabaseService getDb() {
        return db;
    }

    public static ArrayList<Subject> getDefaultSubjects() {
        return defaultSubjects;
    }

    public static boolean insert(Subject subject) throws SQLException {
        if (db.getConn() != null) {
            if (!DatabaseService.instanceInDatabase("subjects", "shortage", subject.getShortage())) {
                if (subject.getDescription() != null) {
                    String sql = "INSERT INTO subjects (name, shortage, description) VALUES (?, ?, ?)";

                    try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                        pstmt.setString(1, subject.getName());
                        pstmt.setString(2, subject.getShortage());
                        pstmt.setString(3, subject.getDescription());
                        pstmt.executeUpdate();
                        System.out.println("[INFO] - Subject inserted into database.");
                        return true;
                    } catch (SQLException e) {
                        System.err.println("[ERROR] - Failed to insert subject into database.");
                        e.printStackTrace();
                    }
                } else {
                    String sql = "INSERT INTO subjects (name, shortage) VALUES (?, ?)";

                    try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                        pstmt.setString(1, subject.getName());
                        pstmt.setString(2, subject.getShortage());
                        pstmt.executeUpdate();
                        System.out.println("[INFO] - Subject inserted into database.");
                        return true;
                    } catch (SQLException e) {
                        System.err.println("[ERROR] - Failed to insert subject into database.");
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    public static boolean insertDefaultSubjects() {
        for (Subject subject : defaultSubjects) {
            try {
                insert(subject);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
