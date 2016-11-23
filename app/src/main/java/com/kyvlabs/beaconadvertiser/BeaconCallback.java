package com.kyvlabs.beaconadvertiser;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;

//Callback interface implement it if you want react to found beacon
public interface BeaconCallback {
    void regionEnter();

    void regionExit();

    void foundedBeacons(Collection<Beacon> beacons);
}
