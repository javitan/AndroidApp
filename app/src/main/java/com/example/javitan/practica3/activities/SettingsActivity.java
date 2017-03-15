package com.example.javitan.practica3.activities;


import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.javitan.practica3.R;
import com.example.javitan.practica3.task.HttpAsyncTask;


public class SettingsActivity extends AppCompatPreferenceActivity {

    HttpAsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This method is deprecated in favour of the modern Fragment-based activities,
        // but it is perfectly safe to use it
        addPreferencesFromResource(R.xml.preferences_settings);
        findPreference("edit_text_preference_1").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                resetData();
                Toast.makeText(SettingsActivity.this, R.string.game_reset_text, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        findPreference("list_preference_1").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                resetData();
                Toast.makeText(SettingsActivity.this, R.string.game_reset_text, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        //Añadir amigos
        Preference pref = findPreference("edit_text_preference_2");
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (isNetworkConnected()) {
                    task = new HttpAsyncTask();
                    task.setParent(getParent());
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String username = prefs.getString("edit_text_preference_1", "Javitan");
                    task.execute("POST", username, (String) newValue);

                    Toast.makeText(getApplicationContext(), R.string.friend_added, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.noInternet), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    private void resetData(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("score", 0);
        editor.putInt("index", 0);
        editor.putInt("hintUsed", 0);
        editor.apply();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info;
        info = manager.getActiveNetworkInfo();
        return ((info != null) && (info.isConnected()));
    }

    /*
        This method is executed when any action from the ActionBar is selected
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Determine the action to take place according to the Id of the action selected
        switch (item.getItemId()) {

            /*
                Up navigation
                NOTE: This is not required usually, but due to the particular implementation of
                AppCompatPreferenceActivity class, it is necessary to explicitly state the action
                to take when the up navigation option is selected
            */
            case android.R.id.home:
                // Navigate from this activity to its parent (they must be located in the same task)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
