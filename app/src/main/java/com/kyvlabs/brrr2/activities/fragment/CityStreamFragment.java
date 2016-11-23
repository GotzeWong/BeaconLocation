package com.kyvlabs.brrr2.activities.fragment;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kyvlabs.brrr2.Application;
import com.kyvlabs.brrr2.BuildConfig;
import com.kyvlabs.brrr2.R;
import com.kyvlabs.brrr2.activities.mvp.CityStreamMvpView;
import com.kyvlabs.brrr2.activities.mvp.CityStreamPresenter;
import com.kyvlabs.brrr2.data.BeaconIds;
import com.kyvlabs.brrr2.data.DBBeacon;
import com.kyvlabs.brrr2.data.DBHelper;
import com.kyvlabs.brrr2.utils.DialogHelper;

import org.altbeacon.beacon.BeaconManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

public class CityStreamFragment extends TitledFragment implements CityStreamMvpView{
    private static final int BT_REQUEST = 102;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 101;

    protected static final String LOG_TAG = "BRRR2";

    private static final int LAYOUT = R.layout.fragment_city_stream;
    //@Bind(R.id.city_stream_recycler_view)
    static RecyclerView mRecyclerView;
    @Bind(R.id.city_stream_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private static CityStreamRecyclerAdapter cityStreamRecyclerAdapter;
    private CityStreamPresenter cityStreamPresenter = new CityStreamPresenter();

    private static Handler UIHandler = new Handler(Looper.getMainLooper());
    private static Context context = null;

    public static CityStreamFragment getInstance(Context context) {
        Bundle args = new Bundle();
        CityStreamFragment fragment = new CityStreamFragment();
//        fragment.setTitle(context.getString(R.string.tab_title_city_stream));
        fragment.setTitle("Hi");
        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void addCard() {
//        cityStreamRecyclerAdapter.addItem(beacon);
//        cityStreamRecyclerAdapter.notifyDataSetChanged();
//    }

    @Override
    public void showError(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void stopLoadIndicator() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BT_REQUEST) {
            if (resultCode != Activity.RESULT_OK) {
                showBTNotEnabledMessage();
            }
        }
    }


    private void checkGrantedCoarseLocationPermission(int grantResult) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISSION", "coarse location permission granted");
        } else {
            showLocationPermissionLimitedMessage();
        }
    }

    private void showLocationPermissionLimitedMessage() {
        DialogHelper.showOkDialog(getActivity(), R.string.location_permission_limited_title, R.string.location_permission_limited_message);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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


        View v = inflater.inflate(LAYOUT, container, false);
        ButterKnife.bind(this, v);


        mRecyclerView = (RecyclerView)v.findViewById(R.id.city_stream_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        cityStreamRecyclerAdapter = new CityStreamRecyclerAdapter(getContext(), Application.getBeaconList());
        mRecyclerView.setAdapter(cityStreamRecyclerAdapter);

        context = getContext();

        cityStreamPresenter.attachView(this);

        if (BuildConfig.DEBUG) {
            cityStreamPresenter.loadCard();
        }

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestCoarseLocationPermission();
                verifyBluetooth();
                cityStreamPresenter.loadNewCard();

            }
        });

        return v;
    }

    private void requestCoarseLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                DialogHelper.showOkDialog(
                        getActivity(),
                        R.string.location_permission_predialog_title,
                        R.string.location_permission_predialog_message,
                        new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                            }
                        });
            }
        }
    }

    private void verifyBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(getActivity()).checkAvailability()) {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, BT_REQUEST);
                }
            }
        } catch (RuntimeException e) {
            showBTLENotSupportedMessage();
        }
    }

    private void showBTLENotSupportedMessage() {
        DialogHelper.showOkDialog(getActivity(), R.string.bluetooth_activation_request_title, R.string.bluetooth_activation_request_message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cityStreamPresenter.detachView();
    }

    private void showBTNotEnabledMessage() {
        DialogHelper.showOkDialog(getActivity(), R.string.bluetooth_not_activated_title, R.string.bluetooth_not_activated_message);
    }

    //set beacons collection to list after reading from db
    public static void setBeacons(Collection<BeaconIds> beacons) {
        //getBeaconsFromDB(beacons);
        addBeaconToAdapter(beacons);
    }

    private static void addBeaconToAdapter(final Collection<BeaconIds> beacons) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                if(null != beacons && beacons.size() > 0)
                Log.d(LOG_TAG, "addBeaconToAdapter ;" + beacons.size() + " beacons");
                List<DBBeacon> beaconList = new ArrayList<DBBeacon>();
                for (BeaconIds ids : beacons) {
                    DBBeacon dbBeacon = new DBBeacon();
                    dbBeacon.setIds(ids);
                    beaconList.add(dbBeacon);
                }
                Application.setBeaconList(beaconList);

                cityStreamRecyclerAdapter = new CityStreamRecyclerAdapter(context, Application.getBeaconList());
                mRecyclerView.setAdapter(cityStreamRecyclerAdapter);
                cityStreamRecyclerAdapter.notifyDataSetChanged();
                Log.d(LOG_TAG, "addBeaconToAdapter ;" + cityStreamRecyclerAdapter.getItemCount() + " cityStreamRecyclerAdapter");

            }
        });
    }

    //After scanning reading founded beacons list from dB.
    private static void getBeaconsFromDB(Collection<BeaconIds> beaconCollection) {

        Log.d(LOG_TAG, "getBeaconsFromDB before;" + beaconCollection.size() + " beacons");
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
                Application.clearBeaconList();
                cityStreamRecyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    private static void addBeaconToAdapter(final DBBeacon beacon) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                Application.addBeaconToList(beacon);
                cityStreamRecyclerAdapter.notifyDataSetChanged();
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
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                checkGrantedCoarseLocationPermission(grantResults[0]);
            }
        }
    }



    @Override
    public void onStart() {
        super.onStart();
//        clearAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
//        clearAdapter();
    }
}
