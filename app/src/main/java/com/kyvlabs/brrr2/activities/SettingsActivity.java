package com.kyvlabs.brrr2.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.kyvlabs.brrr2.R;
import com.kyvlabs.brrr2.data.DBHelper;

public class SettingsActivity extends PreferenceActivity {
    private static String LOG_TAG = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
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
