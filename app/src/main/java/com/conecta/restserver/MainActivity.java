package com.conecta.restserver;

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
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    String TAG = "RestClient";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button restGetButton = (Button) findViewById(R.id.restGetButton);
        final EditText restUrlText = (EditText) findViewById(R.id.restUrlText);
        restUrlText.setText("https://api.github.com/");

        Log.d(TAG, "onCreate " );

        restGetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {

                        Log.d(TAG, "GET " );
                        URL githubEndpoint = null;
                        try {
                            githubEndpoint = new URL(restUrlText.getText().toString());
                            //githubEndpoint = new URL("http://localhost:9090/colicon/buscacontrato/3");
                        } catch (MalformedURLException e) {
                            Log.d(TAG, "eRRO" );
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        final TextView restRestultText = (TextView) findViewById(R.id.restResultText);
                        // Create connection
                        try {

                            HttpsURLConnection myConnection =
                                    (HttpsURLConnection) githubEndpoint.openConnection();
                            myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                            if (myConnection.getResponseCode() == 200) {
                                Log.d(TAG, "CODE 200" );

                                InputStream responseBody = myConnection.getInputStream();
                                InputStreamReader responseBodyReader =
                                        new InputStreamReader(responseBody, "UTF-8");
                                JsonReader jsonReader = new JsonReader(responseBodyReader);
                                jsonReader.beginObject(); // Start processing the JSON object
                               // StringBuilder value = null;


                                while (jsonReader.hasNext()) { // Loop through all keys
                                    String key = jsonReader.nextName(); // Fetch the next key
                                    // if (key.equals("organization_url")) { // Check if desired key
                                    // Fetch the value as a String
                                    final String value  = jsonReader.nextString();
                                   // restRestultText.setText(jsonReader.nextString().toString());
                                    Log.d(TAG, "Value: " + value);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            restRestultText.setText(restRestultText.getText() + value.toString());
                                        }
                                    });

                                }
                                //restRestultText.setText(value);
                                //restRestultText.setText("fff");
                                jsonReader.close();

                                myConnection.disconnect();



                            } else {
                                Log.d(TAG, "codigo diferente de 200: " );
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


}
