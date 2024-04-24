package com.example.personalizedlearning;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity implements QuizAdapter.OnQuizListener {
    private TextView textView;

    private Button btnRefreshQuizzes;

    private Button btnGenerate;
    private String username;


    private RecyclerView recyclerView;
    private List<Quiz> quizzes = new ArrayList<>();
    private QuizAdapter adapter;
    private int currentInterestIndex = 0;
    private ArrayList<String> userInterests;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile_activity);
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        initView();
        loadData();
    }

    private void initView() {
        textView = findViewById(R.id.textView);
        TextView tvUser = findViewById(R.id.tv_user);
        tvUser.setText(username);
        btnGenerate = findViewById(R.id.btnGenerate);
        recyclerView = findViewById(R.id.rv_quiz_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizAdapter(quizzes, this);
        recyclerView.setAdapter(adapter);
        btnRefreshQuizzes = findViewById(R.id.btnRefreshQuizzes);
        btnRefreshQuizzes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshQuizzes();
            }
        });

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateQuiz();
            }
        });
    }

    private void refreshQuizzes() {

        DatabaseHelper db = new DatabaseHelper(this);
        String username = getUsername();
        if (username == null) {
            Toast.makeText(this, "Session expired, please login again.", Toast.LENGTH_LONG).show();
            return;
        }
        int userId = db.getUserId(username);
        if (userId != -1) {
            quizzes = db.getQuizzesForUser(userId);
            adapter.setQuizzes(quizzes);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Quizzes refreshed.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to fetch quizzes.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadData() {
        DatabaseHelper db = new DatabaseHelper(this);
        String username = getUsername();
        if (username == null) {
            Toast.makeText(this, "User not found, please login again.", Toast.LENGTH_LONG).show();
            return;
        }
        int userId = db.getUserId(username);
        if (userId == -1) {
            Toast.makeText(this, "User ID not found for username: " + username, Toast.LENGTH_LONG).show();
            return;
        }

        // Retrieve the image path from the database
        String imagePath = db.getUserImage(userId);

        ImageView profileImageView = findViewById(R.id.profile_picture);
        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher) // Provide a default image in case of error
                    .into(profileImageView); // Set it to the ImageView
        } else {
            profileImageView.setImageResource(R.mipmap.ic_launcher); // Set a default image if the path is null
        }

        userInterests = db.getUserInterests(userId);
        quizzes = db.getQuizzesForUser(userId);
        if (quizzes.isEmpty()) {
            Log.d("ProfileActivity", "No quizzes found for user ID: " + userId);
            Toast.makeText(this, "No quizzes available. Start by generating new quizzes.", Toast.LENGTH_LONG).show();
        } else {
            adapter.setQuizzes(quizzes);
            adapter.notifyDataSetChanged();
        }
        updateQuizCountView();
    }

    private void updateQuizCountView() {
        DatabaseHelper db = new DatabaseHelper(this);
        String username = getUsername();
        int userId = db.getUserId(username);
        if (userId != -1) {
            int incompleteCount = db.getIncompleteQuizCount(userId);
            TextView taskDueTextView = findViewById(R.id.tv_task_due);
            taskDueTextView.setText("You have " + incompleteCount + " tasks due");
        } else {
            Log.e("ProfileActivity", "Failed to fetch user ID for updating quiz count.");
        }
    }

    private void generateQuiz() {
        if (currentInterestIndex >= userInterests.size()) {
            currentInterestIndex = 0;
        }
        String interest = userInterests.get(currentInterestIndex++);
        buttonLlamaAPI(interest);
    }

    public void buttonLlamaAPI(String topic) {
        String url = "http://10.0.2.2:5000/getQuiz?topic=" + Uri.encode(topic);
        Log.d("API Call", "Request URL: " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> handleResponse(response, topic), this::handleError);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
    }

    private void handleResponse(JSONObject response, String topic) {
        try {
            Quiz newQuiz = processApiResponse(response, topic);
            if (newQuiz != null) {
                DatabaseHelper db = new DatabaseHelper(this);
                SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
                String username = sharedPreferences.getString("Username", null);
                int userId = db.getUserId(username);

                long quizId = db.insertQuiz(userId, newQuiz.getQuizName(), topic, newQuiz.getQuestions());
                if (quizId == -1) {
                    Toast.makeText(this, "Failed to insert quiz into the database.", Toast.LENGTH_LONG).show();
                } else {
                    newQuiz.setQuizId((int) quizId);
                    quizzes.add(newQuiz);
                    adapter.notifyItemInserted(quizzes.size() - 1);
                }
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Error parsing quiz data.", Toast.LENGTH_SHORT).show();
            Log.e("ProfileActivity", "JSON parsing error: " + e.getMessage());
        }
    }

    private void handleError(VolleyError error) {
        if (error.networkResponse != null) {
            Log.e("API Error", "Status Code: " + error.networkResponse.statusCode);
            String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
            Log.e("API Error", "Response Body: " + responseBody);
        }
        if (error.getMessage() != null) {
            Log.e("API Error", error.getMessage());
        }
        Toast.makeText(getApplicationContext(), "Failed to fetch quiz. Please try again.", Toast.LENGTH_SHORT).show();
    }


    private Quiz processApiResponse(JSONObject response, String topic) throws JSONException {
        JSONArray quizzesArray = response.getJSONArray("quiz");
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < quizzesArray.length(); i++) {
            JSONObject questionObject = quizzesArray.getJSONObject(i);
            String questionText = questionObject.getString("question");
            JSONArray optionsArray = questionObject.getJSONArray("options");
            List<String> options = new ArrayList<>();
            for (int j = 0; j < optionsArray.length(); j++) {
                options.add(optionsArray.getString(j).trim());
            }
            String correctAnswerLetter = questionObject.getString("correct_answer").trim();
            int correctAnswerIndex = correctAnswerLetter.charAt(0) - 'A';
            if (correctAnswerIndex < 0 || correctAnswerIndex >= options.size()) {
                Log.e("API Error", "Correct answer index out of bounds: " + correctAnswerIndex);
                continue;
            }
            questions.add(new Question(questionText, options, correctAnswerIndex));
        }
        if (questions.isEmpty()) {
            Log.e("ProfileActivity", "No questions parsed from API response");
            return null;
        }
        return new Quiz(-1, "Quiz on " + topic, "A quiz generated by the API on the topic of " + topic, questions, false);
    }

    @Override
    public void onQuizClick(int position) {
        Log.d("ProfileActivity", "Quiz clicked at position: " + position);

        Quiz clickedQuiz = quizzes.get(position);
        if (clickedQuiz != null && clickedQuiz.getQuestions() != null && !clickedQuiz.getQuestions().isEmpty()) {
            Gson gson = new Gson();
            String serializedQuiz = gson.toJson(clickedQuiz);

            Intent intent = new Intent(ProfileActivity.this, QuizActivity.class);
            intent.putExtra("quiz_data", serializedQuiz);
            intent.putExtra("quizId", clickedQuiz.getQuizId());
            intent.putExtra("userName", getUsername());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Quiz data is incomplete.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();  // Refresh the data to reflect any changes
    }

    public String getUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Username", null);
    }
}