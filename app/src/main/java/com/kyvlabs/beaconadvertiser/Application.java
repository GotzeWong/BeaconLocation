package com.kyvlabs.beaconadvertiser;

import android.content.Context;

import java.io.File;

//Base app class. Used like context Container.
public class Application extends android.app.Application {
    private static Context context;

    public static Context getAppContext() {
        return Application.context;
    }

    public static String getCachePath() {
        File path = context.getExternalCacheDir();
        if (path != null) {
            return path.getPath();
        } else {
            return context.getCacheDir().getPath();
        }
    }

    public void onCreate() {
        super.onCreate();
        Application.context = getApplicationContext();
    }
}
