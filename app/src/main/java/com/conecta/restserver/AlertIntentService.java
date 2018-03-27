package com.conecta.restserver;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AlertIntentService extends IntentService implements SensorEventListener, CustomCallback {
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
    HttpPostAsyncTask task, task2;

    CustomCallback callback = new CustomCallback() {
        @Override
        public void completionHandler(Boolean success, RequestType type, final Object object) {
            switch (type) {
                case TRAKERPOS_PULL:

                    break;

                case REFRESH_POS:

                    break;

                case REFRESH_ALERT:

                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
    SM = (SensorManager) getSystemService(SENSOR_SERVICE);
    acellSensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener((SensorEventListener) this, acellSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        Log.d(TAG, "Serviço iniciado. onStartCommand()");
        return flags;
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
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, AlertIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
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
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final Float _x = event.values[0];
        final Float _y = event.values[1];
        final Float _z = event.values[2];
        //acelldisp.setText("X: " + _x + ", y: " + _y + ", z: " + _z);

        if (x == 0.0f) {
            x = _x;
            y = _y;
            z = _z;
        } else {
            if ((x - _x > 1.0f || y - _y > 1.0f || z - _z > 1.0f)) {
                x = _x;
                y = _y;
                z = _z;
                //sendDataButton.performClick();
               // @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
               // posListener = String.valueOf(location.getLatitude()+ "," + location.getLongitude());
                if (posListener == null) posListener = "0.0,0.0";
                Log.e(TAG, "update posListener" + posListener);
                postData.clear();
                postData.put("entity", entity);

                new HttpPostAsyncTask(postData, RequestType.REFRESH_POS, callback)
                        .execute(baseURL + publishPosString + "?pos=" + posListener + "&entity=" + entity);


                Log.e(TAG, "Send alerta giro" + posListener);
                postData.clear();
                postData.put("entity", entity);
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
}
