package com.darryncampbell.locationapiexerciser;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by darry on 06/01/2017.
 */

public class GeofenceUtilities {

    public static boolean USE_SERVICES_TO_RECEIVE_GEOFENCES = false;
    public static final String TAG = "LOCATION API EXERCISER";
    private SharedPreferences mSharedPreferences = null;
    private String PACKAGE_NAME = "com.darryn.campbell.locationapiexerciser";
    private String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";
    private String GEOFENCES_ADDED_KEY_MANAGER = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY_MANAGER";
    private String GEOFENCES_ADDED_KEY_SERVICES = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY_SERVICES";
    private Context context;
    private Boolean mGeofencesAddedForLocationManager = false;
    private Boolean mGeofencesAddedForLocationServices = false;
    public static float TWENTY_METERS = 20;
    public static long TWELVE_HOURS_IN_MILLISECONDS = 12 * 60 * 60 * 1000;
    public static final int FIVE_SECONDS = 5000;
    public static final int GEOFENCE_LOITERING_DELAY_IN_MS = FIVE_SECONDS;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = TWELVE_HOURS_IN_MILLISECONDS;
    public static final int DO_NOT_EXPIRE = -1;


    public GeofenceUtilities(Context context)
    {
        this.context = context;
        mSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);

        if (mSharedPreferences != null)
            mGeofencesAddedForLocationManager = mSharedPreferences.getBoolean(GEOFENCES_ADDED_KEY_MANAGER, false);
        else
            Log.e(TAG, "Problem accessing Shared preferences");
        if (mSharedPreferences != null)
            mGeofencesAddedForLocationServices = mSharedPreferences.getBoolean(GEOFENCES_ADDED_KEY_SERVICES, false);
        else
            Log.e(TAG, "Problem accessing Shared preferences");
    }

    public Boolean GetGeofencesAddedForLocationManager()
    {
        return mGeofencesAddedForLocationManager;
    }

    public void SetGeofencesAddedForLocationManager(Boolean bGeofenceAdded)
    {
        mGeofencesAddedForLocationManager = bGeofenceAdded;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(GEOFENCES_ADDED_KEY_MANAGER, mGeofencesAddedForLocationManager);
        editor.apply();
    }

    public void SetGeofencesAddedForLocationServices(Boolean bGeofenceAdded)
    {
        mGeofencesAddedForLocationServices = bGeofenceAdded;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(GEOFENCES_ADDED_KEY_SERVICES, mGeofencesAddedForLocationServices);
        editor.apply();
    }

    public Boolean GetGeofencesAddedForLocationServices()
    {
        return mGeofencesAddedForLocationServices;
    }


    public static void sendNotification(String notificationDetails, Context context, int id) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_action_name)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(context.getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);
        builder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, builder.build());
    }

    public static void cancelNotification(Context context, int id)
    {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }

    //  Utilities associated with Location Services Geofences
    public static String getErrorString(Context context, int errorCode) {
        Resources mResources = context.getResources();
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return mResources.getString(R.string.geofence_not_available);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return mResources.getString(R.string.geofence_too_many_geofences);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return mResources.getString(R.string.geofence_too_many_pending_intents);
            default:
                return mResources.getString(R.string.unknown_geofence_error);
        }
    }

    public static String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition, context);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    public static String getTransitionString(int transitionType, Context context) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return context.getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return context.getString(R.string.geofence_transition_exited);
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return context.getString(R.string.geofence_transition_dwelling);
            default:
                return context.getString(R.string.unknown_geofence_transition);
        }
    }

}
