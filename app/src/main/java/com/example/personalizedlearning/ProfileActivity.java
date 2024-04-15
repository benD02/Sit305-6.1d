package com.example.personalizedlearning;

import android.content.Context;
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
    private List<Quiz> quizzes = new ArrayList<>(); // Class-level variable for quizzes
    private QuizAdapter adapter;

    public void onQuizClick(int position) {
        Quiz clickedQuiz = quizzes.get(position);
        Gson gson = new Gson();
        String serializedQuiz = gson.toJson(clickedQuiz);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, QuizFragment.newInstance(serializedQuiz))
                .addToBackStack(null)
                .commit();
    }




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable((this));
        setContentView(R.layout.fragment_profile_activity);
        textView = findViewById(R.id.textView);
        btnGenerate = findViewById(R.id.btnGenerate);
        recyclerView = findViewById(R.id.rv_quiz_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        quizzes = new ArrayList<>(); // This initializes the class-level variable directly
        // Populate your quizzes list here...
        adapter = new QuizAdapter(quizzes, (QuizAdapter.OnQuizListener) this);
        recyclerView.setAdapter(adapter);

        try {
            DatabaseHelper db = new DatabaseHelper(ProfileActivity.this);
            String username = getUsername();


            int userId = db.getUserId(username);
            ArrayList<String> userInterests = db.getUserInterests(userId);

            String interestsString = TextUtils.join(", ", userInterests);
            Log.d("topics", userInterests.toString());

            btnGenerate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ProfileActivity", "Generate button clicked");
                    buttonLlamaAPI(v, interestsString);
                }
            });


        } catch (Exception e) {
            Log.e("ProfileActivity", "Error fetching user interests, You may not have set any, remake your account", e);
            Toast.makeText(this, "Error fetching user details. You may not have set any, remake your account.", Toast.LENGTH_LONG).show();


        }
    }


    public String getUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Username", null); // Returns null if "Username" doesn't exist
    }





    public void buttonLlamaAPI(View view, String topic) {
        String url = "http://10.0.2.2:5000/getQuiz?topic=" + Uri.encode(topic);

        Log.d("topics", topic.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Process the response to construct quiz objects
                        Log.d("API response", response.toString());
                        Quiz newQuiz = processApiResponse(response, topic);  // Pass 'topic' here
                        if (newQuiz != null) {
                            updateQuizzes(newQuiz);  // Update your RecyclerView dataset
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error parsing quiz data.", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Log.e("API Error", "Error response from API", error);
            Toast.makeText(getApplicationContext(), "Failed to fetch quiz. Please try again.", Toast.LENGTH_SHORT).show();
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000, // Timeout after 30 seconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));


        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
    }

    private Quiz processApiResponse(JSONObject response, String topic) throws JSONException {
        Gson gson = new Gson();
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
        Quiz quiz = new Quiz("Quiz on " + topic, "A quiz generated by the API on the topic of " + topic);
        quiz.setQuestions(questions);
        return quiz;
    }



    private void updateQuizzes(Quiz newQuiz) {
        runOnUiThread(() -> {
            quizzes.add(newQuiz);
            Log.d("ProfileActivity", "Quiz added: " + newQuiz.getQuizName() + ", Total quizzes now: " + quizzes.size());
            adapter = new QuizAdapter(quizzes, this); // Reinitialize the adapter
            recyclerView.setAdapter(adapter); // Reset the adapter
            Log.d("ProfileActivity", "Adapter fully reset with new data.");
        });
    }








}