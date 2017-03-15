package com.example.javitan.practica3.task;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;

import com.example.javitan.practica3.activities.Scores;
import com.example.javitan.practica3.pojo.HighScoreList;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Javitan on 04/03/2017.
 */

public class HttpAsyncTask extends AsyncTask<String, Void, HighScoreList> {

    private Activity parent = null;

    public void setParent(Activity parent) {
        this.parent = parent;
    }

    @Override
    protected HighScoreList doInBackground(String... params) {
        String httpMethod = params[0];
        String name = "Javitan";
        int i = params.length;
        try {
            name = URLEncoder.encode(params[1], "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String body = "";
        Uri.Builder uriBuilder = new Uri.Builder();

        if (httpMethod.equals("PUT")) {
            uriBuilder.scheme("http");
            uriBuilder.authority("wwtbamandroid.appspot.com");
            uriBuilder.appendPath("rest");
            uriBuilder.appendPath("highscores");
            body = "name=" + name + "&score=" + params[2] + "&format=json";
        } else if (httpMethod.equals("GET")) {
            uriBuilder.scheme("http");
            uriBuilder.authority("wwtbamandroid.appspot.com");
            uriBuilder.appendPath("rest");
            uriBuilder.appendPath("highscores");
            uriBuilder.appendQueryParameter("name", name);
            uriBuilder.appendQueryParameter("format", "json");
        } else if (httpMethod.equals("POST")) {
            uriBuilder.scheme("http");
            uriBuilder.authority("wwtbamandroid.appspot.com");
            uriBuilder.appendPath("rest");
            uriBuilder.appendPath("friends");
            body = "name=" + name + "&friend_name=" + params[2] + "&format=json";
        }

        HighScoreList result = null;
        try {
            URL url = new URL(uriBuilder.build().toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(httpMethod);
            connection.setDoInput(true);
            //Comprobamos qué método se va a hacer
            if (httpMethod.equals("PUT") | httpMethod.equals("POST")) {
                connection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(body);
                writer.flush();
                writer.close();
            }

            //Si sale bien
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                Gson gson = new Gson();
                try {
                    //Convertimos el JSON a objeto POJO de HighScoreList
                    result = gson.fromJson(reader, HighScoreList.class);
                } catch (JsonSyntaxException | JsonIOException e) {
                    e.printStackTrace();
                }
                reader.close();
            }
            connection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(HighScoreList highScoreList) {
        if (highScoreList != null && parent != null) {
            ((Scores) this.parent).putHighScoreFriends(highScoreList);
        }
        //TODO else
        super.onPostExecute(highScoreList);
    }
}
