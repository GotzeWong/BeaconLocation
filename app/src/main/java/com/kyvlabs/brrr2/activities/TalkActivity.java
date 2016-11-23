package com.kyvlabs.brrr2.activities;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.kyvlabs.brrr2.Application;
import com.kyvlabs.brrr2.R;
import com.kyvlabs.brrr2.activities.fragment.CityStreamFragment;
import com.kyvlabs.brrr2.data.BeaconIds;
import com.kyvlabs.brrr2.data.DBBeacon;
import com.kyvlabs.brrr2.data.DBHelper;
import com.kyvlabs.brrr2.data.DataKeys;
import com.kyvlabs.brrr2.views.AdsListViewAdapter;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

public class TalkActivity extends AppCompatActivity implements BeaconConsumer {

    protected static final String LOG_TAG = "TalkActivity";
    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        if (savedInstanceState == null) {

            CityStreamFragment listFragment = new CityStreamFragment();

            Application.setFragmentManager(getSupportFragmentManager());
            FragmentTransaction fragmentTransaction = Application.getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.container, listFragment);
            fragmentTransaction.commit();
        }
        beaconManager = BeaconManager.getInstanceForApplication(this);
        List<BeaconParser> beaconParsers = beaconManager.getBeaconParsers();
        beaconParsers.add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //beaconManager parametrizations
        beaconManager.setBackgroundScanPeriod(Long.valueOf(sharedPreferences.getString(getStrRes(R.string.settings_key_background_scan_period), getStrRes(R.string.settings_default_background_scan_period))));
        beaconManager.setBackgroundBetweenScanPeriod(Long.valueOf(sharedPreferences.getString(getStrRes(R.string.settings_key_background_between_scan_period), getStrRes(R.string.settings_default_background_between_scan_period))));
        beaconManager.setForegroundScanPeriod(Long.valueOf(sharedPreferences.getString(getStrRes(R.string.settings_key_foreground_scan_period), getStrRes(R.string.settings_default_foreground_scan_period))));
        beaconManager.setForegroundBetweenScanPeriod(Long.valueOf(sharedPreferences.getString(getStrRes(R.string.settings_key_foreground_between_scan_period), getStrRes(R.string.settings_default_foreground_between_scan_period))));
    }

    private String getStrRes(int intResource) {
        return this.getResources().getString(intResource);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
///////////////////
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
///////////////////

                Log.d(LOG_TAG, "FOUNDED " + beacons.size() + " beacons");

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Integer beacon_range = Integer.valueOf(sharedPreferences.getString(getStrRes(R.string.settings_key_beacon_range), getStrRes(R.string.settings_default_beacon_range)));
                Iterator<Beacon> iterator = beacons.iterator();
                while (iterator.hasNext()) {
                    Beacon next = iterator.next();
                    Log.d(LOG_TAG, "Distance " + next.getDistance() + " beacons");
                    if (next.getDistance() > beacon_range) {
                        iterator.remove();
                    }
                }

                Log.d(LOG_TAG, "Filtered by distance" + beacon_range + ": " + beacons.size() + " beacons");


                CityStreamFragment.setBeacons(new ArrayList<>(getIdsFromBeacons(beacons)));
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region(getStrRes(R.string.region_name), null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private Collection<BeaconIds> getIdsFromBeacons(Collection<Beacon> beacons) {
        Collection<BeaconIds> beaconIdsList = new ArrayList<>();
        for (Beacon beacon : beacons) {
            beaconIdsList.add(new BeaconIds(beacon.getId1().toString(), beacon.getId2().toString(), beacon.getId3().toString()));
        }
        return beaconIdsList;
    }

    public static class PlaceholderFragment extends Fragment {
        private static List<DBBeacon> beaconsFromDB = new ArrayList<>();
        private static AdsListViewAdapter beaconsAdapter;
        private static ListView adsListView;
        private static View rootView;

        private static Handler UIHandler = new Handler(Looper.getMainLooper());

        public PlaceholderFragment() {
        }

        //set beacons collection to list after reading from db
        public static void setBeacons(Collection<BeaconIds> beacons) {
            getBeaconsFromDB(beacons);
        }

        //After scanning reading founded beacons list from dB.
        private static void getBeaconsFromDB(Collection<BeaconIds> beaconCollection) {

            SharedPreferences userDetails = Application.getAppContext().getSharedPreferences(Application.getAppContext().getString(R.string.saved_auth_key), Context.MODE_PRIVATE);
            String auth = userDetails.getString(Application.getAppContext().getString(R.string.saved_auth_key), "");

            DBHelper dbHelper = new DBHelper();
            dbHelper.getBeaconsByIds(beaconCollection, auth)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            clearAdapter();
                        }
                    })
                    .subscribe(
                            new Action1<DBBeacon>() {
                                @Override
                                public void call(final DBBeacon beacon) {
                                    addBeaconToAdapter(beacon);
                                }
                            },
                            new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    Log.d("Talk on error", "", throwable);
                                }
                            });

            dbHelper.close();

        }

        private static void clearAdapter() {
            runOnUi(new Runnable() {
                @Override
                public void run() {
                    beaconsFromDB.clear();
                    beaconsAdapter.notifyDataSetChanged();
                }
            });
        }

        private static void addBeaconToAdapter(final DBBeacon beacon) {
            runOnUi(new Runnable() {
                @Override
                public void run() {
                    beaconsFromDB.add(beacon);
                    beaconsAdapter.notifyDataSetChanged();
                }
            });
        }

        private static void runOnUi(Runnable runnable) {
            UIHandler.post(runnable);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode) {
                case 0: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length <= 0
                            || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(), "Sorry but we need this permission", Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    }
                }

            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(getActivity(), "Sorry but we need this permission", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            0);
                }
            }

            rootView = inflater.inflate(R.layout.fragment_talk, container, false);
            adsListView = (ListView) rootView.findViewById(R.id.talk_beacons_list);
            beaconsAdapter = new AdsListViewAdapter(getActivity().getApplicationContext(), beaconsFromDB);
            adsListView.setAdapter(beaconsAdapter);

            adsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), AdvertActivity.class);
                    DBBeacon dbBeacon = (DBBeacon) parent.getAdapter().getItem(position);
                    intent.putExtra(DataKeys.BEACON_IDS, dbBeacon.getIds());
                    intent.putExtra(DataKeys.AD_TITLE_KEY, dbBeacon.getTitle());
                    intent.putExtra(DataKeys.AD_PICTURE_KEY, dbBeacon.getPicture());
                    intent.putExtra(DataKeys.AD_LINK_KEY, dbBeacon.getLink());
                    intent.putExtra(DataKeys.AD_DESCRIPTION_KEY, dbBeacon.getDescription());
                    startActivity(intent);
                }
            });
            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            clearAdapter();
        }

        @Override
        public void onResume() {
            super.onResume();
            clearAdapter();
        }
    }
}
