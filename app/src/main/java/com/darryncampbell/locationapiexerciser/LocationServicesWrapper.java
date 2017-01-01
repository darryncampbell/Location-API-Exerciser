package com.darryncampbell.locationapiexerciser;

/**
 * Created by darry on 30/12/2016.
 */

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


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

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        fusedLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (fusedLocation != null) {
            ui.UpdateUIApplicationServicesAvailable("Yes");
            ui.UpdateUIWithFusedLocation(fusedLocation);
            startGMSLocation();
        }
        else
            ui.UpdateUIApplicationServicesAvailable("No");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
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
    }

}
