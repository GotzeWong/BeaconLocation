package com.kyvlabs.brrr2.activities.support.chrometabs;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;

import com.kyvlabs.brrr2.R;

public class CustomTabActivityHelper {

    /**
     * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView
     *
     * @param activity         The host activity
     * @param uri              the Uri to be opened
     * @param fallback         a CustomTabFallback to be used if Custom Tabs is not available
     */
    public static void openCustomTab(Activity activity,
                                     Uri uri,
                                     CustomTabFallback fallback) {
        String packageName = CustomTabsHelper.getPackageNameToUse(activity);

        if (packageName == null) {
            if (fallback != null) {
                fallback.openUri(activity, uri);
            }
        } else {
            CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
            intentBuilder.setToolbarColor(ContextCompat.getColor(activity, R.color.blue));
            intentBuilder.setShowTitle(true);
            intentBuilder.setCloseButtonIcon(BitmapFactory.decodeResource(activity.getResources(),
                    R.drawable.ic_arrow_back));

            intentBuilder.setStartAnimations(activity,
                    R.anim.slide_in_right, R.anim.slide_out_left);
            intentBuilder.setExitAnimations(activity,
                    android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            CustomTabsIntent build = intentBuilder.build();

            build.intent.setPackage(packageName);
            build.launchUrl(activity, uri);
        }
    }

    /**
     * To be used as a fallback to open the Uri when Custom Tabs is not available
     */
    public interface CustomTabFallback {
        /**
         * @param activity The Activity that wants to open the Uri
         * @param uri      The uri to be opened by the fallback
         */
        void openUri(Activity activity, Uri uri);
    }

}