package com.conecta.restserver;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationGetter {
    private Timer timer;
    private LocationManager locationManager;
    private LocationResult locationResult;
    private boolean gpsEnabled = false;
    private boolean networkEnabled = false;

    @SuppressLint("MissingPermission")
    public boolean getLocation(Context context, LocationResult result) {
        locationResult = result;

        if (locationManager == null) {
            locationManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
        }

        try {
            gpsEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            networkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        // stop if no providers are enabled
        if (!gpsEnabled && !networkEnabled) {
            return false;
        }

        if (gpsEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0,locationListenerGps);
        }

        if (networkEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        }

        timer = new Timer();
        timer.schedule(new GetLastLocation(), 20000);

        return true;
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer.cancel();
            locationResult.gotLocation(location);
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerNetwork);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer.cancel();
            locationResult.gotLocation(location);
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerGps);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    class GetLastLocation extends TimerTask {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            locationManager.removeUpdates(locationListenerGps);
            locationManager.removeUpdates(locationListenerNetwork);

            Location netLocation = null, gpsLocation = null;

            if (gpsEnabled)
                gpsLocation = locationManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (networkEnabled)
                netLocation = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            // if there are both values use the latest one
            if (gpsLocation != null && netLocation != null) {
                if (gpsLocation.getTime() > netLocation.getTime())
                    locationResult.gotLocation(gpsLocation);
                else
                    locationResult.gotLocation(netLocation);

                return;
            }

            if (gpsLocation != null) {
                locationResult.gotLocation(gpsLocation);
                return;
            }

            if (netLocation != null) {
                locationResult.gotLocation(netLocation);
                return;
            }

            locationResult.gotLocation(null);
        }
    }

    // Represents a Location Result
    public static abstract class LocationResult {
        public abstract void gotLocation(Location location);
    }
}