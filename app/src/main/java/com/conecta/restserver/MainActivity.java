package com.conecta.restserver;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    String TAG = "RestClient";
    private Button g;
    private TextView t;
    private TextView acell, acelldisp;
    private EditText dist;
    private LocationManager locationManager;
    private LocationListener listener;
    private final String baseURL = "https://coliconwg.appspot.com/";
    private final String entity = "moto";
    private final String deletePosString = "deletepos";
    private final String publishPosString = "publish";
    private final String pullPosString = "pull";
    private final String publishAlertString = "alertpublish";
    private final String pullAlertString = "alertpull";
    String posListener = "";


    private Sensor acellSensor;
    private SensorManager SM;
    Float x = 0.0f;
    Float y = 0.0f;
    Float z = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "onCreate ");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        t = findViewById(R.id.textView);
        acell = findViewById(R.id.acell);
        acelldisp = findViewById(R.id.acelldisp);
        g = findViewById(R.id.button);
        dist = findViewById(R.id.dist);

        SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        acellSensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(this, acellSensor, SensorManager.SENSOR_DELAY_NORMAL);

        final Button pullAlertButton = findViewById(R.id.alertButton);
        final Button pullPosButton = findViewById(R.id.pullPosButton);
        Button sendDataButton = findViewById(R.id.sendDataButton);
        Button deleteButton = findViewById(R.id.deleteButton);

        final ListView trackerList = findViewById(R.id.lista);
        final List<TrackerPos> trackerPosList = new ArrayList<>();
        final ArrayAdapter<TrackerPos> adapter = new ArrayAdapter<TrackerPos>(this,
                android.R.layout.simple_list_item_1, trackerPosList);
        trackerList.setAdapter(adapter);
        trackerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "i: " + i);
                TrackerPos selectedTrack = (TrackerPos) trackerList.getAdapter().getItem(i);
                String pos = selectedTrack.getPos();
                Log.e(TAG, "pos: " + pos);
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                String position[] = pos.split(",");
                Bundle b = new Bundle();
                b.putDouble("latitude", Double.parseDouble(position[0]));
                b.putDouble("longitude", Double.parseDouble(position[1]));
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        final ListView listaAlerta = findViewById(R.id.listaAlerta);
        final List<Alert> alertaList = new ArrayList<>();
        final ArrayAdapter<Alert> adapterAlert = new ArrayAdapter<Alert>(this,
                android.R.layout.simple_list_item_1, alertaList);
        listaAlerta.setAdapter(adapterAlert);
        listaAlerta.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "Pos i: " + id);
            }
        });


        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Delete");
                        URL url = null;
                        try {
                            url = new URL(baseURL + deletePosString + "?entity=" + entity);
                        } catch (final MalformedURLException e) {
                            Log.e(TAG, "eRRO");
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        // Create connection
                        try {
                            Log.e(TAG, url.toString());
                            HttpsURLConnection myConnection =
                                    (HttpsURLConnection) url.openConnection();
                            myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                            myConnection.setRequestMethod("GET");
                            Log.e(TAG, "\nSending request to URL : " + myConnection);

                            if (myConnection.getResponseCode() == 200) {
                                Log.e(TAG, "CODE 200");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pullPosButton.performClick();
                                    }
                                });

                            } else {
                                Log.e(TAG, "codigo diferente de 200: ");
                            }
                        } catch (final IOException e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

        sendDataButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        String pos = "-15.4,-48.2";
                        Log.e(TAG, "SendData - pos:" + pos);
                        URL url = null;
                        try {
                            url = new URL(baseURL + publishPosString + "?pos=" + pos + "&entity=" + entity);
                            Log.e(TAG, "sendDataButton - URL: " + url);
                        } catch (final MalformedURLException e) {
                            Log.e(TAG, "eRRO");
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        try {
                            Log.e(TAG, url.toString());
                            HttpsURLConnection myConnection =
                                    (HttpsURLConnection) url.openConnection();
                            myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                            myConnection.setRequestMethod("GET");
                            Log.e(TAG, "\nSending request to URL : " + myConnection);
                            if (myConnection.getResponseCode() == 200) {
                                Log.e(TAG, "CODE 200");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pullPosButton.performClick();
                                    }
                                });
                            } else {
                                Log.e(TAG, "codigo diferente de 200: ");
                            }
                        } catch (final IOException e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                    }
                });
            }
        });


        pullAlertButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "pullPosButton ");
                        URL url = null;
                        try {
                            url = new URL(baseURL + pullAlertString );
                        } catch (final MalformedURLException e) {
                            Log.e(TAG, "eRRO");
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                        // Create connection
                        try {
                            Log.e(TAG, url.toString());
                            HttpsURLConnection myConnection =
                                    (HttpsURLConnection) url.openConnection();
                            myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                            myConnection.setRequestMethod("GET");
                            Log.e(TAG, "\nSending request to URL : " + myConnection);

                            myConnection.addRequestProperty("entity", "moto");
                            if (myConnection.getResponseCode() == 200) {
                                Log.e(TAG, "CODE 200");
                                String lista = "";
                                alertaList.clear();
                                InputStream responseBody = myConnection.getInputStream();
                                InputStreamReader responseBodyReader =
                                        new InputStreamReader(responseBody, "UTF-8");
                                JsonReader jsonReader = new JsonReader(responseBodyReader);

                                Alert alert;
                                jsonReader.beginArray();
                                while (jsonReader.hasNext()) {
                                    alert = readerAlert(jsonReader);
                                    alertaList.add(alert);
                                    lista = lista + alert.toString();
                                }
                                final String listacompleta = lista;
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
                                Log.e(TAG, "codigo diferente de 200: ");
                            }

                        } catch (final IOException e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

        pullPosButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "pullPosButton ");
                        URL url = null;
                        try {
                            url = new URL(baseURL + pullPosString + "?entity=" + entity);
                        } catch (final MalformedURLException e) {
                            Log.e(TAG, "eRRO");
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                        // Create connection
                        try {
                            Log.e(TAG, url.toString());
                            HttpsURLConnection myConnection =
                                    (HttpsURLConnection) url.openConnection();
                            myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                            myConnection.setRequestMethod("GET");
                            Log.e(TAG, "\nSending request to URL : " + myConnection);

                            myConnection.addRequestProperty("entity", "moto");
                            if (myConnection.getResponseCode() == 200) {
                                Log.e(TAG, "CODE 200");
                                String lista = "";
                                trackerPosList.clear();
                                InputStream responseBody = myConnection.getInputStream();
                                InputStreamReader responseBodyReader =
                                        new InputStreamReader(responseBody, "UTF-8");
                                JsonReader jsonReader = new JsonReader(responseBodyReader);

                                TrackerPos trackerPos;
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
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            } else {
                                Log.e(TAG, "codigo diferente de 200: ");
                            }

                        } catch (final IOException e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                t.setText("\n " + location.getLongitude() + " " + location.getLatitude());
                posListener = location.getLatitude() + "," + location.getLongitude();
                Log.e("LOG", " onLocationChanged - longitude: " + location.getLongitude() + ". latitude: " + location.getLatitude());
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        // final TextView restRestultText = (TextView) findViewById(R.id.restResultText);
                        Log.e(TAG, "SendData");

                        URL url = null;
                        try {
                            url = new URL(baseURL + publishPosString + "?pos=" + posListener + "&entity=" + entity);
                        } catch (final MalformedURLException e) {
                            Log.e(TAG, "eRRO");
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        try {
                            Log.e(TAG, url.toString());
                            HttpsURLConnection myConnection =
                                    (HttpsURLConnection) url.openConnection();
                            myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                            myConnection.setRequestMethod("GET");
                            Log.e(TAG, "\nSending request to URL : " + myConnection);
                            if (myConnection.getResponseCode() == 200) {
                                Log.e(TAG, "CODE 200");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pullPosButton.performClick();
                                    }
                                });
                            } else {
                                Log.e(TAG, "codigo diferente de 200: ");
                            }
                        } catch (final IOException e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                    }
                });
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.e("LOG", "onStatusChanged s:" + s + ". i: " + i);
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.e("LOG", "onProviderEnabled s:" + s);
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.e("LOG", "onProviderDisabled s:" + s);
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        configure_button();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button() {
        // first check for permissions
        Log.e(TAG, "configure_button");
        g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick configure_button");
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) getApplicationContext(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                           11);
                    return;
                }
                locationManager.requestLocationUpdates("gps", 1000, Integer.parseInt(dist.getText().toString()), listener);
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

    public TrackerPos readerTrackerPos(JsonReader reader) throws IOException {
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

    @Override
    public void onSensorChanged(final SensorEvent event) {

        final Float _x = event.values[0];
        final Float _y = event.values[1];
        final Float _z = event.values[2];
        acelldisp.setText("X: " + _x + ", y: " + _y + ", z: " + _z);

        if (x == 0.0f) {
            x = _x;
            y = _y;
            z = _z;
        } else {
            if ((x - _x > 1.0f || y - _y > 1.0f || z - _z > 1.0f)) {
                x = _x;
                y = _y;
                z = _z;
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        /*
                        Location currentBestLocation = null;
                        Log.e(TAG, "bestLocation - latitude " + currentBestLocation.getLatitude()
                                                  + " - longitude " + currentBestLocation.getLongitude());
                        */

                        String pos = "-0.0,0.0";
                        Log.e(TAG, "SendData - pos:" + posListener);
                        URL url = null;
                        try {
                            if (posListener.isEmpty()) posListener = pos;
                            url = new URL(baseURL + publishAlertString + "?pos=" + posListener + "&giro=true&mov=false");
                            Log.e(TAG, "sendDataButton - URL: " + url);
                        } catch (final MalformedURLException e) {
                            Log.e(TAG, "eRRO");
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        try {
                            Log.e(TAG, url.toString());
                            HttpsURLConnection myConnection =
                                    (HttpsURLConnection) url.openConnection();
                            myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                            myConnection.setRequestMethod("GET");
                            Log.e(TAG, "\nSending request to URL : " + myConnection);
                            if (myConnection.getResponseCode() == 200) {
                                Log.e(TAG, "CODE 200");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        acell.setText("X: " + _x + ", y: " + _y + ", z: " + _z);
                                    }
                                });
                            } else {
                                Log.e(TAG, "codigo diferente de 200: ");
                            }
                        } catch (final IOException e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                    }
                });

            } else {
                // Log.e(TAG,"varia;áo menor que 1");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



}
