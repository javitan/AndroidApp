package com.example.javitan.practica3.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.example.javitan.practica3.R;
import com.example.javitan.practica3.adapters.ScoresAdapter;
import com.example.javitan.practica3.database.ScoresSqlHelper;
import com.example.javitan.practica3.pojo.HighScore;
import com.example.javitan.practica3.pojo.HighScoreList;
import com.example.javitan.practica3.task.HttpAsyncTask;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class Scores extends AppCompatActivity {

    ListView scoresViewLocal;
    ListView scoresViewFriends;
    ScoresAdapter adapter;
    String username;
    Boolean eraseVisible = true;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scores, menu);
        menu.findItem(R.id.actionDeleteScores).setVisible(eraseVisible);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.actionDeleteScores):
                ScoresSqlHelper.getInstance(getApplicationContext()).clearAllScores();
                adapter.notifyDataSetChanged();
                getScores();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        final TabHost tabHostScores = (TabHost) findViewById(R.id.tabHostScore);
        tabHostScores.setup();
        Resources res = getResources();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        username = prefs.getString("edit_text_preference_1", "Javitan");


        TabHost.TabSpec spec = tabHostScores.newTabSpec(getString(R.string.tabLocal));
        spec.setContent(R.id.tabLocal);
        spec.setIndicator(getString(R.string.tab_local));
        tabHostScores.addTab(spec);

        spec = tabHostScores.newTabSpec(getString(R.string.tabFriends));
        spec.setContent(R.id.tabFriends);
        spec.setIndicator(getString(R.string.tab_friends));
        tabHostScores.addTab(spec);

        tabHostScores.setCurrentTab(0);

        scoresViewLocal = (ListView) findViewById(R.id.lvLocal);

        getScores();

        tabHostScores.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                switch (tabHostScores.getCurrentTab()) {
                    case 0:
                        eraseVisible = true;
                        supportInvalidateOptionsMenu();
                        break;
                    case 1:
                        eraseVisible = false;
                        supportInvalidateOptionsMenu();
                        break;
                }
            }
        });
    }

    private void getScores() {
        //Cogemos los Scores locales
        HighScoreList scoresLocal = new HighScoreList();
        scoresLocal.setScores(ScoresSqlHelper.getInstance(this).getScoresLocal());
        adapter = new ScoresAdapter(this, R.layout.score_row, scoresLocal);
        scoresViewLocal.setAdapter(adapter);
        //Hacemos el GET de los Scores de amigos
        if (isNetworkConnected()) {
            HttpAsyncTask task = new HttpAsyncTask();
            task.setParent(this);
            task.execute("GET", username);
        } else {
            Toast.makeText(this, getResources().getString(R.string.noInternet), Toast.LENGTH_SHORT).show();
        }
    }

    public void putHighScoreFriends(HighScoreList list) {
        scoresViewFriends = (ListView) findViewById(R.id.lvFriends);
        adapter = new ScoresAdapter(this, R.layout.score_row, list);
        scoresViewFriends.setAdapter(adapter);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info;
        info = manager.getActiveNetworkInfo();
        return ((info != null) && (info.isConnected()));
    }

}
