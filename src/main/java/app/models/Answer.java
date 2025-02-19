package app.models;

import app.dao.AnswerManager;
import java.sql.SQLException;

public class Answer {
    private int id;
    private String text;
    private boolean isCorrect;
    private String explanation;

    public Answer(String text) {
        this.text = text;
        this.isCorrect = false;
        this.explanation = null;
    }

    public Answer(String text, boolean isCorrect, String explanation) {
        this.text = text;
        this.isCorrect = isCorrect;
        this.explanation = explanation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", isCorrect=" + isCorrect +
                ", explanation='" + explanation + '\'' +
                '}';
    }

    public boolean relate(Question question) throws SQLException {
        return AnswerManager.linkAnswerToQuestion(this, question);
    }
}