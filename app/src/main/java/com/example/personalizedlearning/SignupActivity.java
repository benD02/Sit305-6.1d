package com.example.personalizedlearning;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {

    private EditText suFullName, suUsername, suEmail, suConfirmEmail, suPassword, suConfirmPassword;
    private Button buttonConfirm;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to your activity's layout
        setContentView(R.layout.fragment_signup_activity); // Ensure this is the correct layout ID

        // Initialize your views here
        suFullName = findViewById(R.id.suFullName);
        suUsername = findViewById(R.id.suUsername);
        suEmail = findViewById(R.id.suEmail);
        suConfirmEmail = findViewById(R.id.suConfirmEmail);
        suPassword = findViewById(R.id.suPassword);
        suConfirmPassword = findViewById(R.id.suConfirmPassword);
        buttonConfirm = findViewById(R.id.buttonConfirm);

        databaseHelper = new DatabaseHelper(this); // Use 'this' since  in an activity context

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    private void signUp() {
        String fullName = suFullName.getText().toString().trim();
        String username = suUsername.getText().toString().trim();
        String email = suEmail.getText().toString().trim();
        String confirmEmail = suConfirmEmail.getText().toString().trim();
        String password = suPassword.getText().toString().trim();
        String confirmPassword = suConfirmPassword.getText().toString().trim();

        if (!email.equals(confirmEmail)) {
            Toast.makeText(SignupActivity.this, "Emails do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Assuming databaseHelper is already initialized
        long userId = databaseHelper.addUser(fullName, username, email, password);

        if (userId == -1) {
            Toast.makeText(SignupActivity.this, "Signup failed. Please try again.", Toast.LENGTH_LONG).show();
            Log.e("SignupActivity", "Failed to insert new user into the database.");
            return; // Exit if the user could not be added
        }

        // Save the username in SharedPreferences after successful signup
        saveUsername(username);

        Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();

        // Proceed to the InterestActivity
        Intent intent = new Intent(SignupActivity.this, InterestActivity.class);
        startActivity(intent);
    }


    private void saveUsername(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Username", username);
        editor.apply();
        Log.d("SignupActivity", "Username saved in SharedPreferences: " + username);
    }

}
