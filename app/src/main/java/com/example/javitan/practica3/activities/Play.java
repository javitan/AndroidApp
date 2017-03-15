package com.example.javitan.practica3.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.javitan.practica3.R;
import com.example.javitan.practica3.database.ScoresSqlHelper;
import com.example.javitan.practica3.pojo.Question;
import com.example.javitan.practica3.task.HttpAsyncTask;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Play extends AppCompatActivity {

    TextView tvScoreValue;
    private List<Question> questionsList = new ArrayList<Question>();
    private HttpAsyncTask task;
    private Button button1, button2, button3, button4;
    private TextView tvQuestionText;
    private TextView tvPlaying;
    private Drawable currentColor;
    //private String answer = "";
    private Question question = new Question();
    private int index, score, hintUsed, hintUser = 0;
    private String username;
    private int scoreValues[] = {100, 200, 300, 500, 1000, 2000, 4000, 8000, 16000, 32000, 64000, 125000, 250000, 500000, 1000000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        tvScoreValue = (TextView) findViewById(R.id.tvScore);
        questionsList.addAll(generateQuestionListFromXml());
        button1 = (Button) findViewById(R.id.bAns1);
        button2 = (Button) findViewById(R.id.bAns2);
        button3 = (Button) findViewById(R.id.bAns3);
        button4 = (Button) findViewById(R.id.bAns4);
        currentColor = button2.getBackground();
        tvQuestionText = (TextView) findViewById(R.id.tvQuestionText);
        tvPlaying = (TextView) findViewById(R.id.tvPlaying);
        hintUser = Integer.parseInt(sp.getString("list_preference_1", "3"));
        index = sp.getInt("index", 0);
        hintUsed = sp.getInt("hintUsed", -1);
        if (hintUsed == -1) {
            hintUsed = 0;
        }
        putQuestion(index);
    }

    public void putQuestion(int i) {
        if (i < questionsList.size()) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            question = questionsList.get(i);
            if (score == 1000 || score == 32000){
                tvScoreValue.setTextColor(Color.rgb(239,255,48));
            }
            else{
                tvScoreValue.setTextColor(Color.rgb(255,255,255));
            }
            tvScoreValue.setText(Integer.toString(sp.getInt("score", 0)) + "€");
            button1.setText(question.getAnswer1());
            button2.setText(question.getAnswer2());
            button3.setText(question.getAnswer3());
            button4.setText(question.getAnswer4());
            button1.setBackground(currentColor);
            button2.setBackground(currentColor);
            button3.setBackground(currentColor);
            button4.setBackground(currentColor);
            tvQuestionText.setText(question.getText());
            tvPlaying.setText(getString(R.string.playing_for) + " " + scoreValues[index] + "€");
            enableAnswers();
        } else {
            //Toast.makeText(this, "There aren't more questions!!!", Toast.LENGTH_SHORT).show();
            String message = getResources().getString(R.string.win_message);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.win)
                    .setMessage(message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            exitApp();
                        }
                    }).show();
            //Hacemos PUT de la puntuación
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (isNetworkConnected()) {
                username = prefs.getString("edit_text_preference_1", "");
                task = new HttpAsyncTask();
                task.execute("PUT", username, Integer.toString(score));
            } else {
                Toast.makeText(this, R.string.noInternet, Toast.LENGTH_SHORT).show();
            }
            Intent intent = new Intent(this, MainActivity.class);
            //Añadimos la puntación a Local
            ScoresSqlHelper.getInstance(this).addScore(username, score);
            SharedPreferences.Editor editor = prefs.edit();
            //Guardamos las puntuaciones en la lista de scores con PUT
            editor.putInt("score", 0);
            editor.putInt("index", 0);
            editor.putInt("hintUsed", 0);
            editor.apply();
            startActivity(intent);
        }
    }

    private void enableAnswers() {
        button1.setEnabled(true);
        button2.setEnabled(true);
        button3.setEnabled(true);
        button4.setEnabled(true);
    }

    private void endGame() {
        if (score < 1000) {
            score = 0;
            String message = getResources().getString(R.string.lose_message);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.gameover)
                    .setMessage(message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            exitApp();
                        }
                    }).show();
        } else if (score >= 1000 && score <= 32000) {
            if (score >= 1000 && score < 32000) {
                score = 1000;
            } else if (score >= 32000) {
                score = 32000;
            }
            String message = getResources().getString(R.string.win_min_message) + " " + score + "€";
            new AlertDialog.Builder(this)
                    .setTitle(R.string.gameover)
                    .setMessage(message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            exitApp();
                        }
                    }).show();
        }
    }

    public void checkAnswer(String answer) {
        if (answer.equals(question.getRight())) {
            //Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            score = scoreValues[index];
            index++;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("score", score);
            editor.putInt("index", index);
            editor.apply();
            putQuestion(index);
        } else {
            //Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show();
            //Ponemos el botón de rojo indicando que la respuesta es incorrecta
            changeButtonWrong(answer);
            endGame();
        }
    }

    private void changeButtonWrong(String answer) {
        switch (answer) {
            case "1":
                button1.setBackgroundColor(Color.rgb(206, 31, 31));
                break;
            case "2":
                button2.setBackgroundColor(Color.rgb(206, 31, 31));
                break;
            case "3":
                button3.setBackgroundColor(Color.rgb(206, 31, 31));
                break;
            case "4":
                button4.setBackgroundColor(Color.rgb(206, 31, 31));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_play, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void changeButtons(String answer) {
        switch (answer) {
            case ("1"):
                currentColor = button1.getBackground();
                button1.setBackgroundColor(Color.rgb(255, 212, 36));
                break;
            case ("2"):
                currentColor = button2.getBackground();
                button2.setBackgroundColor(Color.rgb(255, 212, 36));
                break;
            case ("3"):
                currentColor = button3.getBackground();
                button3.setBackgroundColor(Color.rgb(255, 212, 36));
                break;
            case ("4"):
                currentColor = button4.getBackground();
                button4.setBackgroundColor(Color.rgb(255, 212, 36));
                break;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info;
        info = manager.getActiveNetworkInfo();
        return ((info != null) && (info.isConnected()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        switch (item.getItemId()) {
            case (R.id.callOption):
                if (hintUsed >= hintUser) {
                    Toast.makeText(this, R.string.cant_use_more_hint, Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    hintUsed++;
                    editor.putInt("hintUsed", hintUsed);
                    editor.apply();
                }
                changeButtons(question.getPhone());
                item.setEnabled(false);
                break;
            case (R.id.fiftyOption):
                if (hintUsed >= hintUser) {
                    Toast.makeText(this, R.string.cant_use_more_hint, Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    hintUsed++;
                    editor.putInt("hintUsed", hintUsed);
                    editor.apply();
                }
                disableButtons(question.getFifty1());
                disableButtons(question.getFifty2());
                item.setEnabled(false);
                break;
            case (R.id.publicOption):
                if (hintUsed >= hintUser) {
                    Toast.makeText(this, R.string.cant_use_more_hint, Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    hintUsed++;
                    editor.putInt("hintUsed", hintUsed);
                    editor.apply();
                }
                changeButtons(question.getAudience());
                item.setEnabled(false);
                break;
            case (R.id.endOption):
                String message = getResources().getString(R.string.exit_message);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.exit)
                        .setMessage(message)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                endGame();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exitApp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //Guardamos las puntuaciones en la lista de scores con PUT
        Resources res = getResources();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        username = prefs.getString("edit_text_preference_1", "");
        if (isNetworkConnected()) {
            task = new HttpAsyncTask();
            task.execute("PUT", username, Integer.toString(score));
            ScoresSqlHelper.getInstance(this).addScore(username, score);
        } else {
            Toast.makeText(this, R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
        //Ponemos las variables a 0 para la siguiente vez que se inicie la partida
        resetData();
        //Volvemos al menú principal de la app
        startActivity(intent);
    }

    private void resetData() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("score", 0);
        editor.putInt("index", 0);
        editor.putInt("hintUsed", 0);
        editor.apply();
    }

    private void disableButtons(String fifty) {
        switch (fifty) {
            case ("1"):
                button1.setEnabled(false);
                currentColor = button1.getBackground();
                button1.setBackgroundColor(Color.rgb(165, 165, 165));
                break;
            case ("2"):
                button2.setEnabled(false);
                currentColor = button2.getBackground();
                button2.setBackgroundColor(Color.rgb(165, 165, 165));
                break;
            case ("3"):
                button3.setEnabled(false);
                currentColor = button3.getBackground();
                button3.setBackgroundColor(Color.rgb(165, 165, 165));
                break;
            case ("4"):
                button4.setEnabled(false);
                currentColor = button4.getBackground();
                button4.setBackgroundColor(Color.rgb(165, 165, 165));
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onPressButton(View v) {
        String answer = "";
        switch (v.getId()) {
            case (R.id.bAns1):
                answer = "1";
                break;
            case (R.id.bAns2):
                answer = "2";
                break;
            case (R.id.bAns3):
                answer = "3";
                break;
            case (R.id.bAns4):
                answer = "4";
                break;
        }
        checkAnswer(answer);
    }

    public List<Question> generateQuestionListFromXml() {
        List<Question> list = new ArrayList<>();
        try {
            XmlResourceParser xmlres = getResources().getXml(R.xml.questions);
            int eventType = xmlres.getEventType();
            String answer1 = "", answer2 = "", answer3 = "", answer4 = "", audience = "", fifty1 = "", fifty2 = "", number = "", phone = "", right = "", text = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    System.out.println("Start document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    if (xmlres.getName().equals("question")) {
                        answer1 = xmlres.getAttributeValue(null, "answer1");
                        answer2 = xmlres.getAttributeValue(null, "answer2");
                        answer3 = xmlres.getAttributeValue(null, "answer3");
                        answer4 = xmlres.getAttributeValue(null, "answer4");
                        audience = xmlres.getAttributeValue(null, "audience");
                        fifty1 = xmlres.getAttributeValue(null, "fifty1");
                        fifty2 = xmlres.getAttributeValue(null, "fifty2");
                        number = xmlres.getAttributeValue(null, "number");
                        phone = xmlres.getAttributeValue(null, "phone");
                        right = xmlres.getAttributeValue(null, "right");
                        text = xmlres.getAttributeValue(null, "text");
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xmlres.getName().equals("question")) {
                        Question q = new Question(number, text, answer1, answer2, answer3, answer4, right, audience, phone, fifty1, fifty2);
                        list.add(q);
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                }
                eventType = xmlres.next();
            }
            xmlres.close();
        } catch (XmlPullParserException e) {

        } catch (IOException e) {

        }

        return list;
    }
}
