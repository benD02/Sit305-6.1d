package com.example.personalizedlearning;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {
    private TextView textView;
    private String stringToken = "LL-n7IhWNrPdXttzHN2GH3CgfuUaDNMpnHAGqBnc67uPQPPByO0A9VXq4ZskSES8cod";
    private String stringURLEndPoint = "https://api.llama-api.com/chat/completions";

    private Button btnGenerate;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable((this));
        setContentView(R.layout.fragment_profile_activity);
        textView = findViewById(R.id.textView);
        btnGenerate = findViewById(R.id.btnGenerate);

        try {
            DatabaseHelper db = new DatabaseHelper(ProfileActivity.this);
            String username = getUsername();

            // Ensure username is not null or empty to avoid further errors
            if(username == null || username.isEmpty()) {
                Toast.makeText(this, "Username not found. Please login again.", Toast.LENGTH_LONG).show();
                // Consider redirecting the user back to the login activity or handling this scenario appropriately
                return; // Stop further execution
            }

            int userId = db.getUserId(username);
            ArrayList<String> userInterests = db.getUserInterests(userId);

            String interestsString = TextUtils.join(", ", userInterests);

            btnGenerate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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


    public void buttonLlamaAPI(View view, String interestsString)
    {
        Log.d("debug ", "Check 1");

       String stringInputText  = "Create a 5-question multiple-choice quiz about the following topics " + interestsString + ". Include questions, four answer choices for each question, and highlight the correct answer.";
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObjectMessage = new JSONObject();
        JSONArray jsonObjectMessageArray = new JSONArray();
        try {
            jsonObjectMessage.put("role", "user");
            jsonObjectMessage.put("content", stringInputText);

            jsonObjectMessageArray.put(0, jsonObjectMessage);
            jsonObject.put("messages", jsonObjectMessageArray);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                stringURLEndPoint,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            String stringOutput = response.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            textView.setText(stringOutput);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("debug ", "Thrown an error");
                textView.setText(error.toString());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mapHeader = new HashMap<>();
                mapHeader.put("Content-Type", "application/json");
                mapHeader.put("Authorization", "Bearer " + stringToken);
                return mapHeader;
            }
        };
        int intTimeoutPeriod = 60000; //60 seconds
        RetryPolicy retryPolicy = new DefaultRetryPolicy(intTimeoutPeriod,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonObjectRequest.setRetryPolicy(retryPolicy);
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
    }







}