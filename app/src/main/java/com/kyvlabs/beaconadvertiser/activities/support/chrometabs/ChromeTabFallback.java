package com.kyvlabs.beaconadvertiser.activities.support.chrometabs;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.kyvlabs.beaconadvertiser.R;

public class ChromeTabFallback implements CustomTabActivityHelper.CustomTabFallback {
    @Override
    public void openUri(Activity activity, Uri uri) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.fail_to_open_browser, Toast.LENGTH_LONG).show();
        }
    }
}