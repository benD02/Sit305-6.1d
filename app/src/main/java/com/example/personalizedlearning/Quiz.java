package com.example.personalizedlearning;
import java.io.Serializable;
import java.util.List;

public class Quiz implements Serializable {
    private int quizId; // Unique identifier for the quiz
    private String quizName;
    private String quizDescription;
    private List<Question> questions;

    // Updated constructor
    public Quiz(int quizId, String quizName, String quizDescription, List<Question> questions) {
        this.quizId = quizId;
        this.quizName = quizName;
        this.quizDescription = quizDescription;
        this.questions = questions;
    }

    // Getters and setters
    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getQuizName() {
        return quizName;
    }

    public String getQuizDescription() {
        return quizDescription;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public int getTotalQuestions() {
        return questions != null ? questions.size() : 0;
    }
}

