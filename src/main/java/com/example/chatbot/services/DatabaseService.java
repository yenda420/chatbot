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
                "shortage VARCHAR(3) NOT NULL, " +
                "description LONGTEXT)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("[INFO] - Table 'subjects' created.");
        } catch (SQLException e) {
            System.err.println("[ERROR] - Failed to create table 'subjects'.");
            e.printStackTrace();
        }
    }

    public static void initialize() {
        DatabaseService dbService = new DatabaseService();
        if (dbService.getConn() != null) {
            dbService.createDatabase();
            dbService.useDatabase();
            dbService.createTableSubjects();
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