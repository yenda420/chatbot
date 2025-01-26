package app.dao;

import app.enums.TopicEnum;
import app.models.Topic;
import app.services.DatabaseService;

import java.sql.*;
import java.util.ArrayList;

public class TopicManager {
    private static DatabaseService db;
    private static final ArrayList<Topic> defaultTopics = new ArrayList<>();

    public TopicManager() {
        db = new DatabaseService();

        for (TopicEnum topicEnum : TopicEnum.values()) {
            defaultTopics.add(topicEnum.getTopic());
        }
    }

    public static boolean save(Topic topic) throws SQLException {
        if (db.getConn() != null) {
            int subjectId = SubjectManager.getId(topic.getSubject());

            if (subjectId == -1) {
                System.err.println("[ERROR] - Subject " + topic.getSubject() + " not found in database.");
                return false;
            }

            if (!DatabaseService.instanceInDatabase("topics", topic.getName(), subjectId, "name", "subjectId")) {
                boolean descriptionExists = topic.getDescription() != null;

                String sql = descriptionExists ?
                        "INSERT INTO topics (subjectId, name, description) VALUES (?, ?, ?)" :
                        "INSERT INTO topics (subjectId, name) VALUES (?, ?)";

                try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                    pstmt.setInt(1, subjectId);
                    pstmt.setString(2, topic.getName());
                    if (descriptionExists) pstmt.setString(3, topic.getDescription());

                    pstmt.executeUpdate();

                    System.out.println("[INFO] - Topic " + topic.getName() + " inserted into database.");
                    return true;
                } catch (SQLException e) {
                    System.err.println("[ERROR] - Failed to save topic " + topic.getName() + " into database.");
                    e.printStackTrace();
                    return false;
                }
            } else {
                System.out.println("[INFO] - Topic " + topic.getName() + " already exists in database.");
                return true;
            }
        }
        return false;
    }

    public static void saveDefaultTopics() {
        for (Topic topic : defaultTopics) {
            try {
                save(topic);
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to save default topic.");
                e.printStackTrace();
                return;
            }
        }
    }

    public static ArrayList<String> getTopics(String fromSubjects) throws SQLException {
        int id = SubjectManager.getId(fromSubjects);
        ArrayList<String> topics = new ArrayList<>();

        String sql = "SELECT name FROM topics WHERE subjectId = " + id;

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery(sql)) {

            while (rs.next()) {
                topics.add(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return topics;
    }

    public static int getId(String topicName) {
        String sql = "SELECT topicId FROM topics WHERE name = ?";
        int topicId = -1;

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setString(1, topicName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    topicId = rs.getInt("topicId");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to get topic ID.");
            e.printStackTrace();
        }
        return topicId;
    }
}