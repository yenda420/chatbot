package app.services;

import app.dao.*;

import java.sql.*;

import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseService {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");
    private static final String DB_NAME = dotenv.get("DB_NAME");

    private static Connection conn;

    public DatabaseService() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL, USER, PASSWORD);

                if (!conn.isClosed()) {
                    LogService.logInfo("Database connected.");
                    LogService.logInfo("Database connected.");
                }
            } else {
                LogService.logInfo("Using existing database connection.");
            }
        } catch (Exception e) {
            LogService.logError("Failed to connect to the database.");
            e.printStackTrace();
        }
    }

    public Connection getConn() {
        return conn;
    }

    private void createDatabase() {
        String sql = "CREATE DATABASE IF NOT EXISTS " + DB_NAME +
                " CHARACTER SET UTF8 COLLATE UTF8_CZECH_CI";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Database '" + DB_NAME + "' created.");
        } catch (SQLException e) {
            LogService.logError("Failed to create database '" + DB_NAME + "'.");
            e.printStackTrace();
        }
    }

    private void useDatabase() {
        String sql = "USE " + DB_NAME;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Now using database '" + DB_NAME + "'.");
        } catch (SQLException e) {
            LogService.logError("Failed to switch to database '" + DB_NAME + "'.");
            e.printStackTrace();
        }
    }

    private void createTableSubjects() {
        String sql = "CREATE TABLE IF NOT EXISTS subjects (" +
                "subjectId INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(50) UNIQUE NOT NULL, " +
                "abbreviation VARCHAR(3) UNIQUE NOT NULL, " +
                "description LONGTEXT)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Table 'subjects' created.");
        } catch (SQLException e) {
            LogService.logError("Failed to create table 'subjects'.");
            e.printStackTrace();
        }
    }

    private void createTableTopics() {
        String sql = "CREATE TABLE IF NOT EXISTS topics (" +
                "topicId INT PRIMARY KEY AUTO_INCREMENT, " +
                "subjectId INT NOT NULL, " +
                "userId INT, " +
                "name VARCHAR(100) UNIQUE NOT NULL, " +
                "description LONGTEXT, " +
                "isPrivate BOOLEAN DEFAULT FALSE, " +
                "CONSTRAINT FK_TOPICS_SUBJECTS FOREIGN KEY (subjectId) REFERENCES subjects (subjectId) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "CONSTRAINT FK_TOPICS_USERS FOREIGN KEY (userId) REFERENCES users (userId) ON DELETE CASCADE ON UPDATE CASCADE)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Table 'topics' created.");
        } catch (SQLException e) {
            LogService.logError("Failed to create table 'topics'.");
            e.printStackTrace();
        }
    }

    private void createTablePrompts() {
        String sql = "CREATE TABLE IF NOT EXISTS prompts (" +
                "promptId INT PRIMARY KEY AUTO_INCREMENT, " +
                "message LONGTEXT, " +
                "attachedFile LONGBLOB)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Table 'prompts' created.");
        } catch (SQLException e) {
            LogService.logError("Failed to create table 'prompts'.");
            e.printStackTrace();
        }
    }

    private void createTableTopicsPrompts() {
        String sql = "CREATE TABLE IF NOT EXISTS topics_prompts (" +
                "topicId INT NOT NULL, " +
                "promptId INT NOT NULL, " +
                "PRIMARY KEY (topicId, promptId), " +
                "CONSTRAINT FK_TOPICS_PROMPTS_TOPICS FOREIGN KEY (topicId) REFERENCES topics (topicId) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "CONSTRAINT FK_TOPICS_PROMPTS_PROMPTS FOREIGN KEY (promptId) REFERENCES prompts (promptId) ON DELETE CASCADE ON UPDATE CASCADE)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Table 'topics_prompts' created.");
        } catch (SQLException e) {
            LogService.logError("Failed to create table 'topics_prompts'.");
            e.printStackTrace();
        }
    }

    private void createTableQuestions() {
        String sql = "CREATE TABLE IF NOT EXISTS questions (" +
                "questionId INT PRIMARY KEY AUTO_INCREMENT, " +
                "text LONGTEXT NOT NULL, " +
                "type ENUM('Ano / Ne', 'Výběr z odpovědí', 'Otevřená otázka') NOT NULL, " +
                "difficulty ENUM('Lehká', 'Těžká', 'Střední') NOT NULL, " +
                "points INT NOT NULL)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Table 'questions' created.");
        } catch (SQLException e) {
            LogService.logError("Failed to create table 'questions'.");
            e.printStackTrace();
        }
    }

    private void createTableUsers() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "userId INT PRIMARY KEY AUTO_INCREMENT, " +
                "firstName VARCHAR(50), " +
                "lastName VARCHAR(50), " +
                "email VARCHAR(100) UNIQUE NOT NULL, " +
                "passwordHash VARCHAR(255) NOT NULL, " +
                "role ENUM('Administrátor', 'Učitel') NOT NULL)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Table 'users' created.");
        } catch (SQLException e) {
            LogService.logError("Failed to create table 'users'.");
            e.printStackTrace();
        }
    }

    private void createTableUsersSubjects() {
        String sql = "CREATE TABLE IF NOT EXISTS users_subjects (" +
                "userId INT NOT NULL, " +
                "subjectId INT NOT NULL, " +
                "PRIMARY KEY (userId, subjectId), " +
                "CONSTRAINT FK_USERS_SUBJECTS_USERS FOREIGN KEY (userId) REFERENCES users (userId) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "CONSTRAINT FK_USERS_SUBJECTS_SUBJECTS FOREIGN KEY (subjectId) REFERENCES subjects (subjectId) ON DELETE CASCADE ON UPDATE CASCADE)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Table 'users_subjects' created.");
        } catch (SQLException e) {
            LogService.logError("Failed to create table 'users_subjects'.");
            e.printStackTrace();
        }
    }

    private void createTableTests() {
        String sql = "CREATE TABLE IF NOT EXISTS tests (" +
                "testId INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(100) NOT NULL, " +
                "difficulty ENUM('Lehká', 'Těžká', 'Střední') NOT NULL, " +
                "numberOfQuestions INT NOT NULL, " +
                "timeLimit INT NOT NULL, " +
                "promptId INT UNIQUE NOT NULL, " +
                "userId INT NOT NULL, " +
                "CONSTRAINT FK_TESTS_PROMPTS FOREIGN KEY (promptId) REFERENCES prompts (promptId) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "CONSTRAINT FK_TESTS_USERS FOREIGN KEY (userId) REFERENCES users (userId) ON DELETE CASCADE ON UPDATE CASCADE)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Table 'tests' created.");
        } catch (SQLException e) {
            LogService.logError("Failed to create table 'tests'.");
            e.printStackTrace();
        }
    }

    private void createTableQuestionsTests() {
        String sql = "CREATE TABLE IF NOT EXISTS questions_tests (" +
                "questionId INT NOT NULL, " +
                "testId INT NOT NULL, " +
                "PRIMARY KEY (questionId, testId), " +
                "CONSTRAINT FK_QUESTIONS_TESTS_QUESTIONS FOREIGN KEY (questionId) REFERENCES questions (questionId) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "CONSTRAINT FK_QUESTIONS_TESTS_TESTS FOREIGN KEY (testId) REFERENCES tests (testId) ON DELETE CASCADE ON UPDATE CASCADE)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Table 'questions_tests' created.");
        } catch (SQLException e) {
            LogService.logError("Failed to create table 'questions_tests'.");
            e.printStackTrace();
        }
    }

    private void createTableAnswers() {
        String sql = "CREATE TABLE IF NOT EXISTS answers (" +
                "answerId INT PRIMARY KEY AUTO_INCREMENT, " +
                "text LONGTEXT UNIQUE NOT NULL)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Table 'answers' created.");
        } catch (SQLException e) {
            LogService.logError("Failed to create table 'answers'.");
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
                "CONSTRAINT FK_QUESTIONS_ANSWERS_QUESTIONS FOREIGN KEY (questionId) REFERENCES questions (questionId) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "CONSTRAINT FK_QUESTIONS_ANSWERS_ANSWERS FOREIGN KEY (answerId) REFERENCES answers (answerId) ON DELETE CASCADE ON UPDATE CASCADE)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LogService.logInfo("Table 'questions_answers' created.");
        } catch (SQLException e) {
            LogService.logError("Failed to create table 'questions_answers'.");
            e.printStackTrace();
        }
    }

    // Specifically for simple tables with one column
    public static boolean instanceInDatabase(String table, String column, String instance) throws SQLException {
        String sql = "SELECT * FROM " + table + " WHERE " + column + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, instance);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    // Specifically for simple tables with one column and an ID
    public static boolean instanceInDatabase(String table, String value, int id, String valueColumn, String idColumn) throws SQLException {
        String sql = "SELECT * FROM " + table + " WHERE " + valueColumn + " = ? AND " + idColumn + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            pstmt.setInt(2, id);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    // Specifically for Many-to-Many relationships
    public static boolean instanceInDatabase(String table, int firstId, int secondId, String firstColumn, String secondColumn) throws SQLException {
        String sql = "SELECT * FROM " + table + " WHERE " + firstColumn + " = ? AND " + secondColumn + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, firstId);
            pstmt.setInt(2, secondId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void saveDefaultData() {
        SubjectManager.saveDefaultSubjects();
        TopicManager.saveDefaultTopics();
        UserManager.saveDefaultUsers();
    }

    // This will force to create connections to all the managers
    private void setConnections() {
        AnswerManager answerManager = new AnswerManager();
        PromptManager promptManager = new PromptManager();
        QuestionManager questionManager = new QuestionManager();
        SubjectManager subjectManager = new SubjectManager();
        TestManager testManager = new TestManager();
        TopicManager topicManager = new TopicManager();
        UserManager userManager = new UserManager();
    }

    public static void initialize() {
        DatabaseService dbService = new DatabaseService();

        if (dbService.getConn() != null) {
            // dbService.dropDatabase(); // Only for testing
            dbService.createDatabase();
            dbService.useDatabase();

            dbService.createTableSubjects();
            dbService.createTableUsers();
            dbService.createTableTopics();
            dbService.createTablePrompts();
            dbService.createTableTopicsPrompts();
            dbService.createTableQuestions();
            dbService.createTableUsersSubjects();
            dbService.createTableTests();
            dbService.createTableQuestionsTests();
            dbService.createTableAnswers();
            dbService.createTableQuestionsAnswers();

            dbService.setConnections();
            dbService.saveDefaultData();
        } else {
            LogService.logError("Database initialization failed due to connection issues.");
        }
    }

    // ======================== //
    // DELETE THIS METHOD LATER //
    // ======================== //
    private void dropDatabase() {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP DATABASE IF EXISTS " + DB_NAME);
            LogService.logInfo("Database '" + DB_NAME + "' dropped.");
        } catch (SQLException e) {
            LogService.logError("Failed to drop database '" + DB_NAME + "'.");
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    LogService.logInfo("Database connection closed.");
                }
            } catch (SQLException e) {
                LogService.logError("Failed to close the database connection.");
                e.printStackTrace();
            }
        }
    }
}