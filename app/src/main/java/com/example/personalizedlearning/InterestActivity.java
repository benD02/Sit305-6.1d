package com.example.personalizedlearning;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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
        DatabaseHelper db = new DatabaseHelper(InterestActivity.this);
        String username = getUsername();
        int userId = db.getUserId(username);

        // Save to database (you'll need to implement this method)
        DatabaseHelper databaseHelper = new DatabaseHelper(InterestActivity.this);
        databaseHelper.addUserInterests(userId, selectedInterests); // Method to be implemented

        Toast.makeText(this, "Interests saved!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(InterestActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
    public String getUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Username", null); // Returns null if "Username" doesn't exist
    }
}
