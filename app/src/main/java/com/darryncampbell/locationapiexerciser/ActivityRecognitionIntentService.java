package com.darryncampbell.locationapiexerciser;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * Created by darry on 06/01/2017.
 */

public class ActivityRecognitionIntentService extends IntentService {

    public static final String TAG = "LOCATION API EXERCISER";

    public ActivityRecognitionIntentService() {
        super("ActivityRegisnitionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null)
        {
            //  Activity Recognition
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            if (result == null)
                return;
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            String type = "";
            float confidence = 0;

            // Select the most confidence type
            for (DetectedActivity da : detectedActivities) {
                if (da.getConfidence() > confidence) {
                    confidence = da.getConfidence();
                    type = getActivityString(
                            getApplicationContext(),
                            da.getType());
                }
            }
            String activityText = "" + type + " [" + String.valueOf(confidence) + "%]";
            Log.i(TAG, "Current Activity: " + activityText);
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(getResources().getString(R.string.Activity_Broadcast_Action));
            broadcastIntent.putExtra(getResources().getString(R.string.Activity_Recognition_Action_Text), activityText);
            sendBroadcast(broadcastIntent);
        }
    }

    public static String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
        }
    }
}
