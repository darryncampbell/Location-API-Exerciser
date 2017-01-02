package com.darryncampbell.locationapiexerciser;

/**
 * Created by darry on 30/12/2016.
 */

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;


public class LocationServicesWrapper  implements
        ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    public static final String TAG = "LOCATION API EXERCISER";

    protected GoogleApiClient mGoogleApiClient;
    Location fusedLocation;
    Boolean pollingGMS;
    Context context;
    LocationUI ui;

    public LocationServicesWrapper(LocationUI ui, Context context)
    {
        pollingGMS = false;
        this.context = context;
        this.ui = ui;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
    }

    public void initializeAndConnect()
    {
        buildGoogleApiClient();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    public void onStart()
    {
        initializeAndConnect();
    }

    public void onStop()
    {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Location Services connected");
        fusedLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (fusedLocation != null) {
            ui.UpdateUIApplicationServicesAvailable("Yes");
            ui.UpdateUIWithFusedLocation(fusedLocation);
            startGMSLocation();
        }
        else
            ui.UpdateUIApplicationServicesAvailable("No");

        //  Start the ACtivity results
        //  todo - tidy
        Intent intent = new Intent(context, FetchAddressIntentService.class);
        //intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mActivityReceiver);
        //intent.putExtra("HI3", (ResultReceiver)mActivityReceiver);
        //intent.putExtra("HI3", "HIII");
        PendingIntent pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                5000,
                pi);
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    public void startGMSLocation()
    {
        if (pollingGMS)
            return;

        pollingGMS = true;
        final long THIRTY_SECONDS = 1000 * 30;
        final long FIVE_SECONDS = 1000 * 5;
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(THIRTY_SECONDS);
        locationRequest.setFastestInterval(FIVE_SECONDS);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        //  GMS Location Listener
        fusedLocation = location;
        ui.UpdateUIWithFusedLocation(fusedLocation);
        Log.i(TAG, "Received location from Google Services: " + location.toString());
    }

    public void GetLocationSettings(final Activity theActivity, Boolean needBle)
    {
        //  todo pass as parameters
        //  todo return
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setNeedBle(needBle);
        builder.setAlwaysShow(true);
        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
//                  final LocationSettingsStates = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Toast.makeText(theActivity, "Location Settings CORRECT", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Location Settings returned Success");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            Toast.makeText(theActivity, "Need additional permissions", Toast.LENGTH_LONG).show();
                            status.startResolutionForResult(
                                    theActivity,
                                    1);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }

                        Log.w(TAG, "Location Settings returned Resolution Required.");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Toast.makeText(theActivity, "Location Settings CANNOT be satisfied", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Location Settings returned Change Unavailable");
                        break;
                }
            }
        });
    }
}
