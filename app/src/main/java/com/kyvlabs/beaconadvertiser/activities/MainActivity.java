package com.kyvlabs.beaconadvertiser.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.kyvlabs.beaconadvertiser.R;
import com.kyvlabs.beaconadvertiser.data.DBHelper;


//Main activity - first activity to show
public class MainActivity extends BaseActivity {

    private static final int BT_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    @Override
    protected void onStart() {
        super.onStart();
        //Bluetooth enable checking
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
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
        //TODO delete before release
        getMenuInflater().inflate(R.menu.test_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        if (id == R.id.action_db) {
//            startActivity(new Intent(this, DBActivity.class));
//            return true;
//        } else
        if (id == R.id.action_clear_timestamps_from_db) {
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
//        } else if (id == R.id.testbutton) {
//
//            List<BeaconIds> bl = new ArrayList<>();
//            bl.add(new BeaconIds("qwer-1234-asd4-vs1qe-mm3a", "2", "1"));
//            bl.add(new BeaconIds("lsad-123zx-ww12-brak", "4", "5"));
//
//            BeaconNotifier beaconNotifier = new BeaconNotifier(this);
//            beaconNotifier.sendNotificationAboutBeacons(bl);
//
////            List<DBBeacon> beaconsFromDB = TalkActivity.PlaceholderFragment.getBeaconsFromDB(bl);
//            return true;
        } else {
            return super.onOptionsItemSelected(item);

        }
    }


}
