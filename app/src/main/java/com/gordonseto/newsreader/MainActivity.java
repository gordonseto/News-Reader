package com.gordonseto.newsreader;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    Map<Integer, String> articleURLs = new HashMap<Integer, String>();
    Map<Integer, String> articleTitles = new HashMap<Integer, String>();
    ArrayList<Integer> articleIds = new ArrayList<Integer>();

    SQLiteDatabase articlesDB = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId INTEGER, url VARCHAR, title VARCHAR, content VARCHAR)");

        DownloadTask task = new DownloadTask();
        try {
            String result = task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();
            JSONArray jsonArray = new JSONArray(result);

            for (int i = 0; i < 20; i++) {
                Integer articleId = Integer.valueOf(jsonArray.getString(i));

                DownloadTask getArticle = new DownloadTask();
                String articleInfo = getArticle.execute("https://hacker-news.firebaseio.com/v0/item/" + articleId.toString() + "/.json?print=pretty").get();
                JSONObject jsonObject = new JSONObject(articleInfo);
                String title = jsonObject.getString("title");
                String articleURL = jsonObject.getString("url");

                articleIds.add(articleId);
                articleTitles.put(articleId, title);
                articleURLs.put(articleId, articleURL);

                articlesDB.execSQL("INSERT INTO articles (articleId, url, title) VALUES (" + articleId.toString() + ", '" + articleURL + "', '" + title + "')");
            }

            Log.i("MYAPP", articleIds.toString());
            Log.i("MYAPP", articleTitles.toString());
            Log.i("MYAPP", articleURLs.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class DownloadTask extends AsyncTask<String, Void ,String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while(data != -1){
                    char current = (char)data;
                    result += current;
                    data = reader.read();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }
    }
}
