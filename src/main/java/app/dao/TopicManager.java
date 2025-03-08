package app.dao;

import app.enums.TopicEnum;
import app.enums.UserRoleEnum;
import app.models.Topic;
import app.models.User;
import app.services.DatabaseService;
import app.services.LogService;

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

    public static Topic getTopic(String topicName) {
        String sql = "SELECT * FROM topics WHERE name = ?";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setString(1, topicName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Topic topic =
                            new Topic(
                                    rs.getString("name"),
                                    SubjectManager.getSubject(new Topic(rs.getString("name"))),
                                    rs.getBoolean("isPrivate"),
                                    UserManager.getUser(rs.getInt("userId"))
                            );
                    topic.setSubject(SubjectManager.getSubject(topic));
                    return topic;
                }
            }
        } catch (SQLException e) {
            LogService.logError("Failed to get topic.");
            e.printStackTrace();
        }

        return null;
    }

    public static ArrayList<Topic> getTopicsByPromptId(int promptId) throws SQLException {
        String sql = "SELECT t.* FROM topics t " +
                    "JOIN topics_prompts tp USING (topicId) " +
                    "WHERE tp.promptId = ?";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setInt(1, promptId);

            ResultSet rs = pstmt.executeQuery();
            ArrayList<Topic> topics = new ArrayList<>();

            while (rs.next()) {
                Topic topic = new Topic(rs.getString("name"));
                topic.setSubject(SubjectManager.getSubject(topic));

                topics.add(topic);

            }

            return topics;
        }
    }

    public static boolean save(Topic topic) throws SQLException {
        if (db.getConn() != null) {
            int subjectId = SubjectManager.getId(topic.getSubject());

            if (subjectId == -1) {
                LogService.logError("Subject " + topic.getSubject() + " not found in database.");
                return false;
            }

            if (!DatabaseService.instanceInDatabase("topics", topic.getName(), subjectId, "name", "subjectId")) {
                String sql;
                int userId = 0;
                boolean hasUser = topic.getCreatedBy() != null;

                if (hasUser) {
                    userId = UserManager.getId(topic.getCreatedBy().getEmail());
                    sql = "INSERT INTO topics (subjectId, name, description, isPrivate, userId) VALUES (?, ?, ?, ?, ?)";
                } else {
                    sql = "INSERT INTO topics (subjectId, name, description, isPrivate) VALUES (?, ?, ?, ?)";
                }

                try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                    pstmt.setInt(1, subjectId);
                    pstmt.setString(2, topic.getName());
                    pstmt.setString(3, topic.getDescription());
                    pstmt.setBoolean(4, topic.isPrivate());
                    if (hasUser) pstmt.setInt(5, userId);

                    pstmt.executeUpdate();

                    LogService.logInfo("Topic " + topic.getName() + " inserted into database.");
                    return true;
                } catch (SQLException e) {
                    LogService.logError("Failed to save topic " + topic.getName() + " into database.");
                    e.printStackTrace();
                    return false;
                }
            } else {
                LogService.logInfo("Topic " + topic.getName() + " already exists in database.");
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
                    LogService.logInfo("Topic " + topic.getName() + " deleted from database.");
                    return true;
                } else {
                    LogService.logInfo("Topic " + topic.getName() + " not found in database.");
                    return false;
                }
            } catch (SQLException e) {
                LogService.logError("Failed to delete topic " + topic.getName() + " from database.");
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    public static boolean update(Topic oldTopic, Topic newTopic) throws SQLException {
        String sql;
        int userId = 0;
        boolean hasUser = newTopic.getCreatedBy() != null;

        if (hasUser) {
            userId = UserManager.getId(newTopic.getCreatedBy().getEmail());
            sql = "UPDATE topics SET subjectId = ?, name = ?, description = ?, isPrivate = ?, userId = ? WHERE topicId = ?";
        } else {
            sql = "UPDATE topics SET subjectId = ?, name = ?, description = ?, isPrivate = ? WHERE topicId = ?";
        }

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setInt(1, SubjectManager.getId(newTopic.getSubject()));
            pstmt.setString(2, newTopic.getName());
            pstmt.setString(3, newTopic.getDescription());
            pstmt.setBoolean(4, newTopic.isPrivate());

            if (hasUser) {
                pstmt.setInt(5, userId);
                pstmt.setInt(6, getId(oldTopic));
            } else {
                pstmt.setInt(5, getId(oldTopic));
            }

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                LogService.logInfo("Topic " + newTopic.getName() + " updated in database.");
                return true;
            } else {
                LogService.logInfo("Topic " + newTopic.getName() + " not found in database.");
                return false;
            }
        } catch (SQLException e) {
            LogService.logError("Failed to update topic " + newTopic.getName() + " in database.");
            e.printStackTrace();
            return false;
        }
    }

    public static void saveDefaultTopics() {
        for (Topic topic : defaultTopics) {
            try {
                save(topic);
            } catch (SQLException e) {
                LogService.logError("Failed to save default topic.");
                e.printStackTrace();
                return;
            }
        }
    }

    public static ArrayList<String> getAllTopics() {
        ArrayList<String> topics = new ArrayList<>();
        String sql = "SELECT * FROM topics";

        try (Statement stmt = db.getConn().createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    topics.add(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return topics;
    }

    public static ArrayList<String> getTopics(User user) throws SQLException {
        if (user.getRole().equals(UserRoleEnum.ADMIN)) {
            return getAllTopics();
        } else {
            ArrayList<String> topics = new ArrayList<>();
            ArrayList<String> subjects = SubjectManager.getSubjects(user);

            for (String subject : subjects) {
                topics.addAll(getTopics(subject, user));
            }

            return topics;
        }
    }

    public static ArrayList<String> getTopics(String fromSubjects, User user) throws SQLException {
        int subjectId = SubjectManager.getId(fromSubjects);
        int userId = UserManager.getId(user.getEmail());
        ArrayList<String> topics = new ArrayList<>();

        String sql = "SELECT name FROM topics " +
                "WHERE subjectId = ? " +
                "AND (isPrivate = FALSE OR userId = ?)";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            pstmt.setInt(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    topics.add(rs.getString("name"));
                }
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
            LogService.logError("Failed to get topic ID.");
            e.printStackTrace();
        }
        return topicId;
    }
}