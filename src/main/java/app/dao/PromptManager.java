package app.dao;

import app.models.Prompt;
import app.models.Topic;
import app.services.DatabaseService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;

public class PromptManager {
    private static DatabaseService db;

    public PromptManager() {
        db = new DatabaseService();
    }

    // Inserts the prompt to the database and links it to its topics
    public static int save(Prompt prompt) throws SQLException, FileNotFoundException {
        int promptId = -1;

        if (db.getConn() != null) {
            String sql;
            PreparedStatement pstmt = null;

            String message = prompt.getMessage();
            String tags = prompt.getTags();

            File file = prompt.getAttachedFile();
            FileInputStream fis = file == null ? null : new FileInputStream(file);

            // Insert the prompt
            try {
                sql = "INSERT INTO prompts (message, attachedFile, tags) VALUES (?, ?, ?)";
                pstmt = db.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                if (message != null) {
                    pstmt.setString(1, message);
                } else {
                    pstmt.setNull(1, Types.VARCHAR);
                }

                if (file != null) {
                    pstmt.setBinaryStream(2, fis, (int) file.length());
                } else {
                    pstmt.setNull(2, Types.BLOB);
                }
                pstmt.setString(1, prompt.getMessage());

                if (tags != null) {
                    pstmt.setString(3, tags);
                } else {
                    pstmt.setNull(3, Types.VARCHAR);
                }

                pstmt.executeUpdate();

                // Get the generated promptId
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        promptId = generatedKeys.getInt(1);
                        System.out.println("[INFO] - Prompt " + prompt + " inserted into database.");
                    } else {
                        throw new SQLException("[ERROR] - Failed to save prompt " + promptId + " into database, no ID obtained.");
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
                System.err.println("[ERROR] - Failed to save prompt " + promptId + " into database.");
                e.printStackTrace();
            } finally {
                if (pstmt != null) {
                    pstmt.close();
                }
            }
        } else {
            System.err.println("[ERROR] - Database connection is null.");
        }

        return promptId;
    }
}