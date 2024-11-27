package com.example.chatbot.services;

import java.sql.*;

public class DatabaseService {
    private static Connection conn;

    private DatabaseService() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/", "root", "");
            if (!conn.isClosed()) {
                System.out.println("[INFO] - Database connected.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to connect to the database.");
            e.printStackTrace();
        }
    }

    public Connection getConn() {
        return conn;
    }

    private void createDatabase() {
        String sql = "CREATE DATABASE IF NOT EXISTS test_generator " +
                "CHARACTER SET UTF8 COLLATE UTF8_CZECH_CI";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("[INFO] - Database 'test_generator' created.");
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to create database 'test_generator'.");
            e.printStackTrace();
        }
    }

    private void useDatabase() {
        String sql = "USE test_generator";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("[INFO] - Now using database 'test_generator'.");
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to switch to database 'test_generator'.");
            e.printStackTrace();
        }
    }

    private void createTableSubjects() {
        String sql = "CREATE TABLE IF NOT EXISTS subjects (" +
                "subjectId INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(50) NOT NULL, " +
                "shortage VARCHAR(10) NOT NULL, " +
                "description LONGTEXT)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("[INFO] - Table 'subjects' created.");
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to create table 'subjects'.");
            e.printStackTrace();
        }
    }

    private void createTableTopics() {
        String sql = "CREATE TABLE IF NOT EXISTS topics (" +
                "topicId INT PRIMARY KEY AUTO_INCREMENT, " +
                "subjectId INT NOT NULL, " +
                "name VARCHAR(100) NOT NULL, " +
                "description LONGTEXT, " +
                "FOREIGN KEY (subjectId) REFERENCES subjects(subjectId))";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("[INFO] - Table 'topics' created.");
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to create table 'topics'.");
            e.printStackTrace();
        }
    }

    private void createTablePrompts() {
        String sql = "CREATE TABLE IF NOT EXISTS prompts (" +
                "promptId INT PRIMARY KEY AUTO_INCREMENT, " +
                "message LONGTEXT NOT NULL, " +
                "attachedFile LONGBLOB, " +
                "tags VARCHAR(255))";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("[INFO] - Table 'prompts' created.");
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to create table 'prompts'.");
            e.printStackTrace();
        }
    }

    private void createTableTopicsPrompts() {
        String sql = "CREATE TABLE IF NOT EXISTS topics_prompts (" +
                "topicId INT NOT NULL, " +
                "promptId INT NOT NULL, " +
                "PRIMARY KEY (topicId, promptId), " +
                "FOREIGN KEY (topicId) REFERENCES topics(topicId), " +
                "FOREIGN KEY (promptId) REFERENCES prompts(promptId))";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("[INFO] - Table 'topics_prompts' created.");
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to create table 'topics_prompts'.");
            e.printStackTrace();
        }
    }

    private void createTableQuestions() {
        String sql = "CREATE TABLE IF NOT EXISTS questions (" +
                "questionId INT PRIMARY KEY AUTO_INCREMENT, " +
                "text LONGTEXT NOT NULL, " +
                "type ENUM('Ano / Ne', 'Výběr z odpověí', 'Otevřená otázka') NOT NULL, " +
                "difficulty INT, " +
                "points INT)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("[INFO] - Table 'questions' created.");
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to create table 'questions'.");
            e.printStackTrace();
        }
    }

    // This should enforce a One-to-One relationship between Tests and Prompts
    private void createTableTests() {
        String sql = "CREATE TABLE IF NOT EXISTS tests (" +
                "testId INT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "difficulty INT, " +
                "numberOfQuestions INT, " +
                "timeLimit INT, " +
                "promptId INT UNIQUE, " +
                "FOREIGN KEY (promptId) REFERENCES prompts(promptId) " +
                "ON DELETE CASCADE ON UPDATE CASCADE)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("[INFO] - Table 'tests' created with one-to-one relationship to 'prompts'.");
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to create table 'tests'.");
            e.printStackTrace();
        }
    }

    private void createTableQuestionsTests() {
        String sql = "CREATE TABLE IF NOT EXISTS questions_tests (" +
                "questionId INT NOT NULL, " +
                "testId INT NOT NULL, " +
                "PRIMARY KEY (questionId, testId), " +
                "FOREIGN KEY (questionId) REFERENCES questions(questionId), " +
                "FOREIGN KEY (testId) REFERENCES tests(testId))";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("[INFO] - Table 'questions_tests' created.");
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to create table 'questions_tests'.");
            e.printStackTrace();
        }
    }

    private void createTableAnswers() {
        String sql = "CREATE TABLE IF NOT EXISTS answers (" +
                "answerId INT PRIMARY KEY AUTO_INCREMENT, " +
                "text LONGTEXT NOT NULL)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("[INFO] - Table 'answers' created.");
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to create table 'answers'.");
            e.printStackTrace();
        }
    }

    private void createTableQuestionsAnswers() {
        String sql = "CREATE TABLE IF NOT EXISTS questions_answers (" +
                "questionId INT NOT NULL, " +
                "answerId INT NOT NULL, " +
                "isCorrect BOOLEAN NOT NULL, " +
                "explanation LONGTEXT, " +
                "PRIMARY KEY (questionId, answerId), " +
                "FOREIGN KEY (questionId) REFERENCES questions(questionId), " +
                "FOREIGN KEY (answerId) REFERENCES answers(answerId))";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("[INFO] - Table 'questions_answers' created.");
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to create table 'questions_answers'.");
            e.printStackTrace();
        }
    }

    public static void initialize() {
        DatabaseService dbService = new DatabaseService();
        if (dbService.getConn() != null) {
            dbService.createDatabase();
            dbService.useDatabase();
            dbService.createTableSubjects();
            dbService.createTableTopics();
            dbService.createTablePrompts();
            dbService.createTableTopicsPrompts();
            dbService.createTableQuestions();
            dbService.createTableTests(); // This will now enforce a 1:1 relationship
            dbService.createTableQuestionsTests();
            dbService.createTableAnswers();
            dbService.createTableQuestionsAnswers();
        } else {
            System.err.println("[ERROR] - Database initialization failed due to connection issues.");
        }
    }

    public static void disconnect() {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    System.out.println("[INFO] - Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to close the database connection.");
                e.printStackTrace();
            }
        }
    }
}