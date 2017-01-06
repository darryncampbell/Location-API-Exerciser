package com.darryncampbell.locationapiexerciser;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.twolinessoftware.android.GpsPlaybackBroadcastReceiver;
import com.twolinessoftware.android.GpsPlaybackListener;
import com.twolinessoftware.android.IPlaybackService;
import com.twolinessoftware.android.PlaybackService;
import com.twolinessoftware.android.framework.util.Logger;

/**
 * Created by darry on 03/01/2017.
 */

public class CustomProviderWrapper implements GpsPlaybackListener {
    public static final String TAG = "LOCATION API EXERCISER";

    private IPlaybackService service;
    private ServiceConnection connection;
    private GpsPlaybackBroadcastReceiver receiver;
    private Context context;
    private LocationUI ui;
    private int state;
    private Boolean initialized;
    private String chosenTrack;
    private int delayTimeOnReplay;
    private Boolean fileError;

    public CustomProviderWrapper(Context context, LocationUI ui) {
        this.context = context;
        this.ui = ui;
        initialized = false;
        fileError = false;
        receiver = null;
    }


    public void onStart()
    {
    }

    public void onStop()
    {
        try {
            context.unregisterReceiver(receiver);
        }
        catch (IllegalArgumentException e) {
            //  receiver was never registered
        }
        try {
            context.unbindService(connection);
        } catch (Exception ie) {
        }
        service = null;
        initialized = false;
    }

    public void initializeAndConnect()
    {
        bindStatusListener();
        connectToService();
        initialized = true;
    }

    private void bindStatusListener() {
        receiver = new GpsPlaybackBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(GpsPlaybackBroadcastReceiver.INTENT_BROADCAST);
        context.registerReceiver(receiver, filter);
    }

    private void connectToService() {
        Intent i = new Intent(context.getApplicationContext(), PlaybackService.class);
        connection = new PlaybackServiceConnection();
        context.bindService(i, connection, Context.BIND_AUTO_CREATE);
    }

    public void userStarted(String locationProvider, String chosenTrack, int delayTimeOnReplay)
    {
        PlaybackService.PROVIDER_NAME = locationProvider;
        this.chosenTrack = chosenTrack;
        this.delayTimeOnReplay = delayTimeOnReplay;
        if (!initialized)
            initializeAndConnect();
        else
            startReporting();
    }

    public void startReporting()
    {
        try
        {
            if (service != null)
                service.startService(this.chosenTrack);
        }
        catch (RemoteException e) {}

        Intent i = new Intent(context, PlaybackService.class);
        i.putExtra("delayTimeOnReplay", "" + this.delayTimeOnReplay);
        context.startService(i);
    }

    public void userStopped()
    {
        try {
            if (service != null)
            service.stopService();
        } catch (RemoteException e) {
        }
    }

/*    public void testing()
    {
        String delayTimeOnReplay = "" + interval;
        try
        {
            if (service != null)
                service.startService("canada_highway");
        }
        catch (RemoteException e) {}

        Intent i = new Intent(context, PlaybackService.class);
        i.putExtra("delayTimeOnReplay", delayTimeOnReplay);
        context.startService(i);
    }
*/

    @Override
    public void onFileLoadStarted() {
        Logger.d(TAG, "File loading started");
        //showProgressDialog();

    }

    @Override
    public void onFileLoadFinished() {
        Logger.d(TAG, "File loading finished");
        //hideProgressDialog();
    }

    @Override
    public void onStatusChange(int newStatus) {
        state = newStatus;
        Log.i(TAG, "Playback Service Connection State: " + state);
        if (!fileError)
            ui.UpdateUIWithCustomProviderRunning(state == PlaybackService.RUNNING);
        //updateUi();
    }

    @Override
    public void onFileError(String message) {
        Log.e(TAG, "Error with Custom Provider: " + message);
        ui.UpdateUIWithCustomProviderEnabled(false);
        fileError = true;
//        userStopped();
        //hideProgressDialog();
    }


    class PlaybackServiceConnection implements ServiceConnection {

        public void onServiceConnected(ComponentName name, IBinder boundService) {
            service = IPlaybackService.Stub.asInterface(boundService);
            try {
                state = service.getState();
            } catch (RemoteException e) {
                Logger.e(TAG, "Unable to access state:" + e.getMessage());
            }
            Log.i(TAG, "Playback Service Connection State: " + state);
            if (!fileError) {
                ui.UpdateUIWithCustomProviderEnabled(true);
                ui.UpdateUIWithCustomProviderRunning(state == PlaybackService.RUNNING);
            }
            //updateUi();
            //  We want to connect and then immediately start reporting
            startReporting();
        }

        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }

    }


}
