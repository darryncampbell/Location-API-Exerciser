package com.darryncampbell.locationapiexerciser;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.widget.TextView;


/**
 * Created by darry on 30/12/2016.
 */

public abstract interface LocationUI {
    public void UpdateUIWithFusedLocation(Location location);
    public void UpdateUIWithLocation(TextView latitude, TextView longitude, TextView accuracy, TextView address, Location theLocation);
    public void UpdateUIApplicationServicesAvailable(Boolean isAvailable);
    public void convertLocationToAddress(Location location);
    public void UpdateUIWithCustomProviderEnabled(Boolean isEnabled);
    public void UpdateUIWithCustomProviderRunning(Boolean isRunning);
    public void UpdateUIWithAwareness(TextView awarenessTextView, String text);
    public void UpdateUIWithGoogleMapsAPILocation(Location location);
    public void UpdateUIWithAPScanResult(ScanResult detectedAP);

}
