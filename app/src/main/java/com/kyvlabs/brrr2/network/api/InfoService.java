package com.kyvlabs.brrr2.network.api;

import java.util.HashMap;

import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

public interface InfoService {

    @POST("/api/info/")
    Observable<Void> info(@Query(value = "auth_key") String authKey, @Body HashMap<String, String> data);

}
