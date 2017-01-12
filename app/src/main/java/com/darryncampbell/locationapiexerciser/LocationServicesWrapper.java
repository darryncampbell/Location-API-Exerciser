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
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.internal.FusedLocationProviderResult;

import java.util.ArrayList;


public class LocationServicesWrapper  implements
        ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    public static final String TAG = "LOCATION API EXERCISER";
    static final long THIRTY_SECONDS = 1000 * 30;
    static final long FIVE_SECONDS = 1000 * 5;
    static final long TEN_SECONDS = 1000 * 10;
    static final long TIME_BETWEEN_GMS_UPDATES = THIRTY_SECONDS;

    protected GoogleApiClient mGoogleApiClient;
    Location fusedLocation;
    Boolean pollingGMS;
    Context context;
    LocationUI ui;
    PendingIntent activityRecognitionPI = null;
    PendingIntent geofenceProximityPI = null;

    public LocationServicesWrapper(LocationUI ui, Context context)
    {
        pollingGMS = false;
        this.context = context;
        this.ui = ui;
        Intent intent = new Intent(context, ActivityRecognitionIntentService.class);
        activityRecognitionPI = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intent2 = new Intent(context, GeofenceLocServicesIntentService.class);
        geofenceProximityPI = PendingIntent.getService(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
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
        if (mGoogleApiClient != null) {
            unregisterForActivityRecognition();
            stopGMSLocation();
            if (mGoogleApiClient.isConnected())
            {
                mGoogleApiClient.disconnect();
            }
        }
        ui.UpdateUIWithFusedLocation(null);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Location Services connected");
        fusedLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (fusedLocation != null) {
            ui.UpdateUIApplicationServicesAvailable(true);
            ui.UpdateUIWithFusedLocation(fusedLocation);
        }
        else
            ui.UpdateUIApplicationServicesAvailable(false);

        startGMSLocation();
        registerForActivityRecognition();

    }

    private void registerForActivityRecognition()
    {
        //  Start the Activity results
        final PendingResult<Status> result = ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                TEN_SECONDS,
                activityRecognitionPI);
        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (!status.isSuccess())
                {
                    //  Something went wrong
                    Log.w(TAG, "Failed to register for Activity Recognition updates");
                }
                else
                {
                    //  Everything went OK
                    Log.i(TAG, "Activity Recognition updates successfully registered for");
                }
            }
        });

    }

    private void unregisterForActivityRecognition()
    {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
        {
            final PendingResult<Status> result = ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    mGoogleApiClient,
                    activityRecognitionPI);
            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (!status.isSuccess())
                    {
                        //  Something went wrong
                        Log.w(TAG, "Failed to unregister for Activity Recognition updates");
                    }
                    else
                    {
                        //  Everything went OK
                        Log.i(TAG, "Activity Recognition updates successfully unregistered");
                    }
                }
            });
        }

    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        ui.UpdateUIApplicationServicesAvailable(false);
        Log.w(TAG, "Location Services connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        Log.w(TAG, "Location Services connection suspended");
        ui.UpdateUIApplicationServicesAvailable(false);
        mGoogleApiClient.connect();
    }

    public void startGMSLocation()
    {
        if (pollingGMS)
            return;

        pollingGMS = true;

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(TIME_BETWEEN_GMS_UPDATES);
        locationRequest.setFastestInterval(TEN_SECONDS);
        final PendingResult<Status> result =
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (!status.isSuccess())
                {
                    //  Something went wrong
                    Log.w(TAG, "Failed to register for Location updates via location services");
                }
                else
                {
                    //  Everything went OK
                    Log.i(TAG, "Location service updates successfully registered for");
                }
            }
        });

    }

    public void stopGMSLocation()
    {
        pollingGMS = false;
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
        {
            final PendingResult<Status> result =
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (!status.isSuccess())
                    {
                        //  Something went wrong
                        Log.w(TAG, "Failed to unregister Location updates via location services");
                    }
                    else
                    {
                        //  Everything went OK
                        Log.i(TAG, "Location service updates successfully unregistered");
                    }
                }
            });
        }
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
        //  Check we are conneted to Google location services
        if (!mGoogleApiClient.isConnected())
        {
            Toast.makeText(theActivity, "Not connected to Google Location Services", Toast.LENGTH_SHORT).show();
            return;
        }
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(TEN_SECONDS);
        mLocationRequest.setFastestInterval(FIVE_SECONDS);
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
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location requests.
                        Toast.makeText(theActivity, "Location Settings CORRECT", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Location Settings returned Success");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            Toast.makeText(theActivity, "Need additional permissions", Toast.LENGTH_SHORT).show();
                            status.startResolutionForResult(
                                    theActivity,
                                    MainActivity.LOCATION_SETTINGS_PERMISSION_REQUEST);
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

    public void startGeofence(Location location) {
        if (!mGoogleApiClient.isConnected())
        {
            //  Client is not connected
            Log.e(TAG, "Unable to create Geofence as client is not connected");
            Toast.makeText(context, "Unable to create Geofence as Client is not connected", Toast.LENGTH_SHORT);
        }
        else {
            if (geofenceProximityPI != null) {
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        getGeofencingRequest(location),
                        geofenceProximityPI).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (!status.isSuccess())
                        {
                            //  Something went wrong
                            Log.w(TAG, "Failed to create Geofence for Location services");
                        }
                        else
                        {
                            //  Everything went OK
                            Log.i(TAG, "Successfully created geofence for location services");
                        }
                    }
                });
            }
        }
    }

    public Boolean stopGeofence() {
        if (!mGoogleApiClient.isConnected())
        {
            //  Client is not connected
            Log.e(TAG, "Unable to remove Geofence as client is not connected");
            Toast.makeText(context, "Unable to remove Geofence as Client is not connected", Toast.LENGTH_SHORT);
            return false;
        }
        else {
            if (geofenceProximityPI != null) {
                LocationServices.GeofencingApi.removeGeofences(
                        mGoogleApiClient,
                        geofenceProximityPI).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (!status.isSuccess())
                        {
                            Log.w(TAG, "Failed to remove Geofence for Location services");
                        }
                        else
                        {
                            Log.i(TAG, "Successfully removed geofence for location services");
                            GeofenceUtilities.cancelNotification(context, 1);
                        }
                    }
                });
                return true;
            }
        }
        return false;
    }

    private GeofencingRequest getGeofencingRequest(Location location) {

        ArrayList<Geofence> geofenceList = new ArrayList<Geofence>();
        geofenceList.add(new Geofence.Builder()
                .setRequestId("From Location Services")
                .setCircularRegion(
                        location.getLatitude(),
                        location.getLongitude(),
                        GeofenceUtilities.TWENTY_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT |
                        Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(GeofenceUtilities.GEOFENCE_LOITERING_DELAY_IN_MS)
                .build());
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }
}
