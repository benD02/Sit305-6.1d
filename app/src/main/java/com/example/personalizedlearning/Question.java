package com.example.personalizedlearning;

import java.io.Serializable;
import java.util.List;

public class Question {
    private String questionText;
    private List<String> options;
    private int correctAnswerIndex;  // Assuming correct answer is stored as an index

    // Constructor
    public Question(String questionText, List<String> options, int correctAnswerIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    // Getter for question text
    public String getQuestionText() {
        return questionText;
    }

    // Getter for options
    public List<String> getOptions() {
        return options;
    }

    // Getter for correct answer index
    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    // Optional: Getter for correct answer text (if needed)
    public String getCorrectAnswer() {
        return options.get(correctAnswerIndex);
    }


}
