package com.darryncampbell.locationapiexerciser;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.DecimalFormat;

import static com.darryncampbell.locationapiexerciser.R.styleable.View;


public class MainActivity extends AppCompatActivity implements LocationUI {

    public static final String TAG = "LOCATION API EXERCISER";
    public static final int LOCATION_SETTINGS_PERMISSION_REQUEST = 1;
    boolean locationPermission = false;
    String customProviderName;
    Location customLocation;
    LocationServicesWrapper locationServicesWrapper = null;
    LocationManagerWrapper locationManagerWrapper = null;
    public AddressResultReceiver mResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        locationManagerWrapper = new LocationManagerWrapper(this, this, customProviderName);
        locationServicesWrapper = new LocationServicesWrapper(this, this);
        customProviderName = "";
        mResultReceiver = new AddressResultReceiver(new Handler());
        Button settingsRequestHighAccuracy = (Button)findViewById(R.id.btnLocationSettingsForHighAccuracy);
        Button settingsRequestBle = (Button)findViewById(R.id.btnLocationSettingsForBle);
        settingsRequestHighAccuracy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationServicesWrapper.GetLocationSettings(MainActivity.this, false);
            }
        });
        settingsRequestBle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationServicesWrapper.GetLocationSettings(MainActivity.this, true);
            }
        });

        //  Register the broadcast receiver for activity recognition
        IntentFilter filter = new IntentFilter();
        filter.addAction(this.getResources().getString(R.string.Activity_Broadcast_Action));
        registerReceiver(myBroadcastReceiver, filter);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS_PERMISSION_REQUEST)
        {
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
            locationServicesWrapper.onStart();
            locationManagerWrapper.startAospLocation();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        locationServicesWrapper.onStop();
        locationManagerWrapper.stopAospLocation();
    }

    @Override
    protected void onDestroy()
    {
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
                    locationServicesWrapper.initializeAndConnect();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public void UpdateUIWithFusedLocation(Location location)
    {
        final TextView txtFusedLatitude = (TextView) findViewById(R.id.txtFusedLatitude);
        final TextView txtFusedLongitude = (TextView) findViewById(R.id.txtFusedLongitude);
        final TextView txtFusedAccuracy = (TextView) findViewById(R.id.txtFusedAccuracy);
        final TextView txtFusedAddress = (TextView) findViewById(R.id.txtFusedAddress);
        UpdateUIWithLocation(txtFusedLatitude, txtFusedLongitude, txtFusedAccuracy, txtFusedAddress, location);
    }

    public void UpdateUIWithLocation(TextView latitude, TextView longitude, TextView accuracy, TextView address, Location theLocation)
    {
        if (theLocation == null)
        {
            latitude.setText("Unavailable");
            longitude.setText("Unavailable");
            accuracy.setText("Unavailable");
            address.setText("Unavailable");
        }
        else
        {
            latitude.setText(new DecimalFormat("#.#######").format(theLocation.getLatitude()));
            longitude.setText(new DecimalFormat("#.#######").format(theLocation.getLongitude()));
            accuracy.setText(new DecimalFormat("#.#######").format(theLocation.getAccuracy()));
            convertLocationToAddress(theLocation);
        }
    }

    public void UpdateUIApplicationServicesAvailable(Boolean isAvailable) {
        TextView txtLocationServicesAvailable = (TextView) findViewById(R.id.txtlocationServicesAvailable);
        if (isAvailable)
        {
            txtLocationServicesAvailable.setText("Yes");
        }
        else
        {
            txtLocationServicesAvailable.setText("No");
            UpdateUIWithFusedLocation(null);
            TextView activityRecognitionTxt = (TextView)findViewById(R.id.txtActivityRecognition);
            activityRecognitionTxt.setText("No Location Services");
        }
    }

    public void convertLocationToAddress(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
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

            // Display the address string
            // or an error message sent from the intent service.
            String addressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
            String provider = resultData.getString(FetchAddressIntentService.Constants.LOCATION_PROVIDER);
            Log.i(TAG, "Received decoded address from " + provider + ": " + addressOutput);
            if (provider.equals(LocationManager.GPS_PROVIDER))
            {
                txtGPSAddress.setText(addressOutput);
            }
            else if (provider.equals(LocationManager.NETWORK_PROVIDER))
            {
                txtNetworkAddress.setText(addressOutput);
            }
            else if (provider.equalsIgnoreCase("fused"))
            {
                txtFusedAddress.setText(addressOutput);
            }
            else
            {
                //  Unrecognised source
                Log.e(TAG, "Unrecognised provider");
            }
        }
    }

    //  Broadcast receiver for the address
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionText = intent.getStringExtra(getResources().getString(R.string.Activity_Recognition_Action_Text));
            TextView activityRecognitionTxt = (TextView)findViewById(R.id.txtActivityRecognition);
            activityRecognitionTxt.setText(actionText);
        }
    };

}
