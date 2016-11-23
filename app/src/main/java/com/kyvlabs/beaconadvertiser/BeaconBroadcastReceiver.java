package com.kyvlabs.beaconadvertiser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kyvlabs.beaconadvertiser.services.BeaconService;

//Start service when device was restarted.
public class BeaconBroadcastReceiver extends BroadcastReceiver {
    private final String LOG_TAG = "BeaconBroadcastReceiver";

    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive " + intent.getAction());
        context.startService(new Intent(context, BeaconService.class));
    }
}

