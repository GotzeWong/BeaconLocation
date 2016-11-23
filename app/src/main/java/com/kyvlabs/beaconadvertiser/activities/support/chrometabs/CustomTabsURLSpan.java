package com.kyvlabs.beaconadvertiser.activities.support.chrometabs;

import android.app.Activity;
import android.net.Uri;
import android.os.Parcel;
import android.text.style.URLSpan;
import android.view.View;

public class CustomTabsURLSpan extends URLSpan {
    private final Activity activity;

    public CustomTabsURLSpan(String url, Activity activity) {
        super(url);
        this.activity = activity;
    }

    public CustomTabsURLSpan(Parcel src, Activity activity) {
        super(src);
        this.activity = activity;
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        CustomTabActivityHelper.openCustomTab(activity, Uri.parse(url), new ChromeTabFallback());
    }
}