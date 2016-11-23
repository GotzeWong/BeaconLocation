package com.kyvlabs.brrr2.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.kyvlabs.brrr2.BeaconCallback;
import com.kyvlabs.brrr2.data.BeaconIds;
import com.kyvlabs.brrr2.scanners.BeaconScanner;
import com.kyvlabs.brrr2.scanners.Scanner;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;

import java.util.ArrayList;
import java.util.Collection;

//Base class. Background service for beacon scanner
public abstract class BeaconService extends Service implements BeaconConsumer, BeaconCallback {

    protected Scanner beaconScanner;

    public BeaconService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //create scanner
        beaconScanner = new BeaconScanner(getApplicationContext(), this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        //cancel scanning
        beaconScanner.cancel(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBeaconServiceConnect() {
        //run scanner
        beaconScanner.execute();
    }

    //Cut DB beacon list to Ids list
    protected Collection<BeaconIds> getIdsFromBeacons(Collection<Beacon> beacons) {
        Collection<BeaconIds> beaconIdsList = new ArrayList<BeaconIds>();
        for (Beacon beacon : beacons) {
            beaconIdsList.add(new BeaconIds(beacon.getId1().toString(), beacon.getId2().toString(), beacon.getId3().toString()));
        }
        return beaconIdsList;
    }
}
