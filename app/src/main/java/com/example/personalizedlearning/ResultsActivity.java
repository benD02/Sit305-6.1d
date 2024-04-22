package com.example.personalizedlearning;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        String userName = getIntent().getStringExtra("userName");
        String quizData = getIntent().getStringExtra("quiz_data");  // Ensure you pass this from QuizActivity

        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);
        int correctAnswers = getIntent().getIntExtra("correctAnswers", 0);

        TextView resultsTextView = findViewById(R.id.resultsTextView);
        String resultsText = "Congratulations, " + userName + "!\nYou answered " + correctAnswers +
                " out of " + totalQuestions + " questions correctly.";
        resultsTextView.setText(resultsText);

        Button retakeQuizButton = findViewById(R.id.retakeQuizButton);
        Button finishQuizButton = findViewById(R.id.finishQuizButton);

        retakeQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultsActivity.this, QuizActivity.class);
                intent.putExtra("quiz_data", quizData);  // Pass the quiz data back
                startActivity(intent);
                finish();  // Finish ResultsActivity to remove it from the back stack
            }
        });
        finishQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quizId = getIntent().getIntExtra("quizId", -1);  // Retrieve the passed quiz ID
                if (quizId != -1) {
                    markCompletedAndShowResults(quizId);  // Call the method to mark the quiz as completed
                } else {
                    Toast.makeText(ResultsActivity.this, "Error: Quiz ID not found.", Toast.LENGTH_SHORT).show();
                    // Handle the error appropriately
                }
            }
        });

    }

    public void markCompletedAndShowResults(int quizId) {
        DatabaseHelper db = new DatabaseHelper(this);
        db.markQuizAsCompleted(quizId);  // Mark the quiz as completed in the database

        Toast.makeText(this, "Quiz marked as completed.", Toast.LENGTH_SHORT).show();

        // Optionally refresh the activity or return to the profile with updated data
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


}
