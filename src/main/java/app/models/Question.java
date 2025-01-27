package app.models;

import app.dao.QuestionManager;

import app.enums.DifficultyEnum;
import app.enums.QuestionTypeEnum;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Question {
    private String text;
    private QuestionTypeEnum type;
    private DifficultyEnum difficulty;
    private int points;
    private List<Answer> answers;
    private String correctAnswer;
    private String explanation;

    public Question(String text, QuestionTypeEnum type, DifficultyEnum difficulty, int points) {
        this.text = text;
        this.type = type;
        this.difficulty = difficulty;
        this.points = points;
        this.explanation = null;

        this.answers = new ArrayList<>();

        if (type.equals(QuestionTypeEnum.YES_NO)) {
            this.answers.add(new Answer("Ano"));
            this.answers.add(new Answer("Ne"));
        } else if (type.equals(QuestionTypeEnum.OPEN_ENDED)) {
            this.answers = null;
        }

    }

    public String getText() {
        return text;
    }

    public QuestionTypeEnum getType() {
        return type;
    }

    public DifficultyEnum getDifficulty() {
        return difficulty;
    }

    public int getPoints() {
        return points;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public int getId() throws SQLException {
        return QuestionManager.getId(this);
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public boolean relate(int testId) throws SQLException {
        return QuestionManager.linkQuestionToTest(this, testId);
    }

    @Override
    public String toString() {
        return "Question{" +
                "text='" + text + '\'' +
                ", type=" + type +
                ", difficulty=" + difficulty +
                ", points=" + points +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}
