package com.kyvlabs.beaconadvertiser.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.kyvlabs.beaconadvertiser.R;

//Base activity Used to show settings menu at all activities
public abstract class BaseActivity extends Activity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //TODO delete before release
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
