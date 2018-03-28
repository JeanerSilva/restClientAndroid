package com.conecta.restserver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AlertIntentService extends IntentService implements SensorEventListener, CustomCallback, LocationListener {
    private final String entity = "moto";
    private final String publishPosString = "pospublish";
    private final String entityAlert = "motoalert";
    private final String publishAlertString = "alertpublish";
    private final String pullAlertString = "alertpull";
    private final String baseURL = "https://coliconwg.appspot.com/";
    private final String configString = "config";
    private final String entityConfig = "motoconfig";
    private final String pullPosString = "pospull";

    private List<Alert> alertList = new ArrayList<>();
    List<TrackerPos> trackerPosList = new ArrayList<>();
    private int totalAlerts = 0;
    private int totalPos = 0;
    private Sensor acellSensor;
    private SensorManager SM;
    Float x = 0.0f;Float y = 0.0f;Float z = 0.0f;
    String TAG = "Service";
    String posListener;
    private LocationManager locationManager;
    Map<String, String> postData = new HashMap<>();
    private boolean giroService = false;
    private boolean gpsService = false;
    public double latitude;
    public double longitude;
    public Criteria criteria;
    public String bestProvider;
    Timer timer;
    String timerStatus;
    String timerInterval;
    String gpsDist;
    String gpsTime;
    String giroSense;
    String transmit;
    NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;
    String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
    int f=0;

    CustomCallback callback = new CustomCallback() {
        @Override
        public void completionHandler(Boolean success, RequestType type, final Object object) {
            switch (type) {
                case CONFIG_GIRO_PULL:
                    Log.d(TAG, "CONFIG_GIRO_PULL" );
                    List<String> statusGiro = (List<String>) object;
                    String statusGiroString = statusGiro.get(0).toString();
                    Log.e(TAG, "statusGiroString: " + statusGiroString);
                    giroService = statusGiroString.equals("on") ? true : false;
                    Toast.makeText(AlertIntentService.this, "statusGiroString: " + statusGiroString, Toast.LENGTH_SHORT).show();
                    break;
                case CONFIG_GPS_PULL:
                    Log.d(TAG, "CONFIG_GPS_PULL" );
                    List<String> statusGps = (List<String>) object;
                    String statusGpsString = statusGps.get(0).toString();
                    Log.e(TAG, "statusGpsString: " + statusGpsString);
                    gpsService = statusGpsString.equals("on") ? true : false;
                    Toast.makeText(AlertIntentService.this, "statusGpsString: " + statusGpsString, Toast.LENGTH_SHORT).show();
                    break;
                case ALERT_PULL:
                    Log.d(TAG, "ALERT_PULL" );
                    totalAlerts = alertList.size();
                    alertList.clear();
                    alertList.addAll((List<Alert>) object);
                    int newTotal = alertList.size();

                    if (totalAlerts != newTotal) {
                        Log.e(TAG, "Alertas alterou de " + totalAlerts + " para " + newTotal);
                        if (transmit.isEmpty()) {
                            notificationBuilder = new NotificationCompat.Builder(AlertIntentService.this, NOTIFICATION_CHANNEL_ID);

                            notificationBuilder.setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.drawable.ic_launcher_background)
                                    .setTicker("Hearty365")
                                    .setPriority(Notification.PRIORITY_MAX)
                                    .setContentTitle("Alerta")
                                    .setContentText("Alertas alterou de " + totalAlerts + " para " + newTotal)
                                    .setContentInfo("Info");

                            notificationManager.notify(/*notification id*/1, notificationBuilder.build());
                        }
                    }
                    break;

                case TRAKERPOS_PULL:
                    Log.d(TAG, "TRAKERPOS_PULL" );
                    totalPos = trackerPosList.size();
                    trackerPosList.clear();
                    trackerPosList.addAll((List<TrackerPos>) object);
                    int newTotalPos = trackerPosList.size();

                    if (totalPos != newTotalPos) {
                        Log.e(TAG, "Número de movimentações alterou de " + totalPos + " para " + newTotalPos);
                        if (transmit.isEmpty()) {
                            notificationBuilder = new NotificationCompat.Builder(AlertIntentService.this, NOTIFICATION_CHANNEL_ID);

                            notificationBuilder.setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.drawable.ic_launcher_background)
                                    .setTicker("Hearty365")
                                    .setPriority(Notification.PRIORITY_MAX)
                                    .setContentTitle("Alerta")
                                    .setContentText("Número de movimentações alterou de " + totalPos + " para " + newTotalPos)
                                    .setContentInfo("Info");

                            notificationManager.notify(/*notification id*/1, notificationBuilder.build());
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void setaGiroService (){
        Log.d(TAG, "Busca Giro Status ");
        new HttpPostAsyncTask(postData, RequestType.CONFIG_GIRO_PULL, callback)
                .execute(baseURL + configString + "?entity=" + entityConfig + "giro" + "&action=pull");
    }

    public void setaGpsService (){
        Log.d(TAG, "Busca GPS Status ");
        new HttpPostAsyncTask(postData, RequestType.CONFIG_GPS_PULL, callback)
                .execute(baseURL + configString + "?entity=" + entityConfig + "gps" + "&action=pull");
            }

    private void buscaAlertas (){
        Log.d(TAG, "Busca Alertas ");
        new HttpPostAsyncTask(postData, RequestType.ALERT_PULL, callback)
                .execute(baseURL + pullAlertString + "?entity=" + entityAlert);
    }

    private void buscaPos (){
        Log.d(TAG, "Busca Alertas ");
        new HttpPostAsyncTask(postData, RequestType.TRAKERPOS_PULL, callback)
                .execute(baseURL + pullPosString + "?entity=" + entity);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStart(intent, startId);

        Bundle extras = intent.getExtras();
        timerStatus = extras.get("timerStatus").toString();
        timerInterval = extras.get("timerInterval").toString();
        gpsDist = extras.get("gpsDist").toString();
        gpsTime = extras.get("gpsTime").toString();
        giroSense = extras.get("giroSense").toString();
        transmit = extras.get("transmiter").toString();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel
                    = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Log.d(TAG, "Serviço iniciado. onStartCommand() - timerInterval: " + timerInterval.toString() + ". timerStatus: " + timerStatus.toString());
        long TIME = (Integer.valueOf(timerInterval.toString()));
        getLocation();
        if (timer == null) {
            timer = new Timer();
            if (timerStatus.toString().equals("on")) {
                TimerTask verificaGiroConfig = new TimerTask() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run Timer");
                        buscaAlertas();
                        buscaPos();
                        Log.d(TAG, "transmit: " + transmit);
                        if (!transmit.isEmpty()) {
                            Log.d(TAG, "transmit not empty");
                            setaGiroService();
                            setaGpsService();
                            if (giroService) {
                                Log.e(TAG, " Giro start");
                                SM = (SensorManager) getSystemService(SENSOR_SERVICE);
                                acellSensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                                SM.registerListener(AlertIntentService.this, acellSensor, SensorManager.SENSOR_DELAY_NORMAL);
                            } else {
                                Log.e(TAG, " Giro stop");
                                if (SM != null) SM.unregisterListener(AlertIntentService.this);
                            }
                            if (gpsService) {
                                posListener = String.valueOf(latitude + "," + longitude);
                                new HttpPostAsyncTask(postData, RequestType.REFRESH_POS, callback)
                                        .execute(baseURL + publishPosString + "?pos=" + posListener + "&entity=" + entity);
                            }

                        }
                    }
                };
                timer.scheduleAtFixedRate(verificaGiroConfig, TIME, TIME);
            } else {
                Log.d(TAG, "Encerrando o serviço");
                timer.cancel();
                //stopSelf();
            }
        }

        return START_STICKY;

    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "destoy");

    }

    IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public AlertIntentService getServerInstance() {
            return AlertIntentService.this;
        }
    }

    public static String stopTimer(){
//        timer.cancel();
        return "Timer Stopped";
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final Float _x = event.values[0];
        final Float _y = event.values[1];
        final Float _z = event.values[2];
        if (x == 0.0f) {
            x = _x;y = _y;z = _z;
        } else {
            Float sense = Float.parseFloat(giroSense.toString());
            if ((x - _x > sense || y - _y > sense || z - _z > sense)) {
                x = _x;y = _y;z = _z;
                posListener = String.valueOf(latitude + "," + longitude);
                Log.e(TAG, "Send alerta giro" + posListener);
                new HttpPostAsyncTask(postData, RequestType.REFRESH_ALERT, callback)
                        .execute(baseURL + publishAlertString + "?pos=" + posListener + "&giro=true&entity=" + entityAlert);
            } else {
                // Log.d(TAG,"varia;áo menor que 1");
            }

        }

    }

    @Override
    public void onLocationChanged(Location location) {
        locationManager.removeUpdates(this);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Toast.makeText(AlertIntentService.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("LOG", "onProviderDisabled");
        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(i);
    }

    public boolean isLocationEnabled(Context context)
    {
        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(i);
        return true;
    }

    @SuppressLint("MissingPermission")
    protected void getLocation() {
        if (isLocationEnabled(AlertIntentService.this)) {
            locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e(TAG, "GPS is on");
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Toast.makeText(AlertIntentService.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
            }
            else{
                locationManager.requestLocationUpdates(bestProvider, Integer.valueOf(gpsTime), Integer.valueOf(gpsDist), this);
            }
        }
        else
        {
            Toast.makeText(AlertIntentService.this, "You need to enable location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    @Override
    public void completionHandler(Boolean success, RequestType type, Object object) {}














    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.conecta.restserver.action.FOO";
    private static final String ACTION_BAZ = "com.conecta.restserver.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.conecta.restserver.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.conecta.restserver.extra.PARAM2";

    public AlertIntentService() {
        super("AlertIntentService");
        Log.e(TAG, "alertIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, AlertIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
        Log.e("Service", "aqui: startAction");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);

            }
        }
    }
    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        Log.d(TAG, "handleActionFoo");
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
