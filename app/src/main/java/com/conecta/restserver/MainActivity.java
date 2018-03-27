package com.conecta.restserver;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements SensorEventListener, CustomCallback {

    String TAG = "RestMain";
    private Button g;
    private TextView t;
    private TextView acell, acelldisp;
    private EditText dist, gpsTime;
    private LocationManager locationManager;
    private LocationListener listener;
    private final String baseURL = "https://coliconwg.appspot.com/";

    private final String entity = "moto";
    private final String deletePosString = "posdelete";
    private final String publishPosString = "pospublish";
    private final String pullPosString = "pospull";
    private final String configString = "config";
    private final String entityConfig = "motoconfig";

    ArrayList<String> alertEntities = new ArrayList<>();
    private final String entityAlert = "motoalert";
    private final String publishAlertString = "alertpublish";
    private final String pullAlertString = "alertpull";
    private final String alertDeleteString = "alertdelete";

    Map<String, String> postData = new HashMap<>();
    Button pullPosButton;
    Button callAlertActivity;
    Button sendDataButton;
    Switch switchGiro, switchGps;
    Boolean switchGiroState;

    List<TrackerPos> trackerPosList = new ArrayList<>();
    TrackerAdapter adapterTracker;

    String posListener = "0.0,0.0";
    CustomCallback callback = new CustomCallback() {
        @Override
        public void completionHandler(Boolean success, RequestType type, final Object object) {
            switch (type) {
                case TRAKERPOS_PULL:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            trackerPosList.clear();
                            trackerPosList.addAll((List<TrackerPos>) object);
                            adapterTracker.notifyDataSetChanged();
                        }
                    });
                    break;

                case REFRESH_POS:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "REFRESH");
                            pullPosButton.performClick();
                        }
                    });
                    break;

                case CONFIG_PUBLISH:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "CONFIG_PUBLISH");
                            //pullPosButton.performClick();
                        }
                    });
                    break;

                case REFRESH_ALERT:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "REFRESH");
                            callAlertActivity.performClick();
                        }
                    });
                    break;

                default:
                    break;
            }
        }
    };

    private Sensor acellSensor;
    private SensorManager SM;
    Float x = 0.0f;
    Float y = 0.0f;
    Float z = 0.0f;

    @Override
    public void onResume() {
        super.onResume();
        pullPosButton.performClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate ");

        alertEntities.add("motoalert");
        alertEntities.add("alertcarro");
        alertEntities.add("unknowalert");

        g = findViewById(R.id.button);
        t = findViewById(R.id.textView);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        acell = findViewById(R.id.acell);
        acelldisp = findViewById(R.id.acelldisp);

        dist = findViewById(R.id.dist);
        gpsTime = findViewById(R.id.gpsTimeTxt);

        //SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        //acellSensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //SM.registerListener(this, acellSensor, SensorManager.SENSOR_DELAY_NORMAL);

        callAlertActivity = findViewById(R.id.alertButton);
        pullPosButton = findViewById(R.id.pullPosButton);
        sendDataButton = findViewById(R.id.sendDataButton);
        Button deleteButton = findViewById(R.id.deleteButton);

        switchGiro = findViewById(R.id.giroAlertSwitsh);
        switchGps = findViewById(R.id.switchGps);
        //switchGiro.setChecked(false);

        switchGiro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.e(TAG, "Start Service");
                    startService(new Intent(MainActivity.this, AlertIntentService.class));


                    Log.d(TAG, "Giro ON ");
                    postData.clear();
                    postData.put("entity", entity);
                    HttpPostAsyncTask task =
                            new HttpPostAsyncTask(postData, RequestType.CONFIG_PUBLISH, callback);
                    task.execute(baseURL + configString + "?entity=" + entityConfig + "giro" + "&action=publish&status=on");


                } else {
                    Log.e(TAG, "Stop Service");
                    stopService(new Intent(MainActivity.this, AlertIntentService.class));

                    Log.d(TAG, "Giro OFF ");
                    postData.clear();
                    postData.put("entity", entity);
                    HttpPostAsyncTask task =
                            new HttpPostAsyncTask(postData, RequestType.CONFIG_PUBLISH, callback);
                    task.execute(baseURL + configString + "?entity=" + entityConfig + "giro" + "&action=publish&status=off");
                }
            }
        });

        switchGps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //g.performClick();
                    Log.d(TAG, "GPS ON ");
                    postData.clear();
                    postData.put("entity", entity);
                    HttpPostAsyncTask task =
                            new HttpPostAsyncTask(postData, RequestType.CONFIG_PUBLISH, callback);
                    task.execute(baseURL + configString + "?entity=" + entityConfig + "gps" + "&action=publish&status=on");
                } else {
                    //SM = (SensorManager) getSystemService(SENSOR_SERVICE);
                    //acellSensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

                    Log.d(TAG, "GPS OFF ");
                    postData.clear();
                    postData.put("entity", entity);
                    HttpPostAsyncTask task =
                            new HttpPostAsyncTask(postData, RequestType.CONFIG_PUBLISH, callback);
                    task.execute(baseURL + configString + "?entity=" + entityConfig + "gps" + "&action=publish&status=off");
                }
            }
        });

        final ListView trackerList = findViewById(R.id.lista);


        adapterTracker = new TrackerAdapter(this,
                R.layout.activity_layout_list_tracker, trackerPosList);
        trackerList.setAdapter(adapterTracker);

        //Abre o mapa
        trackerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "i: " + i);
                TrackerPos selectedTrack = (TrackerPos) trackerList.getAdapter().getItem(i);
                String pos = selectedTrack.getPos();
                Log.d(TAG, "pos: " + pos);
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                String position[] = pos.split(",");
                Bundle b = new Bundle();
                b.putDouble("latitude", Double.parseDouble(position[0]));
                b.putDouble("longitude", Double.parseDouble(position[1]));
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                postData.clear();
                postData.put("entity", entity);
                HttpPostAsyncTask task =
                        new HttpPostAsyncTask(postData, RequestType.REFRESH_POS, callback);
                task.execute(baseURL + deletePosString + "?entity=" + entity);
            }
        });

        sendDataButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String provider = LocationManager.GPS_PROVIDER;
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            10);
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            10);
                    return;
                }
                Location location = locationManager.getLastKnownLocation(provider);
                posListener = String.valueOf(location.getLatitude()+ "," + location.getLongitude());
                t.setText(posListener);
                Log.e(TAG, "update posListener" + posListener);
                postData.clear();
                postData.put("entity", entity);
                new HttpPostAsyncTask(postData, RequestType.REFRESH_POS, callback)
                        .execute(baseURL + publishPosString + "?pos=" + posListener + "&entity=" + entity);

            }
        });


        callAlertActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlertActivity.class);
                Bundle b = new Bundle();
                b.putString("baseURL", baseURL);
                b.putString("publishAlertString", publishAlertString);
                b.putString("pullAlertString", pullAlertString);
                b.putString("pullAlertString", pullAlertString);
                b.putStringArrayList("alertEntities", alertEntities);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        pullPosButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "pullPosButton ");
                postData.clear();
                postData.put("entity", entity);
                HttpPostAsyncTask task =
                        new HttpPostAsyncTask(postData, RequestType.TRAKERPOS_PULL, callback);
                task.execute(baseURL + pullPosString + "?entity=" + entity);
            }
        });

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                t.setText(location.getLongitude() + " " + location.getLatitude());
                posListener = location.getLatitude() + "," + location.getLongitude();
                if (switchGps.isChecked()) {
                    Log.d("LOG", " onLocationChanged - longitude: " + location.getLongitude() + ". latitude: " + location.getLatitude());
                    postData.clear();
                    postData.put("entity", entity);
                    HttpPostAsyncTask task =
                            new HttpPostAsyncTask(postData, RequestType.REFRESH_POS, callback);
                    task.execute(baseURL + publishPosString + "?pos=" + posListener + "&entity=" + entity);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d("LOG", "onStatusChanged s:" + s + ". i: " + i);
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d("LOG", "onProviderEnabled s:" + s);
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("LOG", "onProviderDisabled s:" + s);
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
        Log.d(TAG, "configure_button");
        g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick configure_button");
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            10);
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            10);
                    return;
                }
                locationManager.requestLocationUpdates("gps", Integer.parseInt(gpsTime.getText().toString()), Integer.parseInt(dist.getText().toString()), listener);

            }
        });
    }


    @Override
    public void onSensorChanged(final SensorEvent event) {
       // if  (switchGiro.isChecked()) {
       // if  (1 == 2) {
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            acell.setText("X: " + _x + ", y: " + _y + ", z: " + _z);
                        }
                    });
                    sendDataButton.performClick();
                    Log.e(TAG, "Send alerta giro" + posListener);
                    postData.clear();
                    postData.put("entity", entity);
                    HttpPostAsyncTask task =
                            new HttpPostAsyncTask(postData, RequestType.REFRESH_ALERT, callback);
                    task.execute(baseURL + publishAlertString + "?pos=" + posListener + "&giro=true&entity=" + entityAlert);
                } else {
                    // Log.d(TAG,"varia;áo menor que 1");
                }
            }
        //}

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void completionHandler(Boolean success, RequestType type, Object object) {

    }
}

