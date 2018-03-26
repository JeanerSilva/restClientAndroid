package com.conecta.restserver;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class AlertActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String TAG = "RestAlerta";
    String baseURL = "";
    private String publishAlertString = "";
    private String pullAlertString = "";
    List<String> alertEntities = new ArrayList<>();
    Button pullAlertButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            baseURL = b.getString("baseURL");
            publishAlertString = b.getString("publishAlertString");
            pullAlertString = b.getString("pullAlertString");
            alertEntities = b.getStringArrayList("alertEntities");

        } else {
            Log.d(TAG, "Não foi possível obter os dados do intent");
        }

        pullAlertButton = findViewById(R.id.atualizaAlerta);

        final Spinner spinnerAlert = findViewById(R.id.spinnerAlert);

        ArrayAdapter<String> spinnerAlertAdapter =
                new ArrayAdapter<String> (AlertActivity.this, android.R.layout.simple_spinner_item, alertEntities);
        spinnerAlertAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerAlert.setAdapter(spinnerAlertAdapter);
        spinnerAlert.setOnItemSelectedListener(this);
        final ListView listaAlerta = findViewById(R.id.listaAlerta);
        final List<Alert> alertaList = new ArrayList<>();

        final AlertAdapter adapterAlert = new AlertAdapter(this,
                R.layout.activity_layout_list_alert, alertaList);
        listaAlerta.setAdapter(adapterAlert);


        listaAlerta.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Pos i: " + id);
            }
        });

        pullAlertButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "pullPosButton ");
                        URL url = null;
                        try {
                            url = new URL(baseURL + pullAlertString + "?entity=" + spinnerAlert.getSelectedItem().toString() );
                        } catch (final MalformedURLException e) {
                            Log.d(TAG, "eRRO");
                            Log.d(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                        // Create connection
                        try {
                            Log.d(TAG, url.toString());
                            HttpsURLConnection myConnection =
                                    (HttpsURLConnection) url.openConnection();
                            myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                            myConnection.setRequestMethod("GET");
                            Log.d(TAG, "\nSending request to URL : " + myConnection);

                            myConnection.addRequestProperty("entity", "moto");
                            if (myConnection.getResponseCode() == 200) {
                                Log.d(TAG, "CODE 200");
                                String lista = "";
                                alertaList.clear();
                                InputStream responseBody = myConnection.getInputStream();
                                InputStreamReader responseBodyReader =
                                        new InputStreamReader(responseBody, "UTF-8");
                                JsonReader jsonReader = new JsonReader(responseBodyReader);

                                Alert alert = new Alert("Pos", "Mov", "Giro", "Time");
                                alertaList.add(alert);
                                jsonReader.beginArray();
                                while (jsonReader.hasNext()) {
                                    alert = readerAlert(jsonReader);
                                    alertaList.add(alert);
                                    //lista = lista + alert.toString();
                                }
                                //final String listacompleta = lista;
                                jsonReader.endArray();
                                jsonReader.close();
                                myConnection.disconnect();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapterAlert.notifyDataSetChanged();
                                    }
                                });
                            } else {
                                Log.d(TAG, "codigo diferente de 200: ");
                            }

                        } catch (final IOException e) {
                            Log.d(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

    }
    public Alert readerAlert (JsonReader reader) throws IOException {
        String pos = null;
        String giro = null;
        String mov = null;
        String time = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("pos")) {
                pos = reader.nextString();
            } else if (name.equals("giro")) {
                giro = reader.nextString();
            } else if (name.equals("mov")) {
                mov = reader.nextString();
            } else if (name.equals("time")) {
                time = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Alert(pos, giro, mov, time);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        pullAlertButton.performClick();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
