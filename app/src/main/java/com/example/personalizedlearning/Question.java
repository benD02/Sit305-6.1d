package com.example.personalizedlearning;

import java.io.Serializable;
import java.util.List;

public class Question {
    private String questionText;
    private List<String> options;
    private int correctAnswerIndex;

    // Constructor
    public Question(String questionText, List<String> options, int correctAnswerIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public String getCorrectAnswer() {
        return options.get(correctAnswerIndex);
    }


}
