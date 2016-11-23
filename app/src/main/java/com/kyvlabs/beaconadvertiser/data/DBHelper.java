package com.kyvlabs.beaconadvertiser.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kyvlabs.beaconadvertiser.Application;
import com.kyvlabs.beaconadvertiser.R;
import com.kyvlabs.beaconadvertiser.network.NetworkHelper;
import com.kyvlabs.beaconadvertiser.network.model.BeaconModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

//Helper to work with DB.
public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 66;

    private static final String DB_NAME = "adsDB";

    private static final String AD_TABLE = "ads";

    private static final String AD_TABLE_UUID = "uuid";
    private static final String AD_TABLE_MAJOR = "major";
    private static final String AD_TABLE_MINOR = "minor";
    private static final String AD_TABLE_TITLE = "title";
    private static final String AD_TABLE_PICTURE = "picture";
    private static final String AD_TABLE_LINK = "link";
    private static final String AD_TABLE_DESCRIPTION = "description";
    private static final String AD_TABLE_NEXT_SHOW_TIME = "last_next_time";
    private static final String AD_TABLE_NEXT_UPDATE_TIME = "last_update_time";
    private static final String AD_TABLE_GROUP_ID = "group_id";
    private static final String AD_TABLE_GROUP_NAME = "group_name";

    private static long NOT_UPDATE_INTERVAL = 1000 * 10;//Default interval 10sec.


    //Application context
    private Context context;

    public DBHelper() {
        super(Application.getAppContext(), DB_NAME, null, DB_VERSION);
        this.context = Application.getAppContext();
    }

    //Read All beacons from db
    public List<DBBeacon> getBeaconsFromDB() {
        ArrayList<DBBeacon> beaconsFromDB = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + AD_TABLE, null);
        while (cursor.moveToNext()) {
            DBBeacon beacon = getBeaconFromCursor(cursor);
            beaconsFromDB.add(beacon);
        }
        db.close();
        return beaconsFromDB;

    }

    // get beacon from cursor
    private DBBeacon getBeaconFromCursor(Cursor cursor) {
        DBBeacon beacon = new DBBeacon();
        BeaconIds ids = new BeaconIds(cursor.getString(cursor.getColumnIndex(AD_TABLE_UUID)),
                cursor.getString(cursor.getColumnIndex(AD_TABLE_MAJOR)),
                cursor.getString(cursor.getColumnIndex(AD_TABLE_MINOR)));
        beacon.setIds(ids);
        beacon.setTitle(cursor.getString(cursor.getColumnIndex(AD_TABLE_TITLE)));
        beacon.setPicture(cursor.getString(cursor.getColumnIndex(AD_TABLE_PICTURE)));
        beacon.setLink(cursor.getString(cursor.getColumnIndex(AD_TABLE_LINK)));
        beacon.setDescription(cursor.getString(cursor.getColumnIndex(AD_TABLE_DESCRIPTION)));
        beacon.setNextShowTime(cursor.getLong(cursor.getColumnIndex(AD_TABLE_NEXT_SHOW_TIME)));
        beacon.setNextUpdateTime(cursor.getLong(cursor.getColumnIndex(AD_TABLE_NEXT_UPDATE_TIME)));
        beacon.setGroupName(cursor.getString(cursor.getColumnIndex(AD_TABLE_GROUP_NAME)));
        beacon.setGroupToBind(cursor.getString(cursor.getColumnIndex(AD_TABLE_GROUP_ID)));
        return beacon;
    }

    //set next show time
    public void setNextShowTime(BeaconIds ids, long timestamp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(AD_TABLE_NEXT_SHOW_TIME, timestamp);
        String whereClause = AD_TABLE_UUID + " =? AND " + AD_TABLE_MAJOR + " =? AND " + AD_TABLE_MINOR + "=? ";
        String[] whereArgs = new String[]{ids.getUuid(), ids.getMajor(), ids.getMinor()};
        db.update(AD_TABLE, cv, whereClause, whereArgs);
    }

    // set all beacons show time to 0. All beacons shows next time.
    public void clearAllNextShowTimes() {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(AD_TABLE_NEXT_SHOW_TIME, 0);
        String whereClause = AD_TABLE_NEXT_SHOW_TIME + " > ?";
        String[] whereArgs = new String[]{"0"};
        db.update(AD_TABLE, cv, whereClause, whereArgs);
    }

    public void removeAllBeaconsData() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(AD_TABLE, null, null);
    }

    //Returns beacon list by ids collection
    public Observable<DBBeacon> getBeaconsByIds(Collection<BeaconIds> beaconCollection, final String auth) {
        final NetworkHelper networkHelper = new NetworkHelper();
        Observable<DBBeacon> beaconsDBCollection = Observable.from(beaconCollection)
                .map(new Func1<BeaconIds, DBBeacon>() {
                    @Override
                    public DBBeacon call(BeaconIds beaconIds) {
                        return changeToDbBeacon(beaconIds);
                    }
                });

        Observable<DBBeacon> beaconsToShow;
        if (networkHelper.isOnline()) {
            beaconsToShow = beaconsDBCollection
                    .map(new Func1<DBBeacon, BeaconIds>() {
                        @Override
                        public BeaconIds call(DBBeacon beacon) {
                            return beacon.getIds();
                        }
                    })
                    .toList()
                    .flatMap(new Func1<List<BeaconIds>, Observable<BeaconModel>>() {
                        @Override
                        public Observable<BeaconModel> call(List<BeaconIds> beaconIdses) {
                            if (beaconIdses.size() > 0) {
                                return networkHelper.getBeacons(beaconIdses, auth);
                            } else {
                                return Observable.empty();
                            }
                        }
                    })
                    .map(new Func1<BeaconModel, DBBeacon>() {
                        @Override
                        public DBBeacon call(BeaconModel beaconModel) {
                            return new DBBeacon(beaconModel);
                        }
                    })
                    .map(new Func1<DBBeacon, DBBeacon>() {
                        @Override
                        public DBBeacon call(DBBeacon beacon) {
                            addOrUpdateBeacon(beacon);
                            return beacon;
                        }
                    });
        } else {
            beaconsToShow = Observable.from(beaconCollection)
                    .map(new Func1<BeaconIds, DBBeacon>() {
                        @Override
                        public DBBeacon call(BeaconIds beaconIds) {
                            return getBeaconByIds(beaconIds);
                        }
                    })
                    .filter(new Func1<DBBeacon, Boolean>() {
                        @Override
                        public Boolean call(DBBeacon beacon) {
                            return (beacon.getNextUpdateTime() > System.currentTimeMillis());
                        }
                    })
            ;
        }


        return beaconsToShow;
    }

    //Return beacon by id
    private DBBeacon getBeaconByIds(BeaconIds ids) {
        DBBeacon beacon;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + AD_TABLE + " WHERE " + AD_TABLE_UUID + " = '"
                        + ids.getUuid()
                        + "' AND " + AD_TABLE_MAJOR + " = '"
                        + ids.getMajor()
                        + "' AND " + AD_TABLE_MINOR + " = '"
                        + ids.getMinor() + "'",
                null);
        if (cursor.moveToFirst()) {
            beacon = getBeaconFromCursor(cursor);
        } else {
            beacon = new DBBeacon();
            beacon.setIds(ids);
            beacon.setNextUpdateTime(0);
            //Need to download from internet
        }
        return beacon;
    }

    private DBBeacon changeToDbBeacon(BeaconIds ids) {
        DBBeacon beacon = new DBBeacon();
        beacon.setIds(ids);
        return beacon;
    }

    public void addOrUpdateBeacon(DBBeacon beacon) {

        SQLiteDatabase database = getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(AD_TABLE_UUID, beacon.getIds().getUuid());
        cv.put(AD_TABLE_MAJOR, beacon.getIds().getMajor());
        cv.put(AD_TABLE_MINOR, beacon.getIds().getMinor());
        cv.put(AD_TABLE_TITLE, beacon.getTitle());
        cv.put(AD_TABLE_PICTURE, beacon.getPicture());
        cv.put(AD_TABLE_LINK, beacon.getLink());
        cv.put(AD_TABLE_DESCRIPTION, beacon.getDescription());
        cv.put(AD_TABLE_GROUP_NAME, beacon.getGroupName());
        cv.put(AD_TABLE_GROUP_ID, beacon.getGroupToBind());
        updateCacheTime();
        cv.put(AD_TABLE_NEXT_UPDATE_TIME, System.currentTimeMillis() + NOT_UPDATE_INTERVAL);
        try {
            database.insertWithOnConflict("ads", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLiteConstraintException e) {
            Log.d("DBHELPER", "Something wrong :" + e.getLocalizedMessage());
        }
        Log.d("DBHELPER", "Beacon added to local DB" + beacon.toString());
//        setNextUpdateTime(beacon.getIds(), System.currentTimeMillis() + NOT_UPDATE_INTERVAL);
    }

    public void updateCacheTime() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        NOT_UPDATE_INTERVAL = Long.valueOf(context.getResources().getString(R.string.update_time_period));
//        if (sharedPreferences.getBoolean("settings_key_cache_enabled", true)) {
//            NOT_UPDATE_INTERVAL = 10;
//        } else {
//            NOT_UPDATE_INTERVAL = Long.valueOf(sharedPreferences.getString("settings_key_cache_time", context.getResources().getString(R.string.update_time_period)));
//        }
    }

    //Set default data.
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DATA", "--- onCreate database ---");

        db.execSQL("create table " + AD_TABLE + " ("
                + AD_TABLE_UUID + " text, "
                + AD_TABLE_MAJOR + " text, "
                + AD_TABLE_MINOR + " text, "
                + AD_TABLE_TITLE + " text, "
                + AD_TABLE_PICTURE + " text, "
                + AD_TABLE_LINK + " text, "
                + AD_TABLE_DESCRIPTION + " text, "
                + AD_TABLE_GROUP_ID + " text, "
                + AD_TABLE_GROUP_NAME + " text, "
                + AD_TABLE_NEXT_SHOW_TIME + " long default 0, "
                + AD_TABLE_NEXT_UPDATE_TIME + " long default 0, "
                + "primary key (" + AD_TABLE_UUID + ", " + AD_TABLE_MAJOR + ", " + AD_TABLE_MINOR + ")"
                + ");");
    }

    //Calls when db version is changed.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DATA", "--- onUpgrade database ---");
        db.execSQL("DROP TABLE IF EXISTS " + AD_TABLE);
        onCreate(db);
    }
}
