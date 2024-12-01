package app.dao;

import app.enums.TopicEnum;
import app.models.Topic;
import app.models.Subject;
import app.services.DatabaseService;

import java.sql.*;
import java.util.ArrayList;

public class TopicManager {
    private static DatabaseService db;
    private static ArrayList<Topic> defaultTopics = new ArrayList<>();

    public TopicManager() {
        db = new DatabaseService();

        for (TopicEnum topicEnum : TopicEnum.values()) {
            defaultTopics.add(topicEnum.getTopic());
        }
    }

    public static DatabaseService getDb() {
        return db;
    }

    public static ArrayList<Topic> getDefaultTopics() {
        return defaultTopics;
    }

    public static boolean insert(Topic topic) throws SQLException {
        if (db.getConn() != null) {
            int subjectId = getSubjectId(topic.getSubject());

            if (subjectId == -1) {
                System.err.println("[ERROR] - Subject " + topic.getSubject() + " not found in database.");
                return false;
            }

            // Check if the topic already exists for the subject
            if (!topicExists(topic.getName(), subjectId)) {
                String sql;
                if (topic.getDescription() != null) {
                    sql = "INSERT INTO topics (subjectId, name, description) VALUES (?, ?, ?)";
                } else {
                    sql = "INSERT INTO topics (subjectId, name) VALUES (?, ?)";
                }

                try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                    pstmt.setInt(1, subjectId);
                    pstmt.setString(2, topic.getName());
                    if (topic.getDescription() != null) {
                        pstmt.setString(3, topic.getDescription());
                    }
                    pstmt.executeUpdate();
                    System.out.println("[INFO] - Topic " + topic.getName() + " inserted into database.");
                    return true;
                } catch (SQLException e) {
                    System.err.println("[ERROR] - Failed to insert topic " + topic.getName() + " into database.");
                    e.printStackTrace();
                }
            } else {
                System.out.println("[INFO] - Topic " + topic.getName() + " already exists in database.");
            }
        }
        return false;
    }

    private static int getSubjectId(Subject subject) throws SQLException {
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

    private static boolean topicExists(String topicName, int subjectId) throws SQLException {
        String sql = "SELECT * FROM topics WHERE name = ? AND subjectId = ?";
        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setString(1, topicName);
            pstmt.setInt(2, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static boolean insertDefaultTopics() {
        for (Topic topic : defaultTopics) {
            try {
                insert(topic);
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to insert default topic.");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static ArrayList<String> getTopics() {
        ArrayList<String> topics = new ArrayList<>();
        String sql = "SELECT name FROM topics";

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

    public static Topic getTopic(String topicName) {
        Topic topic = null;
        String sql = "SELECT * FROM topics WHERE name = ?";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setString(1, topicName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int subjectId = rs.getInt("subjectId");
                    Subject subject = SubjectManager.getSubject(subjectId);
                    topic = new Topic(topicName, subject);

                    if (rs.getString("description") != null) {
                        topic.setDescription(rs.getString("description"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return topic;
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