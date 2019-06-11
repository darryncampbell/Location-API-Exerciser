package com.darryncampbell.locationapiexerciser;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.BeaconFence;
import com.google.android.gms.awareness.snapshot.BeaconStateResult;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.BeaconState;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.PlaceLikelihood;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Created by darry on 13/01/2017.
 */

public class AwarenessWrapper {

    public static final String TAG = "LOCATION API EXERCISER";
    static final List BEACON_TYPE_FILTERS = Arrays.asList(
            BeaconState.TypeFilter.with(
                    "steel-ridge-155411",
                    "farewell"),
            BeaconState.TypeFilter.with(
                    "steel-ridge-155411",
                    "greeting"));

    LocationUI ui;
    LocationServicesWrapper clientWrapper;
    Context context;

    public AwarenessWrapper(LocationUI ui, LocationServicesWrapper clientWrapper, Context context)
    {
        this.ui = ui;
        this.clientWrapper = clientWrapper;
        this.context = context;
    }

    private boolean clientConnected()
    {
        if (this.clientWrapper != null && clientWrapper.isConnected())
            return true;
        else
            return false;
    }

    private void TestAPI()
    {
        //Intent intent = new Intent(Awareness.FenceApi);
        //AwarenessFence beaconFence = BeaconFence.near(BEACON_TYPE_FILTERS);
    }

    public void getSnapshot() {
        final TextView txtAwarenessPosition = (TextView) ((Activity) context).findViewById(R.id.txtAwarenessPosition);
        final TextView txtAwarenessActivity = (TextView) ((Activity) context).findViewById(R.id.txtAwarenessActivity);
        final TextView txtAwarenessPlaces = (TextView) ((Activity) context).findViewById(R.id.txtAwarenessPlaces);
        final TextView txtAwarenessBeacons = (TextView) ((Activity) context).findViewById(R.id.txtAwarenessBeacons);
        final TextView txtAwarenessWeather = (TextView) ((Activity) context).findViewById(R.id.txtAwarenessWeather);
        final TextView txtAwarenessHeadphones = (TextView) ((Activity) context).findViewById(R.id.txtAwarenessHeadphones);

        if (!clientConnected())
            return;

        ui.UpdateUIWithAwareness(txtAwarenessPosition, "Pending...");
        ui.UpdateUIWithAwareness(txtAwarenessActivity, "Pending...");
        ui.UpdateUIWithAwareness(txtAwarenessPlaces, "Pending...");
        ui.UpdateUIWithAwareness(txtAwarenessWeather, "Pending...");
        ui.UpdateUIWithAwareness(txtAwarenessHeadphones, "Pending...");

        Awareness.SnapshotApi.getDetectedActivity(clientWrapper.getGoogleApiClient())
                .setResultCallback(new ResultCallback<DetectedActivityResult>() {
                    @Override
                    public void onResult(DetectedActivityResult detectedActivityResult) {
                        if (!detectedActivityResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Awareness could not get the current activity. " + CommonStatusCodes.getStatusCodeString(detectedActivityResult.getStatus().getStatusCode()));
                            ui.UpdateUIWithAwareness(txtAwarenessActivity, "Error retrieving activity");
                        }
                        else {
                            ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
                            DetectedActivity probableActivity = ar.getMostProbableActivity();
                            Log.i(TAG, "Awareness: " + probableActivity.toString());
                            ui.UpdateUIWithAwareness(txtAwarenessActivity, "" +
                                    ActivityRecognitionIntentService.getActivityString(context, probableActivity.getType()) + " [" + probableActivity.getConfidence() + "%]");
                        }
                    }
                });


        //  BEACONS
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ui.UpdateUIWithAwareness(txtAwarenessBeacons, "Searching...");
        Awareness.SnapshotApi.getBeaconState(clientWrapper.getGoogleApiClient(), BEACON_TYPE_FILTERS)
                .setResultCallback(new ResultCallback<BeaconStateResult>() {
                    @Override
                    public void onResult(@NonNull BeaconStateResult beaconStateResult) {
                        if (!beaconStateResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not get beacon state.");
                            ui.UpdateUIWithAwareness(txtAwarenessBeacons, "Error retrieving beacon");
                        }
                        else
                        {
                            BeaconState beaconState = beaconStateResult.getBeaconState();
                            //int status = beaconStateResult.getStatus().getStatusCode();
                            //String message = beaconStateResult.getStatus().getStatusMessage();
                            if (beaconState != null)
                            {
                                List<BeaconState.BeaconInfo> beaconInfoList = beaconState.getBeaconInfo();
                                String beaconStateToDisplay = "";
                                for (int i = 0; i < beaconInfoList.size(); i++)
                                {
                                    BeaconState.BeaconInfo bi = beaconInfoList.get(i);
                                    beaconStateToDisplay += new String(bi.getContent());
                                    if (i < beaconInfoList.size() - 1)
                                        beaconStateToDisplay += "\n";
                                }
                                ui.UpdateUIWithAwareness(txtAwarenessBeacons, beaconStateToDisplay);
                                Log.i(TAG, "Awareness: Read Beacon State: " + beaconState.toString());
                            }
                            else
                            {
                                ui.UpdateUIWithAwareness(txtAwarenessBeacons, "None found");
                            }
                        }
                    }
                });

        //  HEADPHONE STATE
        Awareness.SnapshotApi.getHeadphoneState(clientWrapper.getGoogleApiClient())
                .setResultCallback(new ResultCallback<HeadphoneStateResult>() {
                    @Override
                    public void onResult(@NonNull HeadphoneStateResult headphoneStateResult) {
                        if (!headphoneStateResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not get headphone state.");
                            ui.UpdateUIWithAwareness(txtAwarenessHeadphones, "Error retrieving headphones");
                        }
                        else
                        {
                            HeadphoneState headphoneState = headphoneStateResult.getHeadphoneState();
                            if (headphoneState.getState() == HeadphoneState.PLUGGED_IN) {
                                ui.UpdateUIWithAwareness(txtAwarenessHeadphones, "Plugged in");
                                Log.i(TAG, "Awareness: Headphones are plugged in.");
                            } else {
                                ui.UpdateUIWithAwareness(txtAwarenessHeadphones, "Unplugged");
                                Log.i(TAG, "Awareness: Headphones are NOT plugged in.");
                            }
                        }
                    }
                });

        //  LOCATION
        Awareness.SnapshotApi.getLocation(clientWrapper.getGoogleApiClient())
                .setResultCallback(new ResultCallback<LocationResult>() {
                    @Override
                    public void onResult(@NonNull LocationResult locationResult) {
                        if (!locationResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not get location.");
                            ui.UpdateUIWithAwareness(txtAwarenessPosition, "Error retrieving location");
                        }
                        else
                        {
                            Location location = locationResult.getLocation();
                            Log.i(TAG, "Awareness: Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                            String latitude = new DecimalFormat("#.#######").format(location.getLatitude());
                            String longitude = new DecimalFormat("#.#######").format(location.getLongitude());
                            String accuracy = new DecimalFormat("#.#######").format(location.getAccuracy());
                            ui.UpdateUIWithAwareness(txtAwarenessPosition, "Lat: " + latitude + "\nLong: " + longitude + "\nAccuracy: " + accuracy);
                        }
                    }
                });

        //  PLACES
        Awareness.SnapshotApi.getPlaces(clientWrapper.getGoogleApiClient())
                .setResultCallback(new ResultCallback<PlacesResult>() {
                    @Override
                    public void onResult(@NonNull PlacesResult placesResult) {
                        if (!placesResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not get places.  Need to enable billing for this application?");
                            ui.UpdateUIWithAwareness(txtAwarenessPlaces, "Error retrieving places");
                        }
                        else
                        {
                            List<PlaceLikelihood> placeLikelihoodList = placesResult.getPlaceLikelihoods();
                            // Show the top 5 possible location results.
                            String awarenessPlacesToDisplay = "";
                            for (int i = 0; i < 5; i++) {
                                if (placeLikelihoodList != null && placeLikelihoodList.size() > i) {
                                    PlaceLikelihood p = placeLikelihoodList.get(i);
                                    Log.i(TAG, "Awareness (place): " + p.getPlace().getName().toString() + ", likelihood: " + p.getLikelihood());
                                    if (i != 0)
                                        awarenessPlacesToDisplay += "\n";
                                    awarenessPlacesToDisplay += p.getPlace().getName().toString() + " [" + p.getLikelihood() + "%]";
                                }
                            }
                            if (awarenessPlacesToDisplay.equals(""))
                                ui.UpdateUIWithAwareness(txtAwarenessPlaces, "None found");
                            else
                                ui.UpdateUIWithAwareness(txtAwarenessPlaces, awarenessPlacesToDisplay);
                        }
                    }
                });

        //  WEATHER
        Awareness.SnapshotApi.getWeather(clientWrapper.getGoogleApiClient())
                .setResultCallback(new ResultCallback<WeatherResult>() {
                    @Override
                    public void onResult(@NonNull WeatherResult weatherResult) {
                        if (!weatherResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not get weather.");
                            ui.UpdateUIWithAwareness(txtAwarenessWeather, "Error retrieving weather");
                        }
                        else
                        {
                            Weather weather = weatherResult.getWeather();
                            Log.i(TAG, "Awareness: Weather: " + weather);
                            String weatherToDisplay = "";
                            int[] conditions = weather.getConditions();
                            for (int i = 0; i < conditions.length; i++)
                            {
                                weatherToDisplay += getWeatherCondition(conditions[i]) + "\n";
                            }
                            weatherToDisplay += weather.getTemperature(Weather.FAHRENHEIT) + "f";
                            ui.UpdateUIWithAwareness(txtAwarenessWeather, weatherToDisplay);
                        }
                    }
                });
    }

    public static String getWeatherCondition(int detectedWeather) {
        switch (detectedWeather) {
            case Weather.CONDITION_CLEAR:
                return "Clear";
            case Weather.CONDITION_CLOUDY:
                return "Cloudy";
            case Weather.CONDITION_FOGGY:
                return "Foggy";
            case Weather.CONDITION_HAZY:
                return "Hazy";
            case Weather.CONDITION_ICY:
                return "Icy";
            case Weather.CONDITION_RAINY:
                return "Rainy";
            case Weather.CONDITION_SNOWY:
                return "Snowy";
            case Weather.CONDITION_STORMY:
                return "Stormy";
            case Weather.CONDITION_WINDY:
                return "Windy";
            case Weather.CONDITION_UNKNOWN:
            default:
                return "Unknown";
        }
    }
}
