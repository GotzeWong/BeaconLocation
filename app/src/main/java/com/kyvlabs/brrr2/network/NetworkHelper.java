package com.kyvlabs.brrr2.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.gson.Gson;
import com.kyvlabs.brrr2.Application;
import com.kyvlabs.brrr2.data.BeaconIds;
import com.kyvlabs.brrr2.network.api.BeaconService;
import com.kyvlabs.brrr2.network.api.InfoService;
import com.kyvlabs.brrr2.network.api.LoginService;
import com.kyvlabs.brrr2.network.model.BeaconModel;
import com.kyvlabs.brrr2.network.model.ForgotModel;
import com.kyvlabs.brrr2.network.model.Group;
import com.kyvlabs.brrr2.network.model.LoginModel;
import com.kyvlabs.brrr2.network.model.RegistrationModel;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class NetworkHelper {
    private static final String SERVER_API_URL = "http://labo-pbei.no-ip.org:10001";

    private static Map<String, BeaconIds> requestsInProgress = new HashMap<>();

    public static void removeInProgressByTag(String tag) {
        requestsInProgress.remove(tag);
    }

    public Observable<ArrayList<Group>> getGroups() {
        if (isOnline()) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();

            LoginService service = retrofit.create(LoginService.class);
            return service.getGroups().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        } else {
            Log.d("NETWORK", "There is no internet connection");
            return Observable.just(new ArrayList<Group>());
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) Application.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public Observable<LoginModel> login(String email, String password, List<Integer> groups) {
        if (isOnline()) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient httpClient = new OkHttpClient();
            httpClient.interceptors().add(logging);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(httpClient)
                    .build();

            LoginService service = retrofit.create(LoginService.class);
            Gson converter = new Gson();
            return service.login(email, password, converter.toJson(groups)).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        } else {
            Log.d("NETWORK", "There is no internet connection");
            return Observable.just(new LoginModel());
        }
    }

    public Observable<LoginModel> loginFb(String email, String authKey) {
        if (isOnline()) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();

            LoginService service = retrofit.create(LoginService.class);
            return service.loginFb(email, "DEFAULT_PASSWORD", authKey).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        } else {
            Log.d("NETWORK", "There is no internet connection");
            return Observable.just(new LoginModel());
        }
    }

    public Observable<RegistrationModel> register(String email, String password, List<Integer> groups) {
        if (isOnline()) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();

            LoginService service = retrofit.create(LoginService.class);
            Gson converter = new Gson();
            return service.register(email, password, converter.toJson(groups)).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        } else {
            Log.d("NETWORK", "There is no internet connection");
            return Observable.just(new RegistrationModel());
        }
    }

    public Observable<BeaconModel> getBeacons(Collection<BeaconIds> beacons, String auth) {

        if (isOnline()) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();

            BeaconService service = retrofit.create(BeaconService.class);

            Log.d("NETWORK", "auth: " + auth);
            Observable<BeaconModel> downloadedBeacons =
                    service.getBeacons(encodeQueryPart(generateJsonFromBeaconsList(beacons)), auth)
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .observeOn(Schedulers.io())
                            .flatMap(new Func1<ArrayList<BeaconModel>, Observable<BeaconModel>>() {
                                @Override
                                public Observable<BeaconModel> call(ArrayList<BeaconModel> beaconModels) {
                                    Log.d("NETWORK", "flatMap: " + beaconModels.size());
                                    return Observable.from(beaconModels);
                                }
                            })
                            .filter(new Func1<BeaconModel, Boolean>() {
                                @Override
                                public Boolean call(BeaconModel beaconModel) {
                                    Log.d("NETWORK", "200");
                                    return beaconModel.getStatus().equals("200");
                                }
                            })
//                            .map(new Func1<BeaconModel, BeaconModel>() {
//                                @Override
//                                public BeaconModel call(BeaconModel beaconModel) {
//                                    String s = loadImage(beaconModel.getAbsolutePicture());
//                                    beaconModel.setAbsolutePicture(s);
//                                    return beaconModel;
//                                }
//                            });
                    ;

            return downloadedBeacons;

        } else {
            Log.d("NETWORK", "There is no internet connection");
            return Observable.empty();
        }
    }

    private String encodeQueryPart(String queryPart) {
        String query = "";
        try {
            query = URLEncoder.encode(queryPart, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("NETWORK", "Encoding error", e);
        }
        return query;
    }

    private String generateJsonFromBeaconsList(Collection<BeaconIds> beacons) {
        ArrayList<BeaconIds> beaconsList = (ArrayList<BeaconIds>) beacons;
        StringBuilder json = new StringBuilder();
        json.append('[');
        for (int i = 0; i < beaconsList.size(); i++) {
            BeaconIds beacon = beaconsList.get(i);
            json.append('{')
                    .append("\"uuid\":\"")
                    .append(beacon.getUuid())
                    .append("\",\"major\":\"")
                    .append(beacon.getMajor())
                    .append("\",\"minor\":\"").append(beacon.getMinor()).append("\"}");
            if (i < beaconsList.size() - 1) {
                json.append(',');
            }
        }
        json.append(']');

        Log.d("NETWORK", "Json generated : " + json.toString());
        return json.toString();
    }


    public void sendInfo(String auth, HashMap<String, String> info) {
        if (isOnline()) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();

            InfoService service = retrofit.create(InfoService.class);

            Log.d("NETWORK", "auth: " + auth);
            Observable<Void> result = service.info(auth, info);
            result.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io());
            result.subscribe();

        } else {
            Log.d("NETWORK", "There is no internet connection");
        }
    }

    public Observable<ForgotModel> forgotPassword(String email) {
        if (isOnline()) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();

            LoginService service = retrofit.create(LoginService.class);

            Observable<ForgotModel> result = service.forgotPassword(email);
            return result.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        } else {
            Log.d("NETWORK", "There is no internet connection");
            return Observable.empty();
        }
    }
}
