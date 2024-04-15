package com.example.personalizedlearning;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_TABLE_INTERESTS);
        // This should correctly create the user_interests table linking users and interests
        db.execSQL("CREATE TABLE " + TABLE_USER_INTERESTS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID_FK + " INTEGER NOT NULL,"
                + COLUMN_INTEREST_ID_FK + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "FOREIGN KEY(" + COLUMN_INTEREST_ID_FK + ") REFERENCES " + TABLE_INTERESTS + "(" + COLUMN_INTEREST_ID + ")"
                + ")");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_INTERESTS_TABLE);
            db.execSQL(CREATE_TABLE_INTERESTS); // Ensure this is correctly creating the interests table

        }
    }



    public ArrayList<String> getUserInterests(int userId) {
        ArrayList<String> interests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String QUERY = "SELECT " + TABLE_INTERESTS + "." + COLUMN_INTEREST_NAME +
                " FROM " + TABLE_USER_INTERESTS +
                " JOIN " + TABLE_INTERESTS +
                " ON " + TABLE_USER_INTERESTS + "." + COLUMN_INTEREST_ID_FK + "=" + TABLE_INTERESTS + "." + COLUMN_INTEREST_ID +
                " WHERE " + TABLE_USER_INTERESTS + "." + COLUMN_USER_ID_FK + "=?";

        Cursor cursor = db.rawQuery(QUERY, new String[]{String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String interest = cursor.getString(cursor.getColumnIndex(COLUMN_INTEREST_NAME));
                    interests.add(interest);
                    Log.d("DatabaseHelper", "Fetched interest: " + interest);
                } while (cursor.moveToNext());
                Log.d("DatabaseHelper", "Total interests fetched: " + interests.size());
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
                }

                // Check if the user-interest link already exists to avoid duplicate entries
                if (!isUserInterestLinkExists(db, userId, interestId)) {
                    // Link user to the interest in the user_interests table
                    ContentValues userInterestValues = new ContentValues();
                    userInterestValues.put(COLUMN_USER_ID_FK, userId);
                    userInterestValues.put(COLUMN_INTEREST_ID_FK, interestId);
                    db.insert(TABLE_USER_INTERESTS, null, userInterestValues);
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
        Cursor cursor = db.query(TABLE_USER_INTERESTS, new String[]{"id"},
                COLUMN_USER_ID_FK + "=? AND " + COLUMN_INTEREST_ID_FK + "=?", new String[]{String.valueOf(userId), String.valueOf(interestId)},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
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





}
