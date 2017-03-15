package com.example.javitan.practica3.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.javitan.practica3.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void dashboardButtonClicked(View v) {
        Intent intent = null;

        switch (v.getId()) {
            case (R.id.bPlay):
                intent = new Intent(this, Play.class);
                break;
            case (R.id.bScores):
                intent = new Intent(this, Scores.class);
                break;
            case (R.id.bSettings):
                intent = new Intent(this, SettingsActivity.class);
                break;
        }
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //menu.findItem(R.id.actionCredits).setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case (R.id.actionCredits):
                intent = new Intent(this, Credits.class);
                break;
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }
}
