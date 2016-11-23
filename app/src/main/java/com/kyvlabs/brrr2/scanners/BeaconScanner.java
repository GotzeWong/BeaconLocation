package com.kyvlabs.brrr2.scanners;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kyvlabs.brrr2.BeaconCallback;
import com.kyvlabs.brrr2.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Iterator;

//Scanner implementation
public class BeaconScanner extends Scanner {

    private final Context applicationContext;
    private boolean isLastScanSuccessful = false;
    private boolean needToCountVacuum = false;
    private int vacuumCounter = 0;

    public BeaconScanner(final Context applicationContext, final BeaconConsumer beaconConsumer, final BeaconCallback callback) {
        super(applicationContext, beaconConsumer);
        this.applicationContext = applicationContext;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        //beaconManager parametrizations
        beaconManager.setBackgroundScanPeriod(Long.valueOf(sharedPreferences.getString(getStrRes(R.string.settings_key_background_scan_period), getStrRes(R.string.settings_default_background_scan_period))));
        beaconManager.setBackgroundBetweenScanPeriod(Long.valueOf(sharedPreferences.getString(getStrRes(R.string.settings_key_background_between_scan_period), getStrRes(R.string.settings_default_background_between_scan_period))));
        beaconManager.setForegroundScanPeriod(Long.valueOf(sharedPreferences.getString(getStrRes(R.string.settings_key_foreground_scan_period), getStrRes(R.string.settings_default_foreground_scan_period))));
        beaconManager.setForegroundBetweenScanPeriod(Long.valueOf(sharedPreferences.getString(getStrRes(R.string.settings_key_foreground_between_scan_period), getStrRes(R.string.settings_default_foreground_between_scan_period))));

//create scanning behaviour
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
/////////////////////
//                ArrayList<Identifier> identifierArrayList = new ArrayList<>();
//
//                identifierArrayList.add(Identifier.parse("ebefd083-70a2-47c8-9837-e7b5634df524"));
//                identifierArrayList.add(Identifier.parse("12"));
//                identifierArrayList.add(Identifier.parse("42"));
//
//                Beacon.Builder builder = new Beacon.Builder().setIdentifiers(identifierArrayList);
//                Beacon build = builder.build();
//
//                beacons.add(build);
/////////////////////

                Log.d("MONITOR", "FOUNDED " + beacons.size() + " beacons");

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                Integer beacon_range = Integer.valueOf(sharedPreferences.getString(getStrRes(R.string.settings_key_beacon_range), getStrRes(R.string.settings_default_beacon_range)));
                Iterator<Beacon> iterator = beacons.iterator();
                while (iterator.hasNext()) {
                    Beacon next = iterator.next();
                    Log.d("MONITOR", "Distance " + next.getDistance() + " beacons");
                    if (next.getDistance() > beacon_range) {
                        iterator.remove();
                    }
                }

                Log.d("MONITOR", "Filtered by distance" + beacon_range + ": " + beacons.size() + " beacons");

                if (beacons.size() == 0) {
                    if (isLastScanSuccessful) {
                        needToCountVacuum = true;
                    }
                    if (needToCountVacuum) {
                        vacuumCounter++;
                    }
                    if (vacuumCounter == 3) {
                        needToCountVacuum = false;
                        vacuumCounter = 0;
                        callback.regionExit();
                    }
                    isLastScanSuccessful = false;
                    return;
                }
                if (!isLastScanSuccessful) {
                    callback.regionEnter();
                }
                isLastScanSuccessful = true;
                callback.foundedBeacons(beacons);
            }
        });

    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        Log.d("DEB", "on canceled");
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onCancelled();
    }

}
