package com.example.personalizedlearning;

import java.io.Serializable;
import java.util.List;

public class Quiz implements Serializable {
    private String quizName;
    private String quizDescription;
    private List<Question> questions; // Ensure Question class is Serializable too

    public Quiz(String quizName, String quizDescription, List<Question> questions) {
        this.quizName = quizName;
        this.quizDescription = quizDescription;

    }

    // Getters
    public String getQuizName() {
        return quizName;
    }

    public String getQuizDescription() {
        return quizDescription;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    // Setter
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
