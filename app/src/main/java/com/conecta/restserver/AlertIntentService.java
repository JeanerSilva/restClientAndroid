package com.conecta.restserver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AlertIntentService extends IntentService implements SensorEventListener, CustomCallback, LocationListener {
    private Sensor acellSensor;
    private SensorManager SM;
    Float x = 0.0f;
    Float y = 0.0f;
    Float z = 0.0f;
    String TAG = "Service Alert";
    String posListener;
    private LocationManager locationManager;
    Map<String, String> postData = new HashMap<>();
    private final String entity = "moto";
    private final String deletePosString = "posdelete";
    private final String publishPosString = "pospublish";
    private final String pullPosString = "pospull";
    private final String entityAlert = "motoalert";
    private final String publishAlertString = "alertpublish";
    private final String pullAlertString = "alertpull";
    private final String alertDeleteString = "alertdelete";
    private final String baseURL = "https://coliconwg.appspot.com/";
    private final String configString = "config";
    private final String entityConfig = "motoconfig";
    private boolean giroService = false;
    private boolean gpsService = false;
    private LocationListener listener;
    public double latitude;
    public double longitude;
    public Criteria criteria;
    public String bestProvider;
    Timer timer;

    CustomCallback callback = new CustomCallback() {
        @Override
        public void completionHandler(Boolean success, RequestType type, final Object object) {
            switch (type) {
                case CONFIG_GIRO_PULL:
                    List<String> statusGiro = (List<String>) object;
                    if (statusGiro.get(0).toString().equals("on")) {
                        giroService = true;
                    }
                    if (statusGiro.get(0).toString().equals("off")) {
                        giroService = false;
                    }
                    if (giroService) {
                        Log.e(TAG, " Giro start");
                        SM = (SensorManager) getSystemService(SENSOR_SERVICE);
                        acellSensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                        SM.registerListener(AlertIntentService.this, acellSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    } else {
                        Log.e(TAG, " Giro stop");
                        SM.unregisterListener(AlertIntentService.this);
                    }
                    break;
                case CONFIG_GPS_PULL:
                    List<String> statusGps = (List<String>) object;
                    if (statusGps.get(0).toString().equals("on")) {
                        gpsService = true;
                    }
                    if (statusGps.get(0).toString().equals("off")) {
                        gpsService = false;
                    }

                    if (gpsService) {
                        Log.e(TAG, " GPS start");

                    } else {
                        Log.e(TAG, " GPS stop");

                    }
                    break;
                case REFRESH_POS:

                    break;
                default:
                    break;
            }
        }
    };

    public void setaGiroService (){
        Log.d(TAG, "Giro ON ");
        postData.clear();
        postData.put("entity", entity);
        HttpPostAsyncTask task =
                new HttpPostAsyncTask(postData, RequestType.CONFIG_GIRO_PULL, callback);
        task.execute(baseURL + configString + "?entity=" + entityConfig + "giro" + "&action=pull");
    }

    public void setaGpsService (){
        Log.d(TAG, "Giro ON ");
        postData.clear();
        postData.put("entity", entity);
        HttpPostAsyncTask task =
                new HttpPostAsyncTask(postData, RequestType.CONFIG_GPS_PULL, callback);
        task.execute(baseURL + configString + "?entity=" + entityConfig + "gps" + "&action=pull");
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Serviço iniciado. onStartCommand()");
        long TIME = (1000 * 3);

        if (timer == null) {
            timer = new Timer();
            TimerTask verificaGiroConfig = new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG, "run Timer");
                    setaGiroService();
                    setaGpsService();
                    if (gpsService) {
                        new HttpPostAsyncTask(postData, RequestType.REFRESH_POS, callback)
                                .execute(baseURL + publishPosString + "?pos=" + posListener + "&entity=" + entity);
                    }
                }
            };
            timer.scheduleAtFixedRate(verificaGiroConfig, TIME, TIME);
        }
        getLocation();
        return startId;

    };


    @Override
    public void onSensorChanged(SensorEvent event) {
        final Float _x = event.values[0];
        final Float _y = event.values[1];
        final Float _z = event.values[2];
        if (x == 0.0f) {
            x = _x;y = _y;z = _z;
        } else {
            if ((x - _x > 1.0f || y - _y > 1.0f || z - _z > 1.0f)) {
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void completionHandler(Boolean success, RequestType type, Object object) {

    }

    @Override
    public void onLocationChanged(Location location) {
        locationManager.removeUpdates(this);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Toast.makeText(AlertIntentService.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("LOG", "onProviderDisabled");
        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(i);
    }

    public static boolean isLocationEnabled(Context context)
    {
        //...............
        return true;
    }

    protected void getLocation() {
        if (isLocationEnabled(AlertIntentService.this)) {
            locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

            //You can still do this if you like, you might get lucky:
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e("TAG", "GPS is on");
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Toast.makeText(AlertIntentService.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
                //searchNearestPlace(voice2text);
            }
            else{
                //This is what you need:
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
            }
        }
        else
        {
            Toast.makeText(AlertIntentService.this, "You need to enable location", Toast.LENGTH_SHORT).show();
        }


    }
















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
