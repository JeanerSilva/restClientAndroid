package com.conecta.restserver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CustomCallback {

    String TAG = "RestMain";
    private Button startService;
    private EditText gpsDist, gpsTime, timerInterval, giroSense;
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
    Button stopService;
    Switch switchGiro, switchGps;

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
                case CONFIG_GIRO_PULL:
                    List<String> statusGiro = (List<String>) object;
                    switchGiro.setChecked(statusGiro.get(0).toString().equals("on") ? true : false);
                    break;
                case CONFIG_GPS_PULL:
                    List<String> statusGps = (List<String>) object;
                    switchGps.setChecked(statusGps.get(0).toString().equals("on") ? true : false);
                    break;
                default:
                    break;
            }
        }
    };

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

        Log.d(TAG, "Busca GPS Status ");
        new HttpPostAsyncTask(postData, RequestType.CONFIG_GPS_PULL, callback)
                .execute(baseURL + configString + "?entity=" + entityConfig + "gps" + "&action=pull");
        Log.d(TAG, "Busca Giro Status ");
        new HttpPostAsyncTask(postData, RequestType.CONFIG_GIRO_PULL, callback)
                .execute(baseURL + configString + "?entity=" + entityConfig + "giro" + "&action=pull");

        alertEntities.add("motoalert");
        alertEntities.add("alertcarro");
        alertEntities.add("unknowalert");

        gpsDist = findViewById(R.id.dist);
        gpsTime = findViewById(R.id.gpsTimeTxt);
        timerInterval = findViewById(R.id.timerInterval);
        giroSense = findViewById(R.id.giroSenseTxt);

        callAlertActivity = findViewById(R.id.alertButton);
        pullPosButton = findViewById(R.id.pullPosButton);
        stopService = findViewById(R.id.stopServiceButton);
        startService = findViewById(R.id.startServiceButton);
        Button deleteButton = findViewById(R.id.deleteButton);

        switchGiro = findViewById(R.id.giroAlertSwitsh);
        switchGps = findViewById(R.id.switchGps);
        switchGiro.setChecked(true);

        /*
        Log.e(TAG, "Start Service");
        Intent serviceIntent = new Intent(MainActivity.this, AlertIntentService.class);
        serviceIntent.putExtra("timerStatus", "on");
        serviceIntent.putExtra("timerInterval", timerInterval.toString());
        serviceIntent.putExtra("gpsDist", gpsDist.toString());
        serviceIntent.putExtra("gpsTime", gpsTime.toString());
        startService(serviceIntent);
        */

        switchGiro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "Giro ON ");
                    postData.clear();
                    postData.put("entity", entity);
                    new HttpPostAsyncTask(postData, RequestType.CONFIG_PUBLISH, callback)
                            .execute(baseURL + configString + "?entity=" + entityConfig + "giro" + "&action=publish&status=on");
                } else {
                    Log.d(TAG, "Giro OFF ");
                    new HttpPostAsyncTask(postData, RequestType.CONFIG_PUBLISH, callback)
                            .execute(baseURL + configString + "?entity=" + entityConfig + "giro" + "&action=publish&status=off");

                }
            }
        });

        switchGps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //g.performClick();
                    Log.d(TAG, "GPS ON ");
                    new HttpPostAsyncTask(postData, RequestType.CONFIG_PUBLISH, callback)
                            .execute(baseURL + configString + "?entity=" + entityConfig + "gps" + "&action=publish&status=on");
                } else {
                    Log.d(TAG, "GPS OFF ");
                    new HttpPostAsyncTask(postData, RequestType.CONFIG_PUBLISH, callback)
                            .execute(baseURL + configString + "?entity=" + entityConfig + "gps" + "&action=publish&status=off");
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
            new HttpPostAsyncTask(postData, RequestType.REFRESH_POS, callback)
                       .execute(baseURL + deletePosString + "?entity=" + entity);
            }
        });

        stopService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
             Log.e(TAG, "Stop Service");
             stopService(new Intent(MainActivity.this, AlertIntentService.class));
                Log.d(TAG, "Giro OFF ");
                new HttpPostAsyncTask(postData, RequestType.CONFIG_PUBLISH, callback)
                        .execute(baseURL + configString + "?entity=" + entityConfig + "giro" + "&action=publish&status=off");
                Log.d(TAG, "GPS OFF ");
                new HttpPostAsyncTask(postData, RequestType.CONFIG_PUBLISH, callback)
                        .execute(baseURL + configString + "?entity=" + entityConfig + "gps" + "&action=publish&status=off");
                Log.e(TAG, "Start Service");
                Intent serviceIntent = new Intent(MainActivity.this, AlertIntentService.class);
                serviceIntent.putExtra("timerStatus", "off");
                serviceIntent.putExtra("timerInterval", timerInterval.getText().toString());
                serviceIntent.putExtra("gpsDist", gpsDist.getText().toString());
                serviceIntent.putExtra("gpsTime", gpsTime.getText().toString());
                serviceIntent.putExtra("giroSense", giroSense.getText().toString());
                startService(serviceIntent);
            }
        });


        startService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e(TAG, "Start Service");
                Intent serviceIntent = new Intent(MainActivity.this, AlertIntentService.class);
                serviceIntent.putExtra("timerStatus", "on");
                serviceIntent.putExtra("timerInterval", timerInterval.getText().toString());
                serviceIntent.putExtra("gpsDist", gpsDist.getText().toString());
                serviceIntent.putExtra("gpsTime", gpsTime.getText().toString());
                serviceIntent.putExtra("giroSense", giroSense.getText().toString());
                startService(serviceIntent);
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
    }

    @Override
    public void completionHandler(Boolean success, RequestType type, Object object) {

    }
}

