package com.darryncampbell.locationapiexerciser;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity implements LocationUI {

    public static final String TAG = "LOCATION API EXERCISER";
    boolean locationPermission = false;
    String customProviderName;
    Location customLocation;
    LocationServicesWrapper locationServicesWrapper = null;
    LocationManagerWrapper locationManagerWrapper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        locationManagerWrapper = new LocationManagerWrapper(this, this, customProviderName);
        locationServicesWrapper = new LocationServicesWrapper(this, this);
        customProviderName = "";

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
        UpdateUIWithLocation(txtFusedLatitude, txtFusedLongitude, txtFusedAccuracy, location);
    }

    public void UpdateUIWithLocation(TextView latitude, TextView longitude, TextView accuracy, Location theLocation)
    {
        if (theLocation == null)
        {
            latitude.setText("Unavailable");
            longitude.setText("Unavailable");
            accuracy.setText("Unavailable");
        }
        else
        {
            latitude.setText(new DecimalFormat("#.#######").format(theLocation.getLatitude()));
            longitude.setText(new DecimalFormat("#.#######").format(theLocation.getLongitude()));
            accuracy.setText(new DecimalFormat("#.#######").format(theLocation.getAccuracy()));
        }
    }

    public void UpdateUIApplicationServicesAvailable(String isAvailable) {
        TextView txtLocationServicesAvailable = (TextView) findViewById(R.id.txtlocationServicesAvailable);
        txtLocationServicesAvailable.setText(isAvailable);
    }
}
