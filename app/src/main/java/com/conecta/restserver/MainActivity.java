package com.conecta.restserver;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    Map<String, String> postData = new HashMap<>();
    Button pullPosButton;
    Button callAlertActivity;
    Button stopService, startService, checkService;
    Switch switchGiro, switchGps, switchTransmit;

   AlertIntentService mService;
    boolean mBound = false;

    List<TrackerPos> trackerPosList = new ArrayList<>();
    TrackerAdapter adapterTracker;
    String transmit;

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
                            Toast.makeText(MainActivity.this, "Obtida lista de posições", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MainActivity.this, "Giro: " + statusGiro.get(0).toString(), Toast.LENGTH_SHORT).show();
                    break;
                case CONFIG_GPS_PULL:
                    List<String> statusGps = (List<String>) object;
                    switchGps.setChecked(statusGps.get(0).toString().equals("on") ? true : false);
                    Toast.makeText(MainActivity.this, "Giro: " + statusGps.get(0).toString(), Toast.LENGTH_SHORT).show();
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
    protected void onStop() {
        super.onStop();
        // Unbind from the service
       // if (mBound) {
       //     unbindService(mConnection);
       //     mBound = false;
       // }
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
        stopService = findViewById(R.id.stopServiceButton);
        checkService = findViewById(R.id.checkService);
        Button deleteButton = findViewById(R.id.deleteButton);

        switchGiro = findViewById(R.id.giroAlertSwitsh);
        switchGps = findViewById(R.id.switchGps);
        switchTransmit = findViewById(R.id.switchTransmitter);
        switchGiro.setChecked(true);

        switchTransmit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (mService.stopTimer() == null) Log.e(TAG, "Timer null.");
                transmit = switchTransmit.isChecked() ? "transmiter" : "receptor";
                    if (mBound) {
                        Toast.makeText(MainActivity.this,
                                mService.startTimer(Long.parseLong(timerInterval.getText().toString()), transmit,
                                        gpsDist.getText().toString(),
                                        gpsTime.getText().toString(),
                                        giroSense.getText().toString()),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        String message = "Não foi possível iniciar o serviço.";
                        Toast.makeText(MainActivity.this,
                                message,Toast.LENGTH_SHORT).show();
                        Log.e(TAG, message);
                    }


            }
        });

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






        checkService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e(TAG, "Check Service");
                isMyServiceRunning(AlertIntentService.class);
            }
        });


        stopService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e(TAG, "Stop Service");
                mService.stopTimer();
               // stopService(new Intent(MainActivity.this, AlertIntentService.class));

            }
        });

        startService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e(TAG, "Start Service");

                if (mBound) {
                   transmit = switchTransmit.isChecked() ? "transmiter" : "";
                   Toast.makeText(MainActivity.this,
                           mService.startTimer(Long.parseLong(timerInterval.getText().toString()), transmit,
                           gpsDist.getText().toString(),
                           gpsTime.getText().toString(),
                           giroSense.getText().toString()),
                           Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Não foi possível iniciar o serviço.",Toast.LENGTH_SHORT).show();
                }
                /*
                Intent serviceIntent = new Intent(MainActivity.this, AlertIntentService.class);
                serviceIntent.putExtra("gpsDist", gpsDist.getText().toString());
                serviceIntent.putExtra("gpsTime", gpsTime.getText().toString());
                serviceIntent.putExtra("giroSense", giroSense.getText().toString());

                //serviceIntent.putExtra("transmiter", transmit);
                startService(serviceIntent);
                */
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
    }

    @Override
    public void completionHandler(Boolean success, RequestType type, Object object) {

    }
}

