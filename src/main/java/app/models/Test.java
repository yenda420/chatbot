package app.models;

import app.enums.DifficultyEnum;
import app.enums.QuestionTypeEnum;

public class Test {
    private String name;
    private int numberOfQuestions;
    private int timeLimitInMinutes;
    private DifficultyEnum difficulty;
    private QuestionTypeEnum questionType;
    private Prompt fromPrompt;

    public Test(String name, int numberOfQuestions, int timeLimitInMinutes, DifficultyEnum difficulty, QuestionTypeEnum questionType, Prompt fromPrompt) {
        this.name = name;
        this.numberOfQuestions = numberOfQuestions;
        this.timeLimitInMinutes = timeLimitInMinutes;
        this.difficulty = difficulty;
        this.questionType = questionType;
        this.fromPrompt = fromPrompt;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public int getTimeLimitInMinutes() {
        return timeLimitInMinutes;
    }

    public DifficultyEnum getDifficulty() {
        return difficulty;
    }

    public QuestionTypeEnum getQuestionType() {
        return questionType;
    }

    public Prompt getFromPrompt() {
        return fromPrompt;
    }

    @Override
    public String toString() {
        return "Test{" +
                "name='" + name + '\'' +
                ", numberOfQuestions=" + numberOfQuestions +
                ", timeLimitInMinutes=" + timeLimitInMinutes +
                ", difficulty=" + difficulty +
                ", questionType=" + questionType +
                ", fromPrompt=" + fromPrompt +
                '}';
    }
}
