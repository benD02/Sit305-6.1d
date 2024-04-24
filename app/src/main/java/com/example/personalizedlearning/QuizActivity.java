package com.example.personalizedlearning;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private String userName;

    private int quizId;


    private Quiz currentQuiz;
    private int currentQuestionIndex = 0;

    private int correctAnswers = 0;

    private TextView questionTextView;
    private Button option1Button, option2Button, option3Button, option4Button;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionTextView = findViewById(R.id.questionTextView);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        option4Button = findViewById(R.id.option4Button);
        progressBar = findViewById(R.id.progressBar);

        quizId = getIntent().getIntExtra("quizId", -1);


        String quizData = getIntent().getStringExtra("quiz_data");
        if (quizData == null) {
            Toast.makeText(this, "Quiz data is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentQuiz = new Gson().fromJson(quizData, Quiz.class);

        if (currentQuiz == null || currentQuiz.getQuestions() == null || currentQuiz.getQuestions().isEmpty()) {
            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        displayCurrentQuestion();

    }

    private void displayCurrentQuestion() {
        Question currentQuestion = currentQuiz.getQuestions().get(currentQuestionIndex);
        questionTextView.setText(currentQuestion.getQuestionText());
        List<String> options = currentQuestion.getOptions();

        LinearLayout optionsLayout = findViewById(R.id.optionsRadioGroup);
        optionsLayout.removeAllViews();  // Clear previous options
        for (int i = 0; i < options.size(); i++) {
            Button optionButton = new Button(this);
            optionButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            optionButton.setText(options.get(i));
            optionButton.setTextSize(16);
            int finalI = i;
            optionButton.setOnClickListener(view -> checkAnswer(finalI));
            optionsLayout.addView(optionButton);
            progressBar.setProgress((int) (((float) currentQuestionIndex / currentQuiz.getQuestions().size()) * 100));

        }

    }



    public void loadQuizFromDatabase(int quizId) {
        DatabaseHelper db = new DatabaseHelper(this);
        currentQuiz = db.getQuizById(quizId);
        if (currentQuiz != null && !currentQuiz.getQuestions().isEmpty()) {
            displayCurrentQuestion();
        } else {
            Toast.makeText(this, "Failed to load quiz or no questions available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }



    private void checkAnswer(int selectedOptionIndex) {
        int correctAnswerIndex = currentQuiz.getQuestions().get(currentQuestionIndex).getCorrectAnswerIndex();
        if (selectedOptionIndex == correctAnswerIndex) {
            correctAnswers++;  // Increment the count of correct answers
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT).show();
        }

        // Move to the next question or finish the quiz
        if (currentQuestionIndex < currentQuiz.getQuestions().size() - 1) {
            currentQuestionIndex++;
            displayCurrentQuestion();
        } else {
            finishQuiz();
        }
    }


    private void finishQuiz() {
        int totalQuestions = currentQuiz.getQuestions().size();
        Toast.makeText(this, "Quiz Completed", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("userName", userName);
        intent.putExtra("totalQuestions", totalQuestions);
        intent.putExtra("correctAnswers", correctAnswers);
        intent.putExtra("quizId", quizId);
        startActivity(intent);
        finish();
    }

}
