package com.conecta.restserver;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements CustomCallback {

    String TAG = "RestMain";

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
    private final String alertDeleteString = "alertsdelete";
    TextView timerStatus;

    Map<String, String> postData = new HashMap<>();
    Button pullPosButton;
    Button callAlertActivity;
    Button stopService, startService, checkService;
    Button saveConfig;
    Switch switchGiro, switchGps, switchTransmit, switchWhatsApp;

    Timer timer = new Timer();

   AlertIntentService mService;
    boolean mBound = false;

    List<TrackerPos> trackerPosList = new ArrayList<>();
    TrackerAdapter adapterTracker;
    OperationMode operationMode;
    boolean configReady;

    //String posListener = "0.0,0.0";
    CustomCallback callback = new CustomCallback() {
        @Override
        public void completionHandler(Boolean success, RequestType type, final Object object) {
            switch (type) {
                case TRAKERPOS_PULL:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (object instanceof String) {
                                Toast.makeText(MainActivity.this, object.toString(), Toast.LENGTH_SHORT).show();
                            } else {
                                trackerPosList.clear();
                            trackerPosList.addAll((List<TrackerPos>) object);
                            adapterTracker.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "Obtida lista de posições", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    break;

                case REFRESH_POS:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "REFRESH");
                            Toast.makeText(MainActivity.this, "Posições deletadas.", Toast.LENGTH_SHORT).show();
                            pullPosButton.performClick();
                        }
                    });
                    break;

                case CONFIG_PUBLISH:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "CONFIG_PUBLISH Main");
                            Toast.makeText(MainActivity.this, object.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                case REFRESH_ALERT:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "REFRESH");
                            callAlertActivity.performClick();
                        }
                    });
                    break;
                case CONFIG_PULL:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (object instanceof String) {
                                Toast.makeText(MainActivity.this, object.toString(), Toast.LENGTH_SHORT).show();
                            } else {
                                Config config = (Config) object;
                                switchGiro.setChecked(config.getGiroStatus().toString().equals("on") ? true : false);
                                switchGps.setChecked(config.getGpsStatus().toString().equals("on") ? true : false);
                                switchWhatsApp.setChecked(config.getWhatsApp().toString().equals("whatsapp") ? true : false);
                                gpsTime.setText(config.getGpsTime());
                                gpsDist.setText(config.getGpsDist());
                                giroSense.setText(config.getGiroSense());
                                timerInterval.setText(config.getTimerTransmit());
                                Log.d(TAG, "Config: " + config.toString());
                                Toast.makeText(MainActivity.this, "Obtidas as configurações no servidor.",
                                        Toast.LENGTH_SHORT).show();
                                startService.performClick();
                            }
                        }
                    });
                    configReady = true;
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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Toast.makeText(MainActivity.this, "O serviço está rodando", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        Toast.makeText(MainActivity.this, "O serviço parado", Toast.LENGTH_SHORT).show();
        return false;
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkService.performClick();
        //while (!configReady) {}
        //startService.performClick();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
       }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Toast.makeText(MainActivity.this, "Service connected.", Toast.LENGTH_SHORT).show();
            AlertIntentService.LocalBinder binder = (AlertIntentService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Toast.makeText(MainActivity.this, "Service canceled.", Toast.LENGTH_SHORT).show();
            mBound = false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, AlertIntentService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        timerStatus = findViewById(R.id.timerStatusTxt);

        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate ");
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
        stopService = findViewById(R.id.stopServiceButton);
        saveConfig = findViewById(R.id.saveConfigButton);
        checkService = findViewById(R.id.checkService);
        Button deleteButton = findViewById(R.id.deleteButton);

        switchGiro = findViewById(R.id.giroAlertSwitsh);
        switchGps = findViewById(R.id.switchGps);
        switchWhatsApp = findViewById(R.id.switchWhatsApp);
        switchTransmit = findViewById(R.id.switchTransmitter);
        switchTransmit.setChecked(true);

        final ListView trackerList = findViewById(R.id.lista);

        saveConfig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String gpsStatus = switchGps.isChecked() ? "on" : "off";
                String giroStatus = switchGiro.isChecked() ? "on" : "off";
                String whatsApp = switchWhatsApp.isChecked() ? "whatsapp" : "";
                String url = baseURL + configString + "?entity=" + entityConfig
                        + "&action=publish&girostatus="+ giroStatus.toString() +"&gpsstatus=" + gpsStatus.toString()
                        + "&gpstime=" + gpsTime.getText().toString() + "&gpsdist=" + gpsDist.getText().toString()
                        + "&girosense="+ giroSense.getText().toString()
                        + "&timertransmit=" + timerInterval.getText().toString()
                        + "&whatsapp=" + whatsApp;
                Log.d(TAG, "Save config:" + url);
                new HttpPostAsyncTask(postData, RequestType.CONFIG_PUBLISH, callback).execute(url);
            }
        });

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
                Log.d(TAG, "Deletando posições");
            new HttpPostAsyncTask(postData, RequestType.REFRESH_POS, callback)
                       .execute(baseURL + deletePosString + "?entity=" + entity);
            }
        });


        checkService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Check Service");
                isMyServiceRunning(AlertIntentService.class);
                new HttpPostAsyncTask(postData, RequestType.CONFIG_PULL, callback)
                        .execute(baseURL + configString + "?entity=" + entityConfig + "&action=pull");
            }
        });


        stopService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Stop Service");
                mService.stopTimer();
               // stopService(new Intent(MainActivity.this, AlertIntentService.class));

            }
        });

        startService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Start Service");
                if (mBound) {
                   operationMode = switchTransmit.isChecked() ? OperationMode.TRANSMITER : OperationMode.RECEPTOR;
                    String whatsAppT = switchWhatsApp.isChecked() ? "whatsapp" : "";
                   Toast.makeText(MainActivity.this,
                           mService.startTimer(Long.parseLong(timerInterval.getText().toString()), operationMode,
                                   gpsDist.getText().toString(), gpsTime.getText().toString(), whatsAppT),
                           Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Não foi possível iniciar o serviço.",Toast.LENGTH_SHORT).show();
                }
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
                new HttpPostAsyncTask(postData, RequestType.TRAKERPOS_PULL, callback)
                        .execute(baseURL + pullPosString + "?entity=" + entity);
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

        Log.d(TAG, "Busca Config ");

    }

    @Override
    public void completionHandler(Boolean success, RequestType type, Object object) {

    }
}

