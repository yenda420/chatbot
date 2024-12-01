package app.dao;

import app.services.DatabaseService;

public class AnswerManager {
    private static DatabaseService db;

    public AnswerManager() {
        db = new DatabaseService();
    }
}
