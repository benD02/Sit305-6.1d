package com.example.personalizedlearning;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class InterestActivity extends AppCompatActivity {

    private ListView listViewInterests;
    private Button buttonComplete;
    private ArrayList<String> selectedInterests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_interest_activity);

        listViewInterests = findViewById(R.id.listViewInterests);
        buttonComplete = findViewById(R.id.buttonComplete);

        // Sample interests, replace with your own
        String[] interests = getResources().getStringArray(R.array.interests_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, interests);
        listViewInterests.setAdapter(adapter);

        buttonComplete.setOnClickListener(v -> saveInterests());
    }

    private void saveInterests() {
        // Get selected interests
        selectedInterests.clear();
        for (int i = 0; i < listViewInterests.getCount(); i++) {
            if (listViewInterests.isItemChecked(i)) {
                selectedInterests.add((String) listViewInterests.getItemAtPosition(i));
            }
        }

        if (selectedInterests.isEmpty()) {
            Toast.makeText(this, "No interests selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = getUsername();
        if (username == null) {
            Toast.makeText(this, "Username not found. Please login again.", Toast.LENGTH_LONG).show();
            return; // Handle case where there is no username
        }

        DatabaseHelper db = new DatabaseHelper(InterestActivity.this);
        int userId = db.getUserId(username);

        if (userId == -1) {
            Toast.makeText(this, "User not found.", Toast.LENGTH_LONG).show();
            return; // Handle case where user is not found
        }

        db.addUserInterests(userId, selectedInterests);

        Toast.makeText(this, "Interests saved!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(InterestActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    public String getUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("Username", null);
        Log.d("InterestActivity", "Retrieved Username: " + username);
        return username;
    }

}
