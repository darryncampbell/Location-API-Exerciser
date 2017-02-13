package com.darryncampbell.locationapiexerciser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by darry on 01/02/2017.
 */

public class GMapsGeolocationAPIWrapper {

    public static final String TAG = "LOCATION API EXERCISER";
    Context context;
    LocationUI ui;
    private final String GEOLOCATE_URL = "https://www.googleapis.com/geolocation/v1/geolocate?key=";
    private final String GEOLOCATE_KEY = "AIzaSyAQytQ9-TjPh5QemTd4RNtWMcBZ7khZbIY";
    public static final String GEOLOCATE_PROVIDER = "GEOLOCATE";

    public GMapsGeolocationAPIWrapper(LocationUI ui, Context context) {
        this.ui = ui;
        this.context = context;
    }

    public void ScanForAPsAndReportPosition()
    {
        IntentFilter wifiScanFilter = new IntentFilter();
        wifiScanFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(mWifiScanReceiver, wifiScanFilter);
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
    }

    public void UnregisterReceiver()
    {
        try
        {
            context.unregisterReceiver(mWifiScanReceiver);
        }
        catch (IllegalArgumentException e)
        {
            //  On my TC55 this is thrown
            Log.i(TAG, "Wifi Scanner receiver not registered");
        }
    }

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction() == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                List<ScanResult> apList = wifiManager.getScanResults();
                if (apList.size() == 0)
                {
                    //  No APs were found
                    ui.UpdateUIWithAPScanResult(null);
                    ui.UpdateUIWithGoogleMapsAPILocation(null);
                    return;
                }
                JSONObject params = new JSONObject();
                try
                {
                    //  Specifies whether to fall back to IP geolocation if wifi and cell tower
                    // signals are not available. Note that the IP address in the request header
                    // may not be the IP of the device. Defaults to true. Set considerIp to false
                    // to disable fall back.
                    params.put("considerIp", false);
                    JSONArray wifiAccessPoints = new JSONArray();
                    for (int i = 0; i < apList.size(); i++)
                    {
                        JSONObject macInfo = new JSONObject();
                        macInfo.put("macAddress", apList.get(i).BSSID);
                        macInfo.put("signalStrength", apList.get(i).level);
                        wifiAccessPoints.put(macInfo);
                        Log.i(TAG, "AP: " + apList.get(i).SSID + ", " + apList.get(i).level + "dBm.  " + apList.get(i).BSSID);
                        ui.UpdateUIWithAPScanResult(apList.get(i));
                    }
                    params.put("wifiAccessPoints", wifiAccessPoints);

                } catch (JSONException e) {
                    ui.UpdateUIWithAPScanResult(null);
                    ui.UpdateUIWithGoogleMapsAPILocation(null);
                    Log.e(TAG, "Error adding found APs to JSON object");
                    return;
                }

                String url = GEOLOCATE_URL + GEOLOCATE_KEY;

                JsonObjectRequest jsonRequest = new JsonObjectRequest
                        (Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    float accuracy = Float.parseFloat(response.getString("accuracy"));
                                    response = response.getJSONObject("location");
                                    Double latitude = response.getDouble("lat");
                                    Double longitude = response.getDouble("lng");
                                    Location location = new Location(GEOLOCATE_PROVIDER);
                                    location.setLatitude(latitude);
                                    location.setLongitude(longitude);
                                    location.setAccuracy(accuracy);
                                    Log.i(TAG, "From Google Maps Geolocate API: Lat: " + latitude + ", Long: " + longitude + ", accuracy: " + accuracy);
                                    ui.UpdateUIWithGoogleMapsAPILocation(location);
                                } catch (JSONException e) {
                                    ui.UpdateUIWithGoogleMapsAPILocation(null);
                                    Log.e(TAG, "Error parsing response from Geolocate API: " + e.getMessage());
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                ui.UpdateUIWithGoogleMapsAPILocation(null);
                                Log.e(TAG, "Error with Geolocate API request: " + error.toString());
                            }
                        });

                Volley.newRequestQueue(context).add(jsonRequest);
            }
        }
    };

}
