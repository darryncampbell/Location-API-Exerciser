package com.darryncampbell.locationapiexerciser;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.GeofenceStatusCodes;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by darry on 06/01/2017.
 */

public class GeofenceUtilities {

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
        builder.setSmallIcon(R.mipmap.ic_launcher)
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


}
