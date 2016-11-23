package com.kyvlabs.beaconadvertiser.services;

import android.util.Log;

import com.kyvlabs.beaconadvertiser.BeaconNotifier;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;

public class WalkService extends BeaconService {

    private BeaconNotifier notifier;

    @Override
    public void onCreate() {
        super.onCreate();
        notifier = new BeaconNotifier(getApplicationContext());
        //Need to set beacon types. Doesn't work without it
        beaconScanner.onCreate();
        //Set bm true to slow scanning
        beaconScanner.setBackgroundMode(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notifier.cancelAll();
    }

    //React when entered to the region
    @Override
    public void regionEnter() {
        Log.d("REGION", "regionEnter");
        beaconScanner.setBackgroundMode(false);
    }

    //React when exited from the region
    @Override
    public void regionExit() {
        Log.d("REGION", "regionExit");
        beaconScanner.setBackgroundMode(true);
    }

    //React when beacons are founded
    @Override
    public void foundedBeacons(Collection<Beacon> beacons) {
        Log.d("REGION", "foundedBeacons");
        //send notifications about beacons
        notifier.sendNotificationAboutBeacons(getIdsFromBeacons(beacons));
    }
}
