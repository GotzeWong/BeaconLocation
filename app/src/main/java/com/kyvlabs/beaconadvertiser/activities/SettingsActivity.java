package com.kyvlabs.beaconadvertiser.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.kyvlabs.beaconadvertiser.R;
import com.kyvlabs.beaconadvertiser.data.DBHelper;

public class SettingsActivity extends PreferenceActivity {
    private static String LOG_TAG = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private static final java.lang.String LIST_OF_GROUP_DIALOG_TAG = "list_of_group_dialog";

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            Preference buttonNow = findPreference(getString(R.string.settings_key_clear_cache));
            buttonNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DBHelper dbHelper = new DBHelper();
                    dbHelper.removeAllBeaconsData();
                    Log.d(LOG_TAG, "Invalidate cache");
                    return true;
                }
            });

            Preference changeListOfGroups = findPreference(getString(R.string.settings_key_list_of_groups));
            changeListOfGroups.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ListOfGroupsDialog listOfGroupsDialog = new ListOfGroupsDialog();
                    listOfGroupsDialog.show(getFragmentManager(), LIST_OF_GROUP_DIALOG_TAG);
                    return true;
                }
            });
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.settings_key_cache_enabled))) {
                setEnabledCacheSensitivePreferences(sharedPreferences.getBoolean(key, true));
            }
        }

        public void setEnabledCacheSensitivePreferences(boolean enabled) {
            Preference cacheTimePref = findPreference(getString(R.string.settings_key_cache_time));
            cacheTimePref.setEnabled(enabled);
            Preference invalidateCachePref = findPreference(getString(R.string.settings_key_clear_cache));
            invalidateCachePref.setEnabled(enabled);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }


    }
}
