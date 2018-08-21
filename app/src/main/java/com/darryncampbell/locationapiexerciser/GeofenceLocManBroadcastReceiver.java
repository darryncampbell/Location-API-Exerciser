package com.darryncampbell.locationapiexerciser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

public class GeofenceLocManBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "LOCATION API EXERCISER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null)
        {
            //  The Location Manager has notified us we have transitioned into or out of the specified
            //  Geofence
            if (intent.hasExtra(LocationManager.KEY_PROXIMITY_ENTERING))
            {
                Boolean didEnter = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
                if (didEnter)
                {
                    //GeofenceUtilities.sendNotification("Entered: From Location Manager", context, 0);
                    //  Handle we have entered the specified Geofence
//                    Handler handler=new Handler(Looper.getMainLooper());
//                    handler.post(new Runnable(){
//                        public void run(){
//                            Toast.makeText(getApplicationContext(), "ENTERED GEOFENCE", Toast.LENGTH_LONG).show();
//                        }
//                    });
                    Log.i(TAG, "Entering geofence as specified by Location Manager");
                }
                else
                {
                    //  Handle we have exited the specified Geofence
                    //GeofenceUtilities.sendNotification("Exited: From Location Manager", context, 0);
//                    Handler handler=new Handler(Looper.getMainLooper());
//                    handler.post(new Runnable(){
//                        public void run(){
//                            Toast.makeText(getApplicationContext(), "EXITED GEOFENCE", Toast.LENGTH_LONG).show();
//                        }
//                    });
                    Log.i(TAG, "Exiting geofence as specified by Location Manager");
                }
            }
            else
            {
                //  Something went wrong with the subsystem
                Toast.makeText(context, "ERROR", Toast.LENGTH_LONG);
                Log.i(TAG, "The returned intent does not contain whether we entered the geofence or not");
            }

        }
    }
}
