package com.example.personalizedlearning;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private Quiz currentQuiz;
    private int currentQuestionIndex = 0;
    private TextView questionTextView;
    private Button option1Button, option2Button, option3Button, option4Button;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize UI components
        questionTextView = findViewById(R.id.questionTextView);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        option4Button = findViewById(R.id.option4Button);
        progressBar = findViewById(R.id.progressBar);

        // Get serialized quiz data from intent and deserialize it
        String quizData = getIntent().getStringExtra("quiz_data");
        Gson gson = new Gson();
        currentQuiz = gson.fromJson(quizData, Quiz.class);

        if (currentQuiz != null && !currentQuiz.getQuestions().isEmpty()) {
            displayCurrentQuestion();
        } else {
            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
            finish();  // Close activity if no questions
        }
    }

    private void displayCurrentQuestion() {
        Question currentQuestion = currentQuiz.getQuestions().get(currentQuestionIndex);
        questionTextView.setText(currentQuestion.getQuestionText());
        List<String> options = currentQuestion.getAnswers();

        // Set text on buttons and handle click events
        Button[] buttons = {option1Button, option2Button, option3Button, option4Button};
        for (int i = 0; i < options.size(); i++) {
            buttons[i].setText(options.get(i));
            buttons[i].setOnClickListener(view -> checkAnswer(i));
        }

        // Update progress bar
        progressBar.setProgress((int) (((float) currentQuestionIndex / currentQuiz.getQuestions().size()) * 100));
    }

    private void checkAnswer(int selectedOptionIndex) {
        int correctAnswerIndex = currentQuiz.getQuestions().get(currentQuestionIndex).getCorrectAnswerIndex();
        if (selectedOptionIndex == correctAnswerIndex) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT).show();
        }

        // Move to next question or finish quiz
        if (currentQuestionIndex < currentQuiz.getQuestions().size() - 1) {
            currentQuestionIndex++;
            displayCurrentQuestion();
        } else {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        Toast.makeText(this, "Quiz Completed", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity
    }
}
