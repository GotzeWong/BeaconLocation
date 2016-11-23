package com.kyvlabs.beaconadvertiser.network.api;


import com.kyvlabs.beaconadvertiser.network.model.BeaconModel;

import java.util.ArrayList;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface BeaconService {
    @GET("/api/beacon-data/")
    Observable<ArrayList<BeaconModel>> getBeacons(@Query(value = "beacons", encoded = true) String beaconsJson, @Query(value = "auth_key") String authKey);
}
