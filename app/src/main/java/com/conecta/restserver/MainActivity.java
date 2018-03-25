package com.conecta.restserver;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    String TAG = "RestClient";
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button restGetButton = findViewById(R.id.restGetButton);
        final EditText restUrlText = findViewById(R.id.restUrlText);
        //restUrlText.setText("https://api.github.com/");
        restUrlText.setText("https://coliconwg.appspot.com/pull?entity=moto");


        Log.e(TAG, "onCreate " );

        restGetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        final TextView restRestultText = (TextView) findViewById(R.id.restResultText);
                        Log.e(TAG, "GET " );
                        URL url = null;
                        try {
                            url = new URL(restUrlText.getText().toString());
                           // url = new URL("https://coliconwg.appspot.com/");
                            
                        } catch (final MalformedURLException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    restRestultText.setText( e.getMessage());
                                }
                            });

                            Log.e(TAG, "eRRO" );
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                        // Create connection
                        try {
                            Log.e(TAG, url.toString() );
                            HttpsURLConnection myConnection =
                                    (HttpsURLConnection) url.openConnection();
                            myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                            myConnection.setRequestMethod("GET");
                            System.out.println("\nSending request to URL : " + myConnection);

                            myConnection.addRequestProperty("entity", "moto");
                            if (myConnection.getResponseCode() == 200) {
                                Log.e(TAG, "CODE 200" );
                                String lista = "";
                                InputStream responseBody = myConnection.getInputStream();
                                InputStreamReader responseBodyReader =
                                        new InputStreamReader(responseBody, "UTF-8");
                                JsonReader jsonReader = new JsonReader(responseBodyReader);
                                List<TrackerPos> trackerPosList = new ArrayList<>();
                                TrackerPos trackerPos;
                                jsonReader.beginObject();
                                String key = jsonReader.nextName();
                                Log.e(TAG, "key: " + key);
                                jsonReader.beginArray();
                                while (jsonReader.hasNext()) {
                                    trackerPos = readerTrackerPos(jsonReader);
                                    trackerPosList.add(trackerPos);
                                    lista = lista + trackerPos.toString();
                                }
                                final String listacompleta = lista;
                                jsonReader.endArray();
                                jsonReader.close();
                                myConnection.disconnect();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        restRestultText.setText(restRestultText.getText() + listacompleta);
                                    }
                                });
                           } else {
                                Log.e(TAG, "codigo diferente de 200: " );
                            }

                        } catch (final IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    restRestultText.setText(e.getMessage());
                                }
                            });
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

    }

    public TrackerPos readerTrackerPos (JsonReader reader) throws IOException {
        String pos = null;
        String time = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("pos")) {
                pos = reader.nextString();
            } else if (name.equals("time")) {
                time = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new TrackerPos(pos, time);
    }

}
