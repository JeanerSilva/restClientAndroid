package com.conecta.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.conecta.enums.OperationMode;
import com.conecta.enums.RequestType;
import com.conecta.models.Alert;
import com.conecta.models.Config;
import com.conecta.models.TrackerPos;
import com.conecta.restserver.R;
import com.conecta.util.CustomCallback;
import com.conecta.util.HttpPostAsyncTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.conecta.models.AppConfig.*;

public class AlertIntentService extends IntentService implements SensorEventListener, CustomCallback, LocationListener {
    private List<Alert> alertList = new ArrayList<>();
    List<TrackerPos> trackerPosList = new ArrayList<>();
    private Sensor acellSensor;
    private SensorManager SM;
    Float x = 0.0f, y = 0.0f, z = 0.0f;
    String TAG = "Service";
    private String posListener = "1.1,1.1";
    private LocationManager locationManager;
    Map<String, String> postData = new HashMap<>();
    public double latitude, longitude;
   // public Criteria criteria;
    public String bestProvider;
    Timer timer;
    String gpsDist, gpsTime, giroAlert, gpsAlert, whatsApp, giroSense;
    OperationMode operationMode;
    NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;
    String NOTIFICATION_CHANNEL_ID1 = "my_channel_id_01";
    String NOTIFICATION_CHANNEL_ID2 = "my_channel_id_02";
    String timerInterval;
    Location location;
    long count = 0;
    volatile boolean gotConfig;

    /** this criteria will settle for less accuracy, high power, and cost */
    public static Criteria createCoarseCriteria() {
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_COARSE);
        c.setAltitudeRequired(false);
        c.setBearingRequired(false);
        c.setSpeedRequired(false);
        c.setCostAllowed(true);
        c.setPowerRequirement(Criteria.POWER_HIGH);
        return c;
    }

    /** this criteria needs high accuracy, high power, and cost */
    public static Criteria createFineCriteria() {
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
        c.setAltitudeRequired(false);
        c.setBearingRequired(false);
        c.setSpeedRequired(false);
        c.setCostAllowed(true);
        c.setPowerRequirement(Criteria.POWER_HIGH);
        return c;
    }

    CustomCallback callback = new CustomCallback() {
        @Override
        public void completionHandler(Boolean success, RequestType type, final Object object) {
            switch (type) {
                case CONFIG_PULL:
                    if (object instanceof String) {
                        Log.d(TAG, object.toString());
                    } else {
                        Log.d(TAG, "CONFIG_GIRO_PULL");
                        Config config = (Config) object;
                        giroAlert = config.getGiroStatus();
                        gpsAlert = config.getGpsStatus();
                        gpsTime = config.getGpsTime();
                        gpsDist = config.getGpsDist();
                        giroSense = config.getGiroSense();
                        whatsApp = config.getWhatsApp();
                        gotConfig = true;
                        Log.d(TAG, "statusGiroString: " + config);
                    }
                    break;
                case REFRESH_POS:
                    Log.e(TAG, "Pos sent");
                    break;
                case ALERT_PULL:
                    if (object instanceof String) {
                        Log.d(TAG, object.toString());
                    } else {
                        Log.d(TAG, "ALERT_PULL");
                        int totalAlerts = alertList.size();
                        alertList.clear();
                        alertList.addAll((List<Alert>) object);
                        int newTotal = alertList.size();

                        if (totalAlerts != newTotal) {
                            Log.d(TAG, "Alertas alterou de " + totalAlerts + " para " + newTotal);
                            if (operationMode.equals(OperationMode.RECEPTOR)) {
                                notificationBuilder = new NotificationCompat.Builder(
                                        AlertIntentService.this, NOTIFICATION_CHANNEL_ID1);

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
                    }
                    break;

                case TRAKERPOS_PULL:
                    if (object instanceof String) {
                        Log.d(TAG, object.toString());
                    } else {
                        Log.d(TAG, "TRAKERPOS_PULL");
                        int totalPos = trackerPosList.size();
                        trackerPosList.clear();
                        trackerPosList.addAll((List<TrackerPos>) object);
                        int newTotalPos = trackerPosList.size();

                        Toast.makeText(AlertIntentService.this, "New pos.", Toast.LENGTH_SHORT).show();
                        if (totalPos != newTotalPos) {
                            Log.d(TAG, "Número de movimentações alterou de " + totalPos + " para " + newTotalPos);
                            if (operationMode.equals(OperationMode.RECEPTOR)) {
                                notificationBuilder = new NotificationCompat.Builder(
                                        AlertIntentService.this, NOTIFICATION_CHANNEL_ID2);

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
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void buscaConfig() {
        Log.d(TAG, "Busca Giro Status ");
        new HttpPostAsyncTask(postData, RequestType.CONFIG_PULL, callback)
                .execute(baseURL + configString + "?entity=" + entityConfig + "&action=pull");
    }

    private void buscaAlertas() {
        Log.d(TAG, "Search alerts: " + entityAlert);
        new HttpPostAsyncTask(postData, RequestType.ALERT_PULL, callback)
                .execute(baseURL + pullAlertString + "?entity=" + entityAlert);
    }

    private void buscaPos() {
        Log.d(TAG, "Search positions: " + entity);
        new HttpPostAsyncTask(postData, RequestType.TRAKERPOS_PULL, callback)
                .execute(baseURL + pullPosString + "?entity=" + entity);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStart(intent, startId);
        Bundle b = intent.getExtras();

        timerInterval = b.getString("timerinterval");
        String operationModeS = b.getString("operationalmode");
        if (operationModeS.equals("RECEPTOR")) operationMode = OperationMode.RECEPTOR;
        if (operationModeS.equals("TRANSMITER")) operationMode = OperationMode.TRANSMITER;
        gpsDist = b.getString("gpsdist");
        gpsTime = b.getString("gpstime");

        Log.d(TAG, "timerinterval: " + timerInterval
                    + " operationalmode: " + operationModeS
                    + " gpsDist: " + gpsDist
                    + " gpsTime: " + gpsTime
                    );

        getLocation();
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "destroy");
        //timer.cancel();
        Toast.makeText(AlertIntentService.this, "Service destroyed.", Toast.LENGTH_SHORT).show();
    }

    public IBinder mBinder = new LocalBinder();


    public class LocalBinder extends Binder {
        public AlertIntentService getService() {
            // Return this instance of LocalService so clients can call public methods
            return AlertIntentService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    public String stopTimer() {
        Log.d(TAG, "Timer Stopped.");
        try {
            if (null != SM) SM.unregisterListener(AlertIntentService.this);
            timer.cancel();
            count = 0;

        } catch (Exception e) {
            return null;
        }

        return "Timers Stopped";
    }



    public String startTimer() {

        SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        acellSensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(AlertIntentService.this, acellSensor, SensorManager.SENSOR_DELAY_NORMAL);

        buscaConfig();
        while (!gotConfig) {}
        String message = "Timer iniciado - timerInterval: " + timerInterval + ". Modo de operação: " + operationMode.toString();
        Log.d(TAG, message);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel
                    = new NotificationChannel(NOTIFICATION_CHANNEL_ID1, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

            @SuppressLint("WrongConstant") NotificationChannel notificationChannel2
                    = new NotificationChannel(NOTIFICATION_CHANNEL_ID2, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel2);


        }

        timer = new Timer();
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        count++;
                        Log.d(TAG, "Timer running - modo: " + operationMode);
                        location = prepareLocation();
                        posListener = String.valueOf(latitude + "," + longitude);
                        Log.e(TAG, "Timer - posListener: " + posListener);

                        if (operationMode.equals(OperationMode.RECEPTOR)) buscaAlertas();

                        if (operationMode.equals(OperationMode.TRANSMITER)) {
                            buscaConfig();
                            if (null != whatsApp && whatsApp.equals("whatsapp")) {
                                location = prepareLocation();
                                String url = baseURL + publishPosString + "?pos=" + posListener + "&entity=" + entity;
                                new HttpPostAsyncTask(postData, RequestType.REFRESH_POS, callback)
                                        .execute(url);
                                Log.e(TAG, "PUBLISH POS = " + url);
                            }
                            if (giroAlert.equals("on")) {
                                Log.d(TAG, " Giro running.");
                                if (null == SM) {
                                    SM = (SensorManager) getSystemService(SENSOR_SERVICE);
                                    assert SM != null;
                                    acellSensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                                    SM.registerListener(AlertIntentService.this, acellSensor, SensorManager.SENSOR_DELAY_NORMAL);
                                }
                            } else {
                                Log.d(TAG, " Giro stopped");
                                if (null != SM) {
                                    SM.unregisterListener(AlertIntentService.this);
                                    //SM = null;
                                }
                            }
                        }
                    }
                },
                Long.valueOf(timerInterval), Long.valueOf(timerInterval));


        return message;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final Float _x = event.values[0];
        final Float _y = event.values[1];
        final Float _z = event.values[2];


        if (x == 0.0f) {
            x = _x;y = _y;z = _z;
        } else {

            if (null != giroSense && giroAlert.equals("on")
                    && operationMode == OperationMode.TRANSMITER) {
                Float sense = Float.parseFloat(giroSense);
                if ((x - _x > sense || y - _y > sense || z - _z > sense)) {
                    Log.e(TAG, " onSensorChanged() - giroAlert: " + giroAlert);
                    x = _x;y = _y;z = _z;
                    posListener = String.valueOf(latitude + "," + longitude);
                    Log.d(TAG, "Send alerta giro" + posListener);
                    String url = baseURL + publishAlertString + "?pos=" + posListener + "&font=giro&entity=" + entityAlert;
                    new HttpPostAsyncTask(postData, RequestType.REFRESH_ALERT, callback)
                            .execute(url);
                    Log.e(TAG, "GIRO ALERT = " + url);
                } else {
                    // Log.d(TAG,"varia;áo menor que 1");
                }
            }
        }

    }

    public String getCurrentLocation (){
        prepareLocation();
        return String.valueOf(latitude + "," + longitude) + " - " + String.valueOf(count) ;
    }

    @Override
    public void onLocationChanged(Location location) {
        locationManager.removeUpdates(this);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Toast.makeText(AlertIntentService.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "locationChanged()");
        posListener = String.valueOf(latitude + "," + longitude);
        if (gpsAlert.equals("on") && operationMode == OperationMode.TRANSMITER) {
            String urlPos = baseURL + publishPosString + "?pos=" + posListener + "&entity=" + entity;
            new HttpPostAsyncTask(postData, RequestType.REFRESH_POS, callback)
                    .execute(urlPos);
            Log.e(TAG, "GPS POS = " + urlPos);
            String urlAlert = baseURL + publishAlertString + "?pos=" + posListener + "&font=gps&entity=" + entityAlert;
            new HttpPostAsyncTask(postData, RequestType.REFRESH_ALERT, callback)
                    .execute(urlAlert);
            Log.e(TAG, "GPS ALERT = " + urlAlert);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("LOG", "onProviderDisabled");
        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(i);
    }

    public boolean isLocationEnabled(Context context) {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (provider.contains("gps")) {
            return true;
        }
        return false;
    }

    public Location prepareLocation(){
        Log.d(TAG, "getLocation() - location is enabled");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = createFineCriteria();
        bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true));
        @SuppressLint({"Missingfrmission", "MissingPermission"}) Location location = locationManager.getLastKnownLocation(bestProvider);
        posListener = String.valueOf(latitude + "," + longitude);
        return location;
    }


    @SuppressLint({"StaticFieldLeak", "MissingPermission"})
    protected void getLocation() {
        Log.d(TAG, "getLocation()");
        if (isLocationEnabled(AlertIntentService.this)) {

            location = prepareLocation();
            if (null != location && null != whatsApp && !whatsApp.equals("whatsapp")) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d(TAG, "Get Location - posListener:" + posListener);
            } else {
                try {
                    locationManager.requestLocationUpdates(bestProvider, Long.valueOf(gpsTime), Long.valueOf(gpsDist), AlertIntentService.this);
                    posListener = String.valueOf(latitude + "," + longitude);
                    Toast.makeText(AlertIntentService.this, "Get Location posListener: " + posListener, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Get Location - location == null - posListener: " + posListener);
                } catch (Exception e) {
                    Log.e(TAG, "Get Location - Exception:" + e.getMessage());
                }
            }

        }  else {
            Toast.makeText(AlertIntentService.this, "You need to enable location", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onProviderDisabled");
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(i);
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

    public AlertIntentService() {
        super("AlertIntentService");
        Log.d(TAG, "alertIntentService");
    }












    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_BAZ = "com.conecta.restserver.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.conecta.restserver.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.conecta.restserver.extra.PARAM2";



    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, AlertIntentService.class);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
        Log.d("Service", "aqui: startAction");
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
