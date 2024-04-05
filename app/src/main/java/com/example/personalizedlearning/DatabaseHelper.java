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



    @SuppressLint("Range")
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1; // Default to -1, indicating not found

        String[] projection = {COLUMN_USER_ID};
        String selection = COLUMN_USER_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(TABLE_USERS, // The table to query
                projection,   // The columns to return
                selection,    // The columns for the WHERE clause
                selectionArgs, // The values for the WHERE clause
                null,         // don't group the rows
                null,         // don't filter by row groups
                null);        // The sort order

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
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

        int columnIndex = cursor.getColumnIndex(COLUMN_INTEREST_NAME);
        if(columnIndex != -1) { // Ensures that the column index is valid
            if (cursor.moveToFirst()) {
                do {
                    String interest = cursor.getString(columnIndex);
                    interests.add(interest);
                } while (cursor.moveToNext());
            }
        } else {
            // Handle the case where COLUMN_INTEREST_NAME doesn't exist in your cursor
            Log.e("DatabaseHelper", "Column " + COLUMN_INTEREST_NAME + " does not exist in the cursor.");
        }

        cursor.close();
        db.close();

        return interests;
    }




    public void addUserInterests(int userId, List<String> interests) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (String interestName : interests) {
            long interestId = getInterestId(db, interestName);
            if (interestId == -1) {
                // Interest does not exist, insert it
                ContentValues interestValues = new ContentValues();
                interestValues.put(COLUMN_INTEREST_NAME, interestName);
                interestId = db.insert(TABLE_INTERESTS, null, interestValues);
            }

            // Link user to the interest in the user_interests table
            ContentValues userInterestValues = new ContentValues();
            userInterestValues.put(COLUMN_USER_ID_FK, userId);
            userInterestValues.put(COLUMN_INTEREST_ID_FK, interestId);
            db.insert(TABLE_USER_INTERESTS, null, userInterestValues);
        }

        db.close();
    }


    private long getInterestId(SQLiteDatabase db, String interestName) {
        Cursor cursor = db.query(TABLE_INTERESTS, new String[]{COLUMN_INTEREST_ID},
                COLUMN_INTEREST_NAME + " = ?", new String[]{interestName},
                null, null, null);

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndex(COLUMN_INTEREST_ID));
            cursor.close();
            return id;
        } else {
            cursor.close();
            return -1; // Interest not found
        }
    }

    // Example method to insert user into the database
    public void addUser(String fullName, String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_FULLNAME, fullName);
        values.put(COLUMN_USER_USERNAME, username);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        db.insert(TABLE_USERS, null, values);
        db.close();
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
