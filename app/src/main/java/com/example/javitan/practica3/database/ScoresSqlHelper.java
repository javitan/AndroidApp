package com.example.javitan.practica3.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.javitan.practica3.pojo.HighScore;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

/**
 * Created by Javitan on 19/02/2017.
 */

public class ScoresSqlHelper extends SQLiteOpenHelper {

    private static ScoresSqlHelper instance;

    public synchronized static ScoresSqlHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ScoresSqlHelper(context.getApplicationContext());
        }
        return instance;
    }

    private ScoresSqlHelper(Context context) {
        super(context, "scores_database", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE ScoresTable " + "(id INTEGER PRIMARY KEY AUTOINCREMENT, user TEXT NOT NULL, score TEXT NOT NULL, latitude TEXT, longitude TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public List<HighScore> getScoresLocal() {
        List<HighScore> result = new ArrayList<>();
        HighScore item;

        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query("ScoresTable", new String[]{"user", "score", "latitude", "longitude"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            item = new HighScore();
            item.setName(cursor.getString(0));
            item.setScoring(cursor.getString(1));
            item.setLatitude(cursor.getString(2));
            item.setLongitude(cursor.getString(3));
            result.add(item);
        }
        cursor.close();
        database.close();
        return result;
    }


    public void clearAllScores() {
        SQLiteDatabase database = getWritableDatabase();
        database.delete("ScoresTable", null, null);
        database.close();
    }

    public void addScore(String name, int value) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user", name);
        values.put("score", value);
        values.put("latitude", "");
        values.put("longitude", "");
        database.insert("ScoresTable", null, values);
        database.close();
    }
}
