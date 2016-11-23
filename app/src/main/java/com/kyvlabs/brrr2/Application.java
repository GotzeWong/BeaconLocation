package com.kyvlabs.brrr2;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.kyvlabs.brrr2.data.DBBeacon;
import com.kyvlabs.brrr2.utils.DBHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Base app class. Used like context Container.
public class Application extends android.app.Application {
    private static Context context;
    private static FragmentManager fragmentManager;

    private static DBHelper mydb = new DBHelper(context);
    public static DBHelper getMydb() {
        return mydb;
    }

    public static void setMydb(DBHelper mydb) {
        Application.mydb = mydb;
    }

    public static Context getAppContext() {
        return Application.context;
    }

    public static String getCachePath() {
        File path = context.getExternalCacheDir();
        if (path != null) {
            return path.getPath();
        } else {
            return context.getCacheDir().getPath();
        }
    }

    public static FragmentManager getFragmentManager() {
        return Application.fragmentManager;
    }

    public static void setFragmentManager(FragmentManager fragmentManager) {
        Application.fragmentManager = fragmentManager;
    }

    public void onCreate() {
        super.onCreate();
        Application.context = getApplicationContext();
    }

    public static List<DBBeacon> getBeaconList() {
        return beacons;
    }

    public static void setBeaconList(List<DBBeacon> beaconList) {
        Application.beacons = beaconList;
    }

    private static List<DBBeacon> beacons = new ArrayList<>();

    public static void clearBeaconList() {
        Application.beacons.clear();
    }

    public static void addBeaconToList(DBBeacon beacon) {
        beacons.add(beacon);
        Collections.sort(beacons);
    }
}


