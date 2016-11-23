package com.kyvlabs.beaconadvertiser.scanners;

import android.content.Context;
import android.os.AsyncTask;

import com.kyvlabs.beaconadvertiser.R;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;

import java.util.List;

//Base scanner
public abstract class Scanner extends AsyncTask<Void, Void, Void> {
    //Library base class/ Used to work with beacons
    protected BeaconManager beaconManager;
    protected Context applicationContext;
    //Region for scanning
    protected Region region;
    //who use this scanner
    private BeaconConsumer beaconConsumer;

    public Scanner(Context applicationContext, BeaconConsumer beaconConsumer) {
        this.applicationContext = applicationContext;
        this.beaconConsumer = beaconConsumer;

        //Create region
        region = new Region(getStrRes(R.string.region_name), null, null, null);

        //beaconManager base initialization
        beaconManager = BeaconManager.getInstanceForApplication(applicationContext);
        beaconManager.bind(beaconConsumer);

    }

    protected String getStrRes(int intResource) {
        return applicationContext.getResources().getString(intResource);
    }

    @Override
    protected abstract Void doInBackground(Void... params);

    @Override
    protected void onCancelled() {
        //TODO fix BeaconService has leaked ServiceConnection org.altbeacon.beacon.BeaconManager
        beaconManager.unbind(beaconConsumer);
    }

    public void onCreate() {
        List<BeaconParser> beaconParsers = beaconManager.getBeaconParsers();
        if (beaconParsers.size() < 2) {
            beaconParsers.add(new BeaconParser().
                    setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));//This need to find all types of beacons
        }
    }

    public void setBackgroundMode(boolean isBackground) {
        beaconManager.setBackgroundMode(isBackground);
    }
}
