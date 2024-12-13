package app.dao;

import app.enums.SubjectEnum;
import app.models.Subject;
import app.services.DatabaseService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SubjectManager {
    private static DatabaseService db;
    private static final ArrayList<Subject> defaultSubjects = new ArrayList<>();

    public SubjectManager() {
        db = new DatabaseService();

        for (SubjectEnum subjectEnum : SubjectEnum.values()) {
            defaultSubjects.add(subjectEnum.getSubject());
        }
    }

    public static boolean save(Subject subject) throws SQLException {
        if (db.getConn() != null) {
            if (!DatabaseService.instanceInDatabase("subjects", "shortage", subject.getShortage())) {
                boolean descriptionExists = subject.getDescription() != null;

                String sql = descriptionExists ?
                        "INSERT INTO subjects (name, shortage, description) VALUES (?, ?, ?)" :
                        "INSERT INTO subjects (name, shortage) VALUES (?, ?)";

                try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                    pstmt.setString(1, subject.getName());
                    pstmt.setString(2, subject.getShortage());
                    if (descriptionExists) pstmt.setString(3, subject.getDescription());

                    pstmt.executeUpdate();

                    System.out.println("[INFO] - Subject " + subject.getName() + " inserted into database.");
                    return true;
                } catch (SQLException e) {
                    System.err.println("[ERROR] - Failed to save subject " + subject.getName() + "  into database.");
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public static void insertDefaultSubjects() {
        for (Subject subject : defaultSubjects) {
            try {
                save(subject);
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public static int getSubjectId(Subject subject) throws SQLException {
        String sql = "SELECT subjectId FROM subjects WHERE name = ? AND shortage = ?";
        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setString(1, subject.getName());
            pstmt.setString(2, subject.getShortage());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("subjectId");
                } else {
                    return -1;
                }
            }
        }
    }

    public static Subject getSubject(int subjectId) throws SQLException {
        String sql = "SELECT * FROM subjects WHERE subjectId = ?";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Subject(
                            rs.getString("name"),
                            rs.getString("shortage"),
                            rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

    public static Subject getTopicSubject(String topicName) throws SQLException {
        String sql = "SELECT subjectId FROM topics WHERE name = ?";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setString(1, topicName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    sql = "SELECT * FROM subjects WHERE subjectId = ?";

                    try (PreparedStatement pstmt2 = db.getConn().prepareStatement(sql)) {
                        pstmt2.setInt(1, rs.getInt("subjectId"));
                        try (ResultSet rs2 = pstmt2.executeQuery()) {
                            if (rs2.next()) {
                                return new Subject(
                                        rs2.getString("name"),
                                        rs2.getString("shortage"),
                                        rs2.getString("description")
                                );
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
