package com.darryncampbell.locationapiexerciser;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

/**
 * Created by darry on 30/12/2016.
 */

public class LocationManagerWrapper {

    public static final String TAG = "LOCATION API EXERCISER";
    static final long TEN_SECONDS = 1000 * 10;
    static final long TIME_BETWEEN_GPS_UPDATES = TEN_SECONDS;
    static final long TIME_BETWEEN_NETWORK_UPDATES = TEN_SECONDS;

    Location gpsLocationAosp;
    Location networkLocationAosp;
    LocationManager locationManager;
    String customProviderName;
    LocationProvider gpsProvider;
    LocationProvider networkProvider;
    Context context;
    LocationUI ui;
    LocationListener gpsListener;
    LocationListener networkListener;
    Boolean mStarted;

    public LocationManagerWrapper(LocationUI ui, Context context, String customProviderName) {
        this.ui = ui;
        this.context = context;
        locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        this.customProviderName = customProviderName;
        gpsListener = null;
        networkListener = null;
        mStarted = false;
    }

    public void populateUiStatus() {
        TextView txtStatusEnabled = (TextView) ((Activity) context).findViewById(R.id.txtStatusEnabled);
        TextView txtGpsProviderStatus = (TextView) ((Activity) context).findViewById(R.id.txtGpsProviderStatus);
        TextView txtNetworkProviderStatus = (TextView) ((Activity) context).findViewById(R.id.txtNetworkProviderStatus);
        TextView txtOtherProviders = (TextView) ((Activity) context).findViewById(R.id.txtOtherProviderStatus);
        TextView txtGPSAddress = (TextView) ((Activity) context).findViewById(R.id.txtGPSAddress);
        TextView txtNetworkAddress = (TextView) ((Activity) context).findViewById(R.id.txtNetworkAddress);

        if (isLocationEnabled(context))
            txtStatusEnabled.setText("Yes");
        else
            txtStatusEnabled.setText("No");

        //  GPS Provider
        try {
            gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
            if (gpsProvider == null)
                txtGpsProviderStatus.setText("No GPS");
            else {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    txtGpsProviderStatus.setText("Enabled");
                else {
                    txtGpsProviderStatus.setText("Disabled");
                    txtGPSAddress.setText("Provider Disabled");
                }
            }
        } catch (SecurityException e) {
            txtGpsProviderStatus.setText("Permissions Error");
        }

        //  Network Provider
        try {
            networkProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);
            if (networkProvider == null)
                txtNetworkProviderStatus.setText("No Network");
            else {
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                    txtNetworkProviderStatus.setText("Enabled");
                else {
                    txtNetworkProviderStatus.setText("Disabled");
                    txtNetworkAddress.setText("Provider Disabled");
                }
            }
        } catch (SecurityException e) {
            txtNetworkProviderStatus.setText("Permissions Error");
        }

        //  Custom Provider?
        //  todo

        //  Other Providers
        List<String> allProviders = locationManager.getAllProviders();
        String allProviderStatus = "";
        for (int i = 0; i < allProviders.size(); i++) {
            if (!allProviders.get(i).equalsIgnoreCase("gps") &&
                    !allProviders.get(i).equalsIgnoreCase("network") &&
                    !allProviders.get(i).equalsIgnoreCase(customProviderName)) {
                if (!(allProviderStatus.equals("")))
                    allProviderStatus += ", ";
                allProviderStatus += allProviders.get(i);
            }
        }
        txtOtherProviders.setText(allProviderStatus);
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public void stopAospLocation() {
        mStarted = false;
        if (gpsListener != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(gpsListener);
            }
            gpsListener = null;
        }
        if (networkListener != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(networkListener);
            }
            networkListener = null;
        }
    }

    public void startAospLocation() {
        final TextView txtGpsLatitude = (TextView) ((Activity) context).findViewById(R.id.txtGpsLatitude);
        final TextView txtGpsLongitude = (TextView) ((Activity) context).findViewById(R.id.txtGpsLongitude);
        final TextView txtGpsAccuracy = (TextView) ((Activity) context).findViewById(R.id.txtGpsAccuracy);
        final TextView txtGpsProviderStatus = (TextView) ((Activity) context).findViewById(R.id.txtGpsProviderStatus);
        final TextView txtNetworkLatitude = (TextView) ((Activity) context).findViewById(R.id.txtNetworkLatitude);
        final TextView txtNetworkLongitude = (TextView) ((Activity) context).findViewById(R.id.txtNetworkLongitude);
        final TextView txtNetworkAccuracy = (TextView) ((Activity) context).findViewById(R.id.txtNetworkAccuracy);
        final TextView txtNetworkProviderStatus = (TextView) ((Activity) context).findViewById(R.id.txtNetworkProviderStatus);
        final TextView txtGPSAddress = (TextView) ((Activity) context).findViewById(R.id.txtGPSAddress);
        final TextView txtNetworkAddress = (TextView) ((Activity) context).findViewById(R.id.txtNetworkAddress);
        if (mStarted)
            return;
        mStarted = true;

        gpsListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                gpsLocationAosp = location;
                ui.UpdateUIWithLocation(txtGpsLatitude, txtGpsLongitude, txtGpsAccuracy, txtGPSAddress, gpsLocationAosp);
                Log.i(TAG, "Received Location from GPS: " + location.toString());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                switch (i) {
                    case LocationProvider.OUT_OF_SERVICE:
                        txtGpsProviderStatus.setText("Out of Service");
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        txtGpsProviderStatus.setText("Temporarily Down");
                        break;
                    case LocationProvider.AVAILABLE:
                        txtGpsProviderStatus.setText("Enabled");
                        break;
                    default:
                        txtGpsProviderStatus.setText("Error");
                }
            }

            @Override
            public void onProviderEnabled(String s)
            {
                txtGpsProviderStatus.setText("Enabled");
                Log.i(TAG, "GPS Provider is Enabled");
            }

            @Override
            public void onProviderDisabled(String s)
            {
                txtGpsProviderStatus.setText("Disabled");
                txtGPSAddress.setText("Provider Disabled");
                Log.i(TAG, "GPS Provider is disabled");
            }
        };

        networkListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location) {
                networkLocationAosp = location;
                ui.UpdateUIWithLocation(txtNetworkLatitude, txtNetworkLongitude, txtNetworkAccuracy, txtNetworkAddress, networkLocationAosp);
                Log.i(TAG, "Received Location from Network: " + location.toString());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                switch (i) {
                    case LocationProvider.OUT_OF_SERVICE:
                        txtNetworkProviderStatus.setText("Out of Service");
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        txtNetworkProviderStatus.setText("Temporarily Down");
                        break;
                    case LocationProvider.AVAILABLE:
                        txtNetworkProviderStatus.setText("Enabled");
                        break;
                    default:
                        txtNetworkProviderStatus.setText("Error");
                }
            }

            @Override
            public void onProviderEnabled(String s)
            {
                txtNetworkProviderStatus.setText("Enabled");
                Log.i(TAG, "Network provider is enabled");
            }

            @Override
            public void onProviderDisabled(String s) {
                txtNetworkProviderStatus.setText("Disabled");
                txtNetworkAddress.setText("Provider Disabled");
                Log.i(TAG, "Network provider is disabled");
            }
        };

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //  Just to get rid of Eclipse warnings
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_BETWEEN_GPS_UPDATES, 0, gpsListener);
            Location lastGpsPosition = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastGpsPosition != null)
            {
                gpsLocationAosp = lastGpsPosition;
                ui.UpdateUIWithLocation(txtGpsLatitude, txtGpsLongitude, txtGpsAccuracy, txtGPSAddress, gpsLocationAosp);
            }
        }
        else
            ui.UpdateUIWithLocation(txtGpsLatitude, txtGpsLongitude, txtGpsAccuracy, txtGPSAddress, null);

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_BETWEEN_NETWORK_UPDATES, 0, networkListener);
            Location lastNetworkPosition = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastNetworkPosition != null)
            {
                networkLocationAosp = lastNetworkPosition;
                ui.UpdateUIWithLocation(txtNetworkLatitude, txtNetworkLongitude, txtNetworkAccuracy, txtNetworkAddress, networkLocationAosp);
            }
        }
        else
            ui.UpdateUIWithLocation(txtNetworkLatitude, txtNetworkLongitude, txtNetworkAccuracy, txtNetworkAddress, null);
    }
}
