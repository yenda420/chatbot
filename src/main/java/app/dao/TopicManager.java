package app.dao;

import app.enums.TopicEnum;
import app.models.Topic;

import app.models.User;
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

    public static boolean delete(Topic topic) {
        if (db.getConn() != null) {
            String sql = "DELETE FROM topics WHERE topicId = ?";

            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                pstmt.setInt(1, getId(topic));

                int rowsDeleted = pstmt.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("[INFO] - Topic " + topic.getName() + " deleted from database.");
                    return true;
                } else {
                    System.out.println("[INFO] - Topic " + topic.getName() + " not found in database.");
                    return false;
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to delete topic " + topic.getName() + " from database.");
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    public static boolean update(Topic oldTopic, Topic newTopic) {
        String sql = "UPDATE topics " +
                    "SET subjectId = ?, name = ?, description = ? " +
                    "WHERE topicId = ?";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setInt(1, SubjectManager.getId(newTopic.getSubject()));
            pstmt.setString(2, newTopic.getName());
            pstmt.setString(3, newTopic.getDescription());
            pstmt.setInt(4, getId(oldTopic));

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("[INFO] - Topic " + newTopic.getName() + " updated in database.");
                return true;
            } else {
                System.out.println("[INFO] - Topic " + newTopic.getName() + " not found in database.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to update topic " + newTopic.getName() + " in database.");
            e.printStackTrace();
            return false;
        }
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

    public static ArrayList<String> getTopics(User user) throws SQLException {
        ArrayList<String> subjects = SubjectManager.getSubjects(user);
        ArrayList<String> topics = new ArrayList<>();

        for (String subject : subjects) {
            topics.addAll(getTopics(subject));
        }

        return topics;
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

    public static int getId(Topic topic) {
        String sql = "SELECT topicId FROM topics WHERE name = ?";
        int topicId = -1;

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setString(1, topic.getName());

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