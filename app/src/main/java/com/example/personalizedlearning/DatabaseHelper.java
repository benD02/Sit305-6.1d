package com.example.personalizedlearning;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserDB";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_FULLNAME = "full_name";
    private static final String COLUMN_USER_USERNAME = "username";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PASSWORD = "password";

    private static final String TABLE_INTERESTS = "interests";
    private static final String COLUMN_INTEREST_ID = "interest_id";
    private static final String COLUMN_INTEREST_NAME = "interest_name";

    private static final String TABLE_USER_INTERESTS = "user_interests";
    private static final String COLUMN_USER_ID_FK = "user_id";
    private static final String COLUMN_INTEREST_ID_FK = "interest_id";
    private static final String TABLE_QUIZZES = "quizzes";
    private static final String COLUMN_QUIZ_ID = "quiz_id";
    private static final String COLUMN_QUIZ_NAME = "quiz_name";
    private static final String COLUMN_QUIZ_TOPIC = "quiz_topic";

    private static final String TABLE_QUESTIONS = "questions";
    private static final String COLUMN_QUESTION_ID = "question_id";
    private static final String COLUMN_QUIZ_FOREIGN_ID = "quiz_id";
    private static final String COLUMN_QUESTION_TEXT = "question_text";
    private static final String COLUMN_CORRECT_ANSWER = "correct_answer";
    private static final String COLUMN_OPTIONS = "options";  // This could be a JSON string of options

    private static final String CREATE_QUIZZES_TABLE = "CREATE TABLE " + TABLE_QUIZZES + "("
            + COLUMN_QUIZ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "user_id INTEGER,"  // Associate each quiz with a user
            + COLUMN_QUIZ_NAME + " TEXT,"
            + COLUMN_QUIZ_TOPIC + " TEXT,"
            + "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(user_id)" + ")";

    private static final String CREATE_QUESTIONS_TABLE = "CREATE TABLE " + TABLE_QUESTIONS + "("
            + COLUMN_QUESTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_QUIZ_FOREIGN_ID + " INTEGER,"
            + COLUMN_QUESTION_TEXT + " TEXT,"
            + COLUMN_CORRECT_ANSWER + " TEXT,"
            + COLUMN_OPTIONS + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_QUIZ_FOREIGN_ID + ") REFERENCES " + TABLE_QUIZZES + "(" + COLUMN_QUIZ_ID + "))";


    private static final String CREATE_INTERESTS_TABLE = "CREATE TABLE " + TABLE_USER_INTERESTS + "("
            + COLUMN_USER_ID_FK + " INTEGER,"
            + COLUMN_INTEREST_ID_FK + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
            + "FOREIGN KEY(" + COLUMN_INTEREST_ID_FK + ") REFERENCES " + TABLE_INTERESTS + "(" + COLUMN_INTEREST_ID + ")"
            + ")";


    private static final String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USER_FULLNAME + " TEXT,"
            + COLUMN_USER_USERNAME + " TEXT," + COLUMN_USER_EMAIL + " TEXT,"
            + COLUMN_USER_PASSWORD + " TEXT" + ")";


    private static final String CREATE_TABLE_INTERESTS = "CREATE TABLE " + TABLE_INTERESTS + "("
            + COLUMN_INTEREST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_INTEREST_NAME + " TEXT" + ")";


    public long insertQuiz(int userId, String quizName, String topic, List<Question> questions) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues quizValues = new ContentValues();
        quizValues.put("user_id", userId);
        quizValues.put(COLUMN_QUIZ_NAME, quizName);
        quizValues.put(COLUMN_QUIZ_TOPIC, topic);
        long quizId = db.insert(TABLE_QUIZZES, null, quizValues);

        if (questions != null) {
            for (Question question : questions) {
                ContentValues questionValues = new ContentValues();
                questionValues.put(COLUMN_QUIZ_FOREIGN_ID, quizId);
                questionValues.put(COLUMN_QUESTION_TEXT, question.getQuestionText());
                questionValues.put(COLUMN_CORRECT_ANSWER, question.getCorrectAnswerIndex());
                questionValues.put(COLUMN_OPTIONS, new Gson().toJson(question.getOptions()));
                db.insert(TABLE_QUESTIONS, null, questionValues);
            }
        } else {
            Log.e("DatabaseHelper", "Questions list is null for quiz: " + quizName);
        }
        db.close();
        return quizId;
    }

    public List<Quiz> getAllQuizzes() {
        List<Quiz> quizzes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_QUIZZES, null);
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_QUIZ_ID);
            int nameIndex = cursor.getColumnIndex(COLUMN_QUIZ_NAME);
            int topicIndex = cursor.getColumnIndex(COLUMN_QUIZ_TOPIC);

            if (idIndex == -1 || nameIndex == -1 || topicIndex == -1) {
                Log.e("DatabaseHelper", "One or more column indices not found");
                cursor.close();
                db.close();
                return quizzes; // Return empty or handle error accordingly
            }

            do {
                long quizId = cursor.getLong(idIndex);
                String quizName = cursor.getString(nameIndex);
                String topic = cursor.getString(topicIndex);
                List<Question> questions = getQuestionsForQuiz(quizId);
                quizzes.add(new Quiz(quizName, topic, questions));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return quizzes;
    }

    public List<Question> getQuestionsForQuiz(long quizId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Question> questions = new ArrayList<>();
        String[] projection = {
                COLUMN_QUESTION_ID,
                COLUMN_QUESTION_TEXT,
                COLUMN_OPTIONS,
                COLUMN_CORRECT_ANSWER
        };

        Cursor cursor = db.query(
                TABLE_QUESTIONS,           // The table to query
                projection,                // The columns to return
                COLUMN_QUIZ_FOREIGN_ID + " = ?",  // The columns for the WHERE clause
                new String[] { String.valueOf(quizId) },  // The values for the WHERE clause
                null,                      // group by
                null,                      // having
                null                       // order by
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int questionTextIndex = cursor.getColumnIndex(COLUMN_QUESTION_TEXT);
                int optionsIndex = cursor.getColumnIndex(COLUMN_OPTIONS);
                int correctAnswerIndex = cursor.getColumnIndex(COLUMN_CORRECT_ANSWER);

                if (questionTextIndex != -1 && optionsIndex != -1 && correctAnswerIndex != -1) {
                    String questionText = cursor.getString(questionTextIndex);
                    String optionsJson = cursor.getString(optionsIndex);
                    int correctAnswer = cursor.getInt(correctAnswerIndex);

                    // Convert the JSON string back to a list
                    Type listType = new TypeToken<ArrayList<String>>() {}.getType();
                    List<String> options = new Gson().fromJson(optionsJson, listType);

                    questions.add(new Question(questionText, options, correctAnswer));
                } else {
                    // Log error or throw an exception
                    Log.e("DatabaseHelper", "Column index not found");
                }
            }
            cursor.close();
        }
        db.close();
        return questions;
    }




    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1; // Default to -1, indicating not found

        String[] projection = {COLUMN_USER_ID};
        String selection = COLUMN_USER_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Log.d("DatabaseHelper", "Looking for username: " + username); // Log the username being searched

        Cursor cursor = db.query(TABLE_USERS, projection, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
            Log.d("DatabaseHelper", "User found with ID: " + userId); // Log the found user ID
        } else {
            Log.d("DatabaseHelper", "User not found for username: " + username); // Log if user not found
        }
        cursor.close();
        db.close();

        return userId;
    }




    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Creating database tables...");

        db.execSQL(CREATE_USER_TABLE);
        Log.d("DatabaseHelper", "Users table created.");

        db.execSQL(CREATE_TABLE_INTERESTS);
        Log.d("DatabaseHelper", "Interests table created.");

        db.execSQL(CREATE_QUIZZES_TABLE);
        Log.d("DatabaseHelper", "Quizzes table created.");

        db.execSQL(CREATE_QUESTIONS_TABLE);
        Log.d("DatabaseHelper", "Questions table created.");

        db.execSQL(CREATE_INTERESTS_TABLE);
        Log.d("DatabaseHelper", "User interests link table created.");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZZES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INTERESTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_INTERESTS);
        onCreate(db);
    }




    public ArrayList<String> getUserInterests(int userId) {
        ArrayList<String> interests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String QUERY = "SELECT " + TABLE_INTERESTS + "." + COLUMN_INTEREST_NAME +
                " FROM " + TABLE_USER_INTERESTS +
                " JOIN " + TABLE_INTERESTS +
                " ON " + TABLE_USER_INTERESTS + "." + COLUMN_INTEREST_ID_FK + "=" + TABLE_INTERESTS + "." + COLUMN_INTEREST_ID +
                " WHERE " + TABLE_USER_INTERESTS + "." + COLUMN_USER_ID_FK + "=?";

        Log.d("DatabaseHelper", "Fetching interests with query: " + QUERY);
        Cursor cursor = db.rawQuery(QUERY, new String[]{String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String interest = cursor.getString(cursor.getColumnIndex(COLUMN_INTEREST_NAME));
                    interests.add(interest);
                    Log.d("DatabaseHelper", "Fetched interest: " + interest);
                } while (cursor.moveToNext());
            } else {
                Log.d("DatabaseHelper", "No interests found for user ID: " + userId);
            }
            cursor.close();
        } else {
            Log.e("DatabaseHelper", "Cursor is null. Query failed.");
        }

        db.close();
        return interests;
    }




    public void addUserInterests(int userId, List<String> interests) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();  // Start transaction
        try {
            for (String interestName : interests) {
                long interestId = getInterestId(db, interestName);
                if (interestId == -1) {
                    // Interest does not exist, insert it
                    ContentValues interestValues = new ContentValues();
                    interestValues.put(COLUMN_INTEREST_NAME, interestName);
                    interestId = db.insert(TABLE_INTERESTS, null, interestValues);
                    Log.d("DatabaseHelper", "Inserted new interest: " + interestName + " with ID: " + interestId);
                }

                // Check if the user-interest link already exists to avoid duplicate entries
                if (!isUserInterestLinkExists(db, userId, interestId)) {
                    // Link user to the interest in the user_interests table
                    ContentValues userInterestValues = new ContentValues();
                    userInterestValues.put(COLUMN_USER_ID_FK, userId);
                    userInterestValues.put(COLUMN_INTEREST_ID_FK, interestId);
                    db.insert(TABLE_USER_INTERESTS, null, userInterestValues);
                    Log.d("DatabaseHelper", "Linked user ID: " + userId + " with interest ID: " + interestId);
                }
            }
            db.setTransactionSuccessful();  // Mark the transaction as successful
        } catch (Exception e) {
            // Handle possible errors
            Log.e("DatabaseHelper", "Error adding user interests", e);
        } finally {
            db.endTransaction();  // End transaction
            db.close();
        }
    }


    private boolean isUserInterestLinkExists(SQLiteDatabase db, int userId, long interestId) {
        String query = "SELECT 1 FROM " + TABLE_USER_INTERESTS +
                " WHERE " + COLUMN_USER_ID_FK + "=?" +
                " AND " + COLUMN_INTEREST_ID_FK + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(interestId)});
        boolean exists = cursor.moveToFirst();  // True if cursor is not empty
        cursor.close();
        return exists;
    }


    private long getInterestId(SQLiteDatabase db, String interestName) {
        Cursor cursor = db.query(TABLE_INTERESTS, new String[]{COLUMN_INTEREST_ID},
                COLUMN_INTEREST_NAME + " = ?", new String[]{interestName},
                null, null, null);
        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndex(COLUMN_INTEREST_ID));
        }
        cursor.close();
        return id;
    }


    // Example method to insert user into the database
    public long addUser(String fullName, String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_FULLNAME, fullName);
        values.put(COLUMN_USER_USERNAME, username);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        long userId = db.insert(TABLE_USERS, null, values);
        db.close();
        return userId;  // Return the user ID of the new user or -1 if there was an error
    }


    // Example method to check user for login
    public boolean checkUser(String username, String password) {
        String[] columns = {
                COLUMN_USER_ID
        };
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USER_USERNAME + "=?" + " AND " + COLUMN_USER_PASSWORD + "=?";
        String[] selectionArgs = { username, password };
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        return cursorCount > 0;
    }

    public Quiz getQuizById(int quizId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Quiz quiz = null;

        // Query to get the Quiz details
        Cursor quizCursor = db.query(TABLE_QUIZZES, new String[]{COLUMN_QUIZ_ID, COLUMN_QUIZ_NAME, COLUMN_QUIZ_TOPIC},
                COLUMN_QUIZ_ID + " = ?", new String[]{String.valueOf(quizId)},
                null, null, null);

        if (quizCursor.moveToFirst()) {
            String quizName = quizCursor.getString(quizCursor.getColumnIndex(COLUMN_QUIZ_NAME));
            String quizTopic = quizCursor.getString(quizCursor.getColumnIndex(COLUMN_QUIZ_TOPIC));
            List<Question> questions = getQuestionsForQuiz(quizId);  // Assuming you already have this method implemented

            quiz = new Quiz(quizName, quizTopic, questions);
        }
        quizCursor.close();
        db.close();
        return quiz;
    }






}
