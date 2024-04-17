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
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity implements QuizAdapter.OnQuizListener {
    private TextView textView;
    private String stringToken = "LL-n7IhWNrPdXttzHN2GH3CgfuUaDNMpnHAGqBnc67uPQPPByO0A9VXq4ZskSES8cod";
    private String stringURLEndPoint = "https://api.llama-api.com/chat/completions";

    private Button btnGenerate;
    private RecyclerView recyclerView;
    private List<Quiz> quizzes = new ArrayList<>();
    private QuizAdapter adapter;
    private int currentInterestIndex = 0;
    private ArrayList<String> userInterests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile_activity);
        TextView usernameView = findViewById(R.id.tv_user);
        usernameView.setText(getUsername());
        initView();
        loadData();
    }

    private void initView() {
        textView = findViewById(R.id.textView);
        btnGenerate = findViewById(R.id.btnGenerate);
        recyclerView = findViewById(R.id.rv_quiz_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizAdapter(quizzes, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        DatabaseHelper db = new DatabaseHelper(this);
        String username = getUsername();
        if (username == null) {
            Toast.makeText(this, "User not found, please login again.", Toast.LENGTH_LONG).show();
            return;
        }
        int userId = db.getUserId(username);
        userInterests = db.getUserInterests(userId);
        if (userInterests.isEmpty()) {
            Toast.makeText(this, "No interests defined. Please add some interests.", Toast.LENGTH_LONG).show();
            return;
        }
        btnGenerate.setOnClickListener(v -> generateQuiz());
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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> handleResponse(response, topic), this::handleError);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
    }

    private void handleResponse(JSONObject response, String topic) {
        try {
            Quiz newQuiz = processApiResponse(response, topic);
            if (newQuiz != null) {
                SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
                String username = sharedPreferences.getString("Username", null);
                if (username == null) {
                    Toast.makeText(this, "User not found, please login again.", Toast.LENGTH_LONG).show();
                    return;
                }

                DatabaseHelper db = new DatabaseHelper(this);
                int userId = db.getUserId(username);  // Make sure this method correctly fetches the user ID
                if (userId == -1) {
                    Toast.makeText(this, "Failed to locate user in database.", Toast.LENGTH_LONG).show();
                    return;
                }

                long quizId = db.insertQuiz(userId, newQuiz.getQuizName(), topic, newQuiz.getQuestions());
                if (quizId == -1) {
                    Toast.makeText(this, "Failed to insert quiz into the database.", Toast.LENGTH_LONG).show();
                } else {
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
        Toast.makeText(getApplicationContext(), "Failed to fetch quiz. Please try again.", Toast.LENGTH_SHORT).show();
        Log.e("API Error", "Error response from API", error);
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
        return new Quiz("Quiz on " + topic, "A quiz generated by the API on the topic of " + topic, questions);
    }

    public void onQuizClick(int position) {
        Log.d("ProfileActivity", "Quiz clicked at position: " + position);

        Quiz clickedQuiz = quizzes.get(position);
        if (clickedQuiz != null && clickedQuiz.getQuestions() != null && !clickedQuiz.getQuestions().isEmpty()) {
            // Serialize the Quiz object to pass it via intent
            Gson gson = new Gson();
            String serializedQuiz = gson.toJson(clickedQuiz);
            Log.d("ProfileActivity", "Serialized Quiz Data: " + serializedQuiz);

            // Create an intent and start the QuizActivity
            Intent intent = new Intent(ProfileActivity.this, QuizActivity.class);
            intent.putExtra("quiz_data", serializedQuiz);
            intent.putExtra("userName", getUsername());
            startActivity(intent);
        } else {
            // Log error and show a toast if the quiz has no questions
            Log.e("ProfileActivity", "No questions available in the quiz at position: " + position);
            Toast.makeText(this, "Quiz data is incomplete.", Toast.LENGTH_SHORT).show();
        }
    }


    public String getUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Username", null);
    }
}