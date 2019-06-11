package com.darryncampbell.locationapiexerciser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceLocServicesBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "LOCATION API EXERCISER";
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (intent != null)
        {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                String errorMessage = GeofenceUtilities.getErrorString(context,
                        geofencingEvent.getErrorCode());
                Log.e(TAG, errorMessage);
                return;
            }

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

                // Get the geofences that were triggered. A single event can trigger multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                // Get the transition details as a String.
                final String geofenceTransitionDetails = GeofenceUtilities.getGeofenceTransitionDetails(
                        context,
                        geofenceTransition,
                        triggeringGeofences
                );

                // Send notification and log the transition details.
                //  Handle we have entered the specified Geofence
                GeofenceUtilities.sendNotification(geofenceTransitionDetails, context, 1);

                Log.i(TAG, geofenceTransitionDetails);
            } else {
                // Log the error.
                Log.e(TAG, context.getString(R.string.geofence_transition_invalid_type, geofenceTransition));
            }
        }
    }

}
