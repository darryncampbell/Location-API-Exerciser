package com.darryncampbell.locationapiexerciser;

import android.location.Location;
import android.widget.TextView;


/**
 * Created by darry on 30/12/2016.
 */

public abstract interface LocationUI {
    public void UpdateUIWithFusedLocation(Location location);
    public void UpdateUIWithLocation(TextView latitude, TextView longitude, TextView accuracy, Location theLocation);
    public void UpdateUIApplicationServicesAvailable(String isAvailable);

    //todo
    public void convertLocationToAddress(Location location);
}
