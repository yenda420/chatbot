package app.dao;

import app.models.Prompt;
import app.models.Topic;
import app.services.DatabaseService;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;

public class PromptManager {
    private static DatabaseService db;

    public PromptManager() {
        db = new DatabaseService();
    }

    public static int insert(Prompt prompt) throws SQLException {
        int promptId = -1;

        if (db.getConn() != null) {
            // Ensure that tags and topics are not null and topics is not empty
            if (prompt.getTags() != null && prompt.getTopics() != null && !prompt.getTopics().isEmpty()) {
                String sql;
                PreparedStatement pstmt = null;

                try {
                    if (prompt.getMessage() != null && prompt.getAttachedFile() != null) {
                        // Both message and attachedFile are present
                        sql = "INSERT INTO prompts (message, attachedFile, tags) VALUES (?, ?, ?)";
                        pstmt = db.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        pstmt.setString(1, prompt.getMessage());
                        FileInputStream fis = new FileInputStream(prompt.getAttachedFile());
                        pstmt.setBinaryStream(2, fis, (int) prompt.getAttachedFile().length());
                        pstmt.setString(3, prompt.getTags());

                    } else if (prompt.getMessage() != null) {
                        // Only message is present
                        sql = "INSERT INTO prompts (message, tags) VALUES (?, ?)";
                        pstmt = db.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        pstmt.setString(1, prompt.getMessage());
                        pstmt.setString(2, prompt.getTags());

                    } else if (prompt.getAttachedFile() != null) {
                        // Only attachedFile is present
                        sql = "INSERT INTO prompts (attachedFile, tags) VALUES (?, ?)";
                        pstmt = db.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        FileInputStream fis = new FileInputStream(prompt.getAttachedFile());
                        pstmt.setBinaryStream(1, fis, (int) prompt.getAttachedFile().length());
                        pstmt.setString(2, prompt.getTags());

                    } else {
                        // Neither message nor attachedFile is present
                        sql = "INSERT INTO prompts (tags) VALUES (?)";
                        pstmt = db.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        pstmt.setString(1, prompt.getTags());
                    }

                    pstmt.executeUpdate();

                    // Get the generated promptId
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            promptId = generatedKeys.getInt(1);
                            System.out.println("[INFO] - Prompt " + prompt + " inserted into database.");
                        } else {
                            throw new SQLException("[ERROR] - Failed to insert prompt " + promptId + " into database, no ID obtained.");
                        }
                    }

                    // Link the prompt to its topics in the topics_prompts table
                    String linkSql = "INSERT INTO topics_prompts (topicId, promptId) VALUES (?, ?)";
                    PreparedStatement linkStmt = db.getConn().prepareStatement(linkSql);

                    for (Topic topic : prompt.getTopics()) {
                        int topicId = TopicManager.getId(topic.getName());

                        linkStmt.setInt(1, topicId);
                        linkStmt.setInt(2, promptId);
                        linkStmt.addBatch();
                    }

                    linkStmt.executeBatch();

                    System.out.println("[INFO] - Prompt " + prompt + " linked to topics in 'topics_prompts' table.");

                    return promptId;
                } catch (SQLException e) {
                    System.err.println("[ERROR] - Failed to insert prompt " + promptId + " into database.");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.err.println("[ERROR] - Failed to read attached file " + prompt.getAttachedFile() + ".");
                    e.printStackTrace();
                } finally {
                    if (pstmt != null) {
                        pstmt.close();
                    }
                }
            } else {
                if (prompt.getTags() == null) {
                    System.err.println("[ERROR] - Prompt 'tags' cannot be null.");
                }
                if (prompt.getTopics() == null || prompt.getTopics().isEmpty()) {
                    System.err.println("[ERROR] - Prompt must have at least one topic.");
                }
            }
        } else {
            System.err.println("[ERROR] - Database connection is null.");
        }

        return promptId;
    }
}