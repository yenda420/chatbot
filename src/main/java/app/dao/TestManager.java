package app.dao;

import app.services.DatabaseService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class TestManager {
    private static DatabaseService db;

    public TestManager() {
        db = new DatabaseService();
    }

    public static ArrayList<String> getQuestionTypes() {
        ArrayList<String> types = new ArrayList<>();
        String sql = "SELECT COLUMN_TYPE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = ? " +
                "AND COLUMN_NAME = ?";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setString(1, "questions");
            pstmt.setString(2, "type");

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String columnType = rs.getString("COLUMN_TYPE");
                // Extract the enum values from the COLUMN_TYPE string
                String enumValues = columnType.substring(columnType.indexOf("(") + 1, columnType.lastIndexOf(")"));
                // Split the values by comma
                String[] values = enumValues.split(",");

                for (String value : values) {
                    // Remove single quotes and trim whitespace
                    value = value.trim().replace("'", "");
                    types.add(value);
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] - Failed to get question types");
            e.printStackTrace();
        }

        return types;
    }

    public static ArrayList<String> getQuestionDifficulties() {
        ArrayList<String> difficulties = new ArrayList<>();
        String sql = "SELECT COLUMN_TYPE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = ? " +
                "AND COLUMN_NAME = ?";

        try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
            pstmt.setString(1, "questions");
            pstmt.setString(2, "difficulty");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String columnType = rs.getString("COLUMN_TYPE");
                    // Extract the enum values from the COLUMN_TYPE string
                    String enumValues = columnType.substring(columnType.indexOf("(") + 1, columnType.lastIndexOf(")"));
                    // Split the values by comma
                    String[] values = enumValues.split(",");

                    for (String value : values) {
                        // Remove single quotes and trim whitespace
                        value = value.trim().replace("'", "");
                        difficulties.add(value);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] - Failed to get question difficulties");
            e.printStackTrace();
        }

        return difficulties;
    }
}