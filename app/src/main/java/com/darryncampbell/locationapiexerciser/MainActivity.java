package com.darryncampbell.locationapiexerciser;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.os.SystemClock.elapsedRealtimeNanos;
import static com.darryncampbell.locationapiexerciser.R.id.radioProviderCustom;
import static com.darryncampbell.locationapiexerciser.R.id.radioProviderGps;
import static com.darryncampbell.locationapiexerciser.R.id.radioProviderNetwork;
import static com.darryncampbell.locationapiexerciser.R.id.radioTrackDataCanada;
import static com.darryncampbell.locationapiexerciser.R.id.radioTrackDataSpain;
import static com.darryncampbell.locationapiexerciser.R.id.radioTrackDataTasmania;
import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity implements LocationUI {

    public static final String TAG = "LOCATION API EXERCISER";
    public static final int LOCATION_SETTINGS_PERMISSION_REQUEST = 1;
    boolean locationPermission = false;
    String customProviderName;
    LocationManagerWrapper locationManagerWrapper = null;
    CustomProviderWrapper customProviderWrapper = null;
    GMapsGeolocationAPIWrapper gMapsGeolocationAPIWrapper = null;
    public AddressResultReceiver mResultReceiver;
    Location bestLocationForGeofenceWithLocationManager = null;
    Location bestLocationForGeofenceWithLocationServices = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        locationManagerWrapper = new LocationManagerWrapper(this, this, customProviderName);
        customProviderWrapper = new CustomProviderWrapper(this, this);
        gMapsGeolocationAPIWrapper = new GMapsGeolocationAPIWrapper(this, this);
        customProviderName = "";
        mResultReceiver = new AddressResultReceiver(new Handler());
        Button settingsRequestHighAccuracy = (Button) findViewById(R.id.btnLocationSettingsForHighAccuracy);
        Button settingsRequestBle = (Button) findViewById(R.id.btnLocationSettingsForBle);
        final Button btnAwarenessGetSnapshot = (Button) findViewById(R.id.btnAwarenessGetSnapshot);
        final Button btnGeofenceForLocationManagerStart = (Button) findViewById(R.id.btnGeofencingViaLocationManagerStart);
        final Button btnGeofenceForLocationManagerStop = (Button) findViewById(R.id.btnGeofencingViaLocationManagerStop);
        final Button btnGeofenceForLocationServicesStart = (Button) findViewById(R.id.btnGeofencingViaLocationServicesStart);
        final Button btnGeofenceForLocationServicesStop = (Button) findViewById(R.id.btnGeofencingViaLocationServicesStop);
        btnGeofenceForLocationManagerStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Start Location Manager Geofence
                if (bestLocationForGeofenceWithLocationManager != null) {
                    btnGeofenceForLocationManagerStart.setEnabled(false);
                    btnGeofenceForLocationManagerStop.setEnabled(true);
                    locationManagerWrapper.startGeofence(bestLocationForGeofenceWithLocationManager);
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to set Geofence as no location available", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnGeofenceForLocationManagerStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Stop Location Manager Geofence
                btnGeofenceForLocationManagerStart.setEnabled(true);
                btnGeofenceForLocationManagerStop.setEnabled(false);
                locationManagerWrapper.stopGeofence();
            }
        });
        btnGeofenceForLocationServicesStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Start Location Services Geofence
                if (bestLocationForGeofenceWithLocationServices != null) {
                    btnGeofenceForLocationServicesStart.setEnabled(false);
                    btnGeofenceForLocationServicesStop.setEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to set Geofence as no location available", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnGeofenceForLocationServicesStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Stop Location Services Geofence
            }
        });
        final Button btnScanForAPs = (Button) findViewById(R.id.btnGoogleMapsGeolocationFetch);
        btnScanForAPs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Scan for APs, look up the AP location and display it
                ClearGMapsUIFields();
                gMapsGeolocationAPIWrapper.ScanForAPsAndReportPosition();
            }
        });
        final SeekBar customProviderIntervalSeek = (SeekBar) findViewById(R.id.seekCustomProviderInterval);
        customProviderIntervalSeek.setMax(59000);
        customProviderIntervalSeek.setProgress(0);
        final TextView customProviderIntervalText = (TextView) findViewById(R.id.txtCustomProviderInterval);
        customProviderIntervalText.setText("" + (customProviderIntervalSeek.getProgress() + 1000));
        customProviderIntervalSeek.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                        progressValue = progressValue / 1000;
                        progressValue = progressValue * 1000;
                        customProviderIntervalText.setText("" + (progressValue + 1000));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
        Button customProviderStart = (Button) findViewById(R.id.btnCustomProviderStart);
        Button customProviderStop = (Button) findViewById(R.id.btnCustomProviderStop);
        customProviderStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioGroup radioProviderGroup = (RadioGroup) findViewById(R.id.radioProvider);
                RadioGroup radioTrackGroup = (RadioGroup) findViewById(R.id.radioTrackData);
                int selectedProviderId = radioProviderGroup.getCheckedRadioButtonId();
                int selectedTrackId = radioTrackGroup.getCheckedRadioButtonId();
                String selectedProvider = "";
                String selectedTrack = "";
                switch (selectedProviderId) {
                    case radioProviderGps:
                        selectedProvider = LocationManager.GPS_PROVIDER;
                        break;
                    case radioProviderNetwork:
                        selectedProvider = LocationManager.NETWORK_PROVIDER;
                        break;
                    case radioProviderCustom:
                        selectedProvider = getString(R.string.CUSTOM_PROVIDER);
                        break;
                }
                switch (selectedTrackId) {
                    case radioTrackDataCanada:
                        selectedTrack = "canada_highway";
                        break;
                    case radioTrackDataTasmania:
                        selectedTrack = "tasmania";
                        break;
                    case radioTrackDataSpain:
                        selectedTrack = "spain";
                        break;
                }
                int chosenInterval = (customProviderIntervalSeek.getProgress() + 1000);
                customProviderWrapper.userStarted(selectedProvider, selectedTrack, chosenInterval);
            }
        });
        customProviderStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customProviderWrapper.userStopped();
            }
        });

        //  Register the broadcast receiver for activity recognition
        IntentFilter filter = new IntentFilter();
        filter.addAction(this.getResources().getString(R.string.Activity_Broadcast_Action));
        registerReceiver(myBroadcastReceiver, filter);


/*        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adhocTesting();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS_PERMISSION_REQUEST) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i(TAG, "User agreed to make required location settings changes.");
                    Toast.makeText(this, "User agreed to change location settings", Toast.LENGTH_LONG).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    Toast.makeText(this, "User did NOT agree to change location settings", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onStart() {
        super.onStart();
        //  Request Permission for fine location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            //  Permissions had previously been granted
            locationManagerWrapper.populateUiStatus();
            locationManagerWrapper.startAospLocation();
            customProviderWrapper.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManagerWrapper.stopAospLocation();
        locationManagerWrapper.stopCustomProviderListener();
        customProviderWrapper.onStop();
        TextView activityRecognitionTxt = (TextView) findViewById(R.id.txtActivityRecognition);
        activityRecognitionTxt.setText("Unregistered");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Location Fine permission granted");
                    locationPermission = true;
                    locationManagerWrapper.populateUiStatus();
                    locationManagerWrapper.startAospLocation();
                } else {
                    //  Permission denied
                    Log.e(TAG, "Fine location information is required to run this application");
                    Toast.makeText(this, "Fine location information is required to run this application", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            adhocTesting();
            return true;
        }
        else if (id == R.id.action_about)
        {
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                Toast.makeText(this, "Version: " + pInfo.versionName, Toast.LENGTH_LONG).show();
            } catch (PackageManager.NameNotFoundException e) {
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void UpdateUIWithFusedLocation(Location location) {
        final TextView txtFusedLatitude = (TextView) findViewById(R.id.txtFusedLatitude);
        final TextView txtFusedLongitude = (TextView) findViewById(R.id.txtFusedLongitude);
        final TextView txtFusedAccuracy = (TextView) findViewById(R.id.txtFusedAccuracy);
        final TextView txtFusedAddress = (TextView) findViewById(R.id.txtFusedAddress);
        UpdateUIWithLocation(txtFusedLatitude, txtFusedLongitude, txtFusedAccuracy, txtFusedAddress, location);
        if (location != null) {
            UpdateGeofenceLocation(true, location);
        }
    }

    public void UpdateUIWithLocation(TextView latitude, TextView longitude, TextView accuracy, TextView address, Location theLocation) {
        if (theLocation == null) {
            latitude.setText("Unavailable");
            longitude.setText("Unavailable");
            accuracy.setText("Unavailable");
            if (address != null)
                address.setText("Unavailable");
        } else {
            latitude.setText(new DecimalFormat("#.#######").format(theLocation.getLatitude()));
            longitude.setText(new DecimalFormat("#.#######").format(theLocation.getLongitude()));
            //long elapsedNanos = theLocation.getElapsedRealtimeNanos();
            //long nanosFromSystemClock = elapsedRealtimeNanos();
            //long age = nanosFromSystemClock - elapsedNanos;
            //long ageInSeconds = age / 1000000000;
            String szAccuracy;
            szAccuracy = new DecimalFormat("#.#").format(theLocation.getAccuracy());
            String szLastUpdateTime;
            long millis = currentTimeMillis();
            Date date = new Date(millis);
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss");  //  HH:mm:ss:SSS
            szLastUpdateTime = formatter.format(date);
            accuracy.setText(szAccuracy + " @ " + szLastUpdateTime);
            if (address != null)
                convertLocationToAddress(theLocation);
            if (theLocation.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER) ||
                    theLocation.getProvider().equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                if (bestLocationForGeofenceWithLocationManager == null)
                    UpdateGeofenceLocation(false, theLocation);
                else {
                    if (theLocation.getAccuracy() < bestLocationForGeofenceWithLocationManager.getAccuracy())
                        UpdateGeofenceLocation(false, theLocation);
                }
            }

            //  Put up a notification on Oreo devices to test background location
            /*
            String notificationLatitude = (new DecimalFormat("#.##").format(theLocation.getLatitude()));
            String notificationLongitude = (new DecimalFormat("#.##").format(theLocation.getLongitude()));
            String notificationAccuracy = new DecimalFormat("#.#").format(theLocation.getAccuracy());
            String notificationLastUpdate = szLastUpdateTime;
            String notificationProvider = "Fused";
            int notificationId = 10;
            if (theLocation.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                notificationProvider = "GPS";
                notificationId = 11;
            }
            else if (theLocation.getProvider().equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                notificationProvider = "Network";
                notificationId = 12;
            }
            String locationNotificationText = "[" + notificationProvider + "]: " + notificationLatitude + " " + notificationLongitude +
                    " " + notificationAccuracy + " @" + notificationLastUpdate;

            GeofenceUtilities.sendNotification(locationNotificationText, this, notificationId);
            */
        }
    }

    public void UpdateUIApplicationServicesAvailable(int availabilityState) {
        TextView txtLocationServicesAvailable = (TextView) findViewById(R.id.txtlocationServicesAvailable);
        TextView activityRecognitionTxt = (TextView) findViewById(R.id.txtActivityRecognition);
        String noLocationServices = "No Loc. Services";  //  Should use resource!!
    }

    public void convertLocationToAddress(Location location) {
    }

    private void UpdateGeofenceLocation(Boolean isLocationServices, Location newLocation) {
        if (newLocation == null)
            return;

        TextView txtGeofenceLocationForLocationManager = (TextView) findViewById(R.id.txtGeofencingViaLocationManager);
        TextView txtGeofenceLocationForLocationServices = (TextView) findViewById(R.id.txtGeofencingViaLocationServices);
        if (isLocationServices) {
            bestLocationForGeofenceWithLocationServices = newLocation;
            txtGeofenceLocationForLocationServices.setText("" + newLocation.getProvider() + " position");
        } else {
            bestLocationForGeofenceWithLocationManager = newLocation;
            txtGeofenceLocationForLocationManager.setText("" + newLocation.getProvider() + " position");
        }
    }

    @Override
    public void UpdateUIWithCustomProviderEnabled(Boolean isEnabled) {
        TextView customProviderStatusTxt = (TextView) findViewById(R.id.txtCustomProviderStatus);
        TextView customProviderLongitude = (TextView) findViewById(R.id.txtCustomLongitude);
        TextView customProviderLatitude = (TextView) findViewById(R.id.txtCustomLatitude);
        TextView customProviderAccuracy = (TextView) findViewById(R.id.txtCustomAccuracy);
        Button customProviderStart = (Button) findViewById(R.id.btnCustomProviderStart);
        Button customProviderStop = (Button) findViewById(R.id.btnCustomProviderStop);
        RadioButton radioGpsProvider = (RadioButton) findViewById(R.id.radioProviderGps);
        RadioButton radioNetworkProvider = (RadioButton) findViewById(R.id.radioProviderNetwork);
        RadioButton radioCustomProvider = (RadioButton) findViewById(R.id.radioProviderCustom);
        RadioButton radioTrackDataCanada = (RadioButton) findViewById(R.id.radioTrackDataCanada);
        RadioButton radioTrackDataTasmania = (RadioButton) findViewById(R.id.radioTrackDataTasmania);
        RadioButton radioTrackDataSpain = (RadioButton) findViewById(R.id.radioTrackDataSpain);
        SeekBar customProviderIntervalSeek = (SeekBar) findViewById(R.id.seekCustomProviderInterval);

        if (isEnabled) {
            customProviderStatusTxt.setText("Enabled");
        } else {
            customProviderStatusTxt.setText("Disabled");
            customProviderLongitude.setText("Unavailable");
            customProviderLatitude.setText("Unavailable");
            customProviderAccuracy.setText("Unavailable");
            customProviderStart.setEnabled(false);
            customProviderStop.setEnabled(false);
            radioGpsProvider.setEnabled(false);
            radioNetworkProvider.setEnabled(false);
            radioCustomProvider.setEnabled(false);
            radioTrackDataCanada.setEnabled(false);
            radioTrackDataTasmania.setEnabled(false);
            radioTrackDataSpain.setEnabled(false);
            customProviderIntervalSeek.setEnabled(false);
        }
    }

    @Override
    public void UpdateUIWithCustomProviderRunning(Boolean isRunning) {
        Button customProviderStart = (Button) findViewById(R.id.btnCustomProviderStart);
        Button customProviderStop = (Button) findViewById(R.id.btnCustomProviderStop);
        TextView customProviderStatusTxt = (TextView) findViewById(R.id.txtCustomProviderStatus);
        RadioButton radioGpsProvider = (RadioButton) findViewById(R.id.radioProviderGps);
        RadioButton radioNetworkProvider = (RadioButton) findViewById(R.id.radioProviderNetwork);
        RadioButton radioCustomProvider = (RadioButton) findViewById(R.id.radioProviderCustom);
        RadioButton radioTrackDataCanada = (RadioButton) findViewById(R.id.radioTrackDataCanada);
        RadioButton radioTrackDataTasmania = (RadioButton) findViewById(R.id.radioTrackDataTasmania);
        RadioButton radioTrackDataSpain = (RadioButton) findViewById(R.id.radioTrackDataSpain);
        SeekBar customProviderIntervalSeek = (SeekBar) findViewById(R.id.seekCustomProviderInterval);

        if (isRunning) {
            Log.i(TAG, "Custom provider has reported its state as running");
            customProviderStart.setEnabled(false);
            customProviderStop.setEnabled(true);
            radioGpsProvider.setEnabled(false);
            radioNetworkProvider.setEnabled(false);
            radioCustomProvider.setEnabled(false);
            radioTrackDataCanada.setEnabled(false);
            radioTrackDataTasmania.setEnabled(false);
            radioTrackDataSpain.setEnabled(false);
            customProviderIntervalSeek.setEnabled(false);
            customProviderStatusTxt.setText("Running");
            locationManagerWrapper.startCustomProviderListener();
        } else {
            Log.i(TAG, "Custom provider has reported its state as stopped");
            customProviderStart.setEnabled(true);
            customProviderStop.setEnabled(false);
            radioTrackDataCanada.setEnabled(true);
            radioTrackDataTasmania.setEnabled(true);
            radioTrackDataSpain.setEnabled(true);
            customProviderIntervalSeek.setEnabled(true);
            customProviderStatusTxt.setText("Stopped");
            locationManagerWrapper.stopCustomProviderListener();
        }
    }

    @Override
    public void UpdateUIWithAwareness(TextView awarenessTextView, String text)
    {
        if (awarenessTextView != null)
            awarenessTextView.setText(text);
    }

    private void UpdateUIWithAwarenessAvailable(boolean available)
    {
        TextView txtAwarenessPosition = (TextView) findViewById(R.id.txtAwarenessPosition);
        TextView txtAwarenessActivity = (TextView) findViewById(R.id.txtAwarenessActivity);
        TextView txtAwarenessPlaces = (TextView) findViewById(R.id.txtAwarenessPlaces);
        TextView txtAwarenessBeacons = (TextView) findViewById(R.id.txtAwarenessBeacons);
        TextView txtAwarenessWeather = (TextView) findViewById(R.id.txtAwarenessWeather);
        TextView txtAwarenessHeadphones = (TextView) findViewById(R.id.txtAwarenessHeadphones);

        if (available)
        {
            txtAwarenessPosition.setText("Get Snapshot");
            txtAwarenessActivity.setText("Get Snapshot");
            txtAwarenessPlaces.setText("Get Snapshot");
            txtAwarenessBeacons.setText("Get Snapshot");
            txtAwarenessWeather.setText("Get Snapshot");
            txtAwarenessHeadphones.setText("Get Snapshot");
        }
    }

    public void UpdateUIWithGoogleMapsAPILocation(Location location)
    {
        TextView txtGMapsGeolocationApiLatitude = (TextView) findViewById(R.id.txtMapsGeolocationAPILatitude);
        TextView txtGMapsGeolocationApiLongitude = (TextView) findViewById(R.id.txtMapsGeolocationAPILongitude);
        TextView txtGMapsGeolocationApiAccuracy = (TextView) findViewById(R.id.txtMapsGeolocationAPIAccuracy);
        TextView txtGMapsGeolocationApiAddress = (TextView) findViewById(R.id.txtMapsGeolocationAPIAddress);
        if (location != null)
        {
            UpdateUIWithLocation(txtGMapsGeolocationApiLatitude, txtGMapsGeolocationApiLongitude,
                    txtGMapsGeolocationApiAccuracy, txtGMapsGeolocationApiAddress, location);
        }
        else
        {
            txtGMapsGeolocationApiLatitude.setText("Lookup Error");
            txtGMapsGeolocationApiLongitude.setText("Lookup Error");
            txtGMapsGeolocationApiAccuracy.setText("Lookup Error");
            txtGMapsGeolocationApiAddress.setText("Lookup Error");
        }
        gMapsGeolocationAPIWrapper.UnregisterReceiver();
    }

    public void UpdateUIWithAPScanResult(ScanResult detectedAP)
    {
        TextView txtAPScanResults = (TextView) findViewById(R.id.txtScanResultsAps);
        if (detectedAP != null)
        {
            String apResultAsString =  detectedAP.SSID + ", " + detectedAP.level + "dBm.  " + detectedAP.BSSID;
            txtAPScanResults.setText(txtAPScanResults.getText() + "\n" + apResultAsString);
        }
        else
        {
            txtAPScanResults.setText("AP Scan not available");
        }
    }

    private void ClearGMapsUIFields()
    {
        TextView txtGMapsGeolocationApiLatitude = (TextView) findViewById(R.id.txtMapsGeolocationAPILatitude);
        TextView txtGMapsGeolocationApiLongitude = (TextView) findViewById(R.id.txtMapsGeolocationAPILongitude);
        TextView txtGMapsGeolocationApiAccuracy = (TextView) findViewById(R.id.txtMapsGeolocationAPIAccuracy);
        TextView txtGMapsGeolocationApiAddress = (TextView) findViewById(R.id.txtMapsGeolocationAPIAddress);
        TextView txtAPScanResults = (TextView) findViewById(R.id.txtScanResultsAps);
        txtGMapsGeolocationApiLatitude.setText("Awaiting Scan");
        txtGMapsGeolocationApiLongitude.setText("Awaiting Scan");
        txtGMapsGeolocationApiAccuracy.setText("Awaiting Scan");
        txtGMapsGeolocationApiAddress.setText("Awaiting Scan");
        txtAPScanResults.setText("Found APs will be shown here");
    }


    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            final TextView txtGPSAddress = (TextView) findViewById(R.id.txtGPSAddress);
            final TextView txtNetworkAddress = (TextView) findViewById(R.id.txtNetworkAddress);
            final TextView txtFusedAddress = (TextView) findViewById(R.id.txtFusedAddress);
            final TextView txtGMapsAddress = (TextView) findViewById(R.id.txtMapsGeolocationAPIAddress);

            // Display the address string
            // or an error message sent from the intent service.
        }
    }

    //  Broadcast receiver for the address
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionText = intent.getStringExtra(getResources().getString(R.string.Activity_Recognition_Action_Text));
            TextView activityRecognitionTxt = (TextView) findViewById(R.id.txtActivityRecognition);
            activityRecognitionTxt.setText(actionText);
        }
    };

    public void adhocTesting() {

    }
}
