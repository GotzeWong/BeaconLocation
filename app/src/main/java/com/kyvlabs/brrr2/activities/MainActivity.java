package com.kyvlabs.brrr2.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.kyvlabs.brrr2.Application;
import com.kyvlabs.brrr2.BeaconNotifier;
import com.kyvlabs.brrr2.R;
import com.kyvlabs.brrr2.activities.fragment.CityStreamFragment;
import com.kyvlabs.brrr2.activities.support.DBActivity;
import com.kyvlabs.brrr2.data.BeaconIds;
import com.kyvlabs.brrr2.data.DBHelper;
import com.kyvlabs.brrr2.services.WalkService;
import com.kyvlabs.brrr2.utils.StringHelper;

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

import butterknife.ButterKnife;


//Main activity - first activity to show
public class MainActivity extends AppCompatActivity
        implements BeaconConsumer, NavigationView.OnNavigationItemSelectedListener{

    private static final int BT_REQUEST = 101;

    private static final int LAYOUT = R.layout.activity_main;
    private BeaconManager beaconManager;
    protected static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        stopService(new Intent(this, WalkService.class));

        if (savedInstanceState == null) {

            CityStreamFragment listFragment = new CityStreamFragment();

            Application.setFragmentManager(getSupportFragmentManager());
            FragmentTransaction fragmentTransaction = Application.getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.container, listFragment);
            fragmentTransaction.commit();

        }

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        beaconManager = BeaconManager.getInstanceForApplication(this);
        List<BeaconParser> beaconParsers = beaconManager.getBeaconParsers();
        beaconParsers.add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //beaconManager parametrizations
        beaconManager.setBackgroundScanPeriod(Long.valueOf(sharedPreferences.getString(StringHelper.getStrRes(Application.getAppContext(),R.string.settings_key_background_scan_period), StringHelper.getStrRes(Application.getAppContext(),R.string.settings_default_background_scan_period))));
        beaconManager.setBackgroundBetweenScanPeriod(Long.valueOf(sharedPreferences.getString(StringHelper.getStrRes(Application.getAppContext(),R.string.settings_key_background_between_scan_period), StringHelper.getStrRes(Application.getAppContext(),(R.string.settings_default_background_between_scan_period)))));
        beaconManager.setForegroundScanPeriod(Long.valueOf(sharedPreferences.getString(StringHelper.getStrRes(Application.getAppContext(),R.string.settings_key_foreground_scan_period), StringHelper.getStrRes(Application.getAppContext(),R.string.settings_default_foreground_scan_period))));
        beaconManager.setForegroundBetweenScanPeriod(Long.valueOf(sharedPreferences.getString(StringHelper.getStrRes(Application.getAppContext(),R.string.settings_key_foreground_between_scan_period), StringHelper.getStrRes(Application.getAppContext(),R.string.settings_default_foreground_between_scan_period))));
    }

    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(LOG_TAG, "onServiceConnected");
        }
        public void onServiceDisconnected(ComponentName name) {
            Log.v(LOG_TAG, "onServiceDisconnected");
        }
    };

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

                Log.d(LOG_TAG, "FOUNDED " + beacons.size() + " beacons");

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Integer beacon_range = Integer.valueOf(sharedPreferences.getString(StringHelper.getStrRes(Application.getAppContext(),R.string.settings_key_beacon_range), StringHelper.getStrRes(Application.getAppContext(),R.string.settings_default_beacon_range)));
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
            beaconManager.startRangingBeaconsInRegion(new Region(StringHelper.getStrRes(Application.getAppContext(),R.string.region_name), null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private Collection<BeaconIds> getIdsFromBeacons(Collection<Beacon> beacons) {
        Collection<BeaconIds> beaconIdsList = new ArrayList<>();
        for (Beacon beacon : beacons) {
            BeaconIds beaconIds = new BeaconIds(beacon.getId1().toString(), beacon.getId2().toString(), beacon.getId3().toString());
            beaconIds.setRssi(beacon.getRssi());
            beaconIdsList.add(beaconIds);
        }
        return beaconIdsList;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Bluetooth enable checking
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null != mBluetoothAdapter && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BT_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BT_REQUEST) {
            if (resultCode != RESULT_OK) {
                finish();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //TODO delete before release
        if (id == R.id.action_db) {
            startActivity(new Intent(this, DBActivity.class));
            return true;
        } else if (id == R.id.action_clear_timestamps_from_db) {
            DBHelper dbHelper = new DBHelper();
            dbHelper.clearAllNextShowTimes();
            return true;
        } else if (id == R.id.logout_button) {
            SharedPreferences preferences = getSharedPreferences(getString(R.string.saved_auth_key), 0);
            preferences.edit().remove(getString(R.string.saved_auth_key)).commit();

            FacebookSdk.sdkInitialize(getApplicationContext());
            LoginManager.getInstance().logOut();

            finish();
            return true;
        } else if (id == R.id.testbutton) {

            List<BeaconIds> bl = new ArrayList<>();
            bl.add(new BeaconIds("qwer-1234-asd4-vs1qe-mm3a", "2", "1"));
            bl.add(new BeaconIds("lsad-123zx-ww12-brak", "4", "5"));

            BeaconNotifier beaconNotifier = new BeaconNotifier(this);
            beaconNotifier.sendNotificationAboutBeacons(bl);

//            List<DBBeacon> beaconsFromDB = TalkActivity.PlaceholderFragment.getBeaconsFromDB(bl);
            return true;
        } else if (id == R.id.action_settings) {

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all) {
            // Handle the camera action
        } else if (id == R.id.nav_nearby) {
            startService(new Intent(this, WalkService.class));
            this.finish();
        } else if (id == R.id.nav_fav) {

        } else if (id == R.id.nav_search) {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {

            SharedPreferences preferences = Application.getAppContext().getSharedPreferences(getString(R.string.saved_auth_key), MODE_PRIVATE);
            preferences.edit().remove(getString(R.string.saved_auth_key)).commit();

            FacebookSdk.sdkInitialize(getApplicationContext());
            LoginManager.getInstance().logOut();

            stopService(new Intent(this, WalkService.class));

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

            finish();
            return true;

        } else if (id == R.id.nav_about) {

            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        //stopService(new Intent(this, WalkService.class));
        super.onDestroy();
    }
}
