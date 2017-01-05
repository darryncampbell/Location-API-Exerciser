package com.darryncampbell.locationapiexerciser;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchAddressIntentService extends IntentService {

    public static final String TAG = "LOCATION API EXERCISER";
    protected ResultReceiver mReceiver;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    //  Intents will be received either as a request to resolve the addresses from the GeoCode lookup or
    //  as activity recognition returned from the activity recognizer
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
            String errorMessage = "";

            Location location = intent.getParcelableExtra(
                    Constants.LOCATION_DATA_EXTRA);
            if (location != null)
            {
                //  Only Geocoded addresses will have a location
                String provider = location.getProvider();
                if (!Geocoder.isPresent()) {
                    deliverResultToReceiver(Constants.FAILURE_RESULT, provider, "No Location Services");
                    return;
                }
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                List<Address> addresses = null;

                try {
                    addresses = geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            // In this sample, get just a single address.
                            1);
                } catch (IOException ioException) {
                    // Catch network or other I/O problems.
                    errorMessage = getString(R.string.service_not_available);
                    Log.e(TAG, errorMessage, ioException);
                } catch (IllegalArgumentException illegalArgumentException) {
                    // Catch invalid latitude or longitude values.
                    errorMessage = getString(R.string.invalid_lat_long_used);
                    Log.e(TAG, errorMessage + ". " +
                            "Latitude = " + location.getLatitude() +
                            ", Longitude = " +
                            location.getLongitude(), illegalArgumentException);
                }

                // Handle case where no address was found.
                if (addresses == null || addresses.size() == 0) {
                    if (errorMessage.isEmpty()) {
                        errorMessage = getString(R.string.no_address_found);
                        Log.e(TAG, errorMessage);
                    }
                    deliverResultToReceiver(Constants.FAILURE_RESULT, provider, errorMessage);
                } else {
                    Address address = addresses.get(0);
                    ArrayList<String> addressFragments = new ArrayList<String>();

                    // Fetch the address lines using getAddressLine,
                    // join them, and send them to the thread.
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addressFragments.add(address.getAddressLine(i));
                    }
                    deliverResultToReceiver(Constants.SUCCESS_RESULT, provider,
                            TextUtils.join(System.getProperty("line.separator"),
                                    addressFragments));
                }
            }
            else
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
    }

    private void deliverResultToReceiver(int resultCode, String provider, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        bundle.putString(Constants.LOCATION_PROVIDER, provider);
        mReceiver.send(resultCode, bundle);
    }


    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
        public static final String LOCATION_PROVIDER = "RESULT_SOURCE_PROVIDER";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
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
