package com.kyvlabs.brrr2.utils;

import android.content.Context;

/**
 * Created by Gotze on 2016/6/1.
 */
public class StringHelper {

    public static String getStrRes(Context context, int intResource) {
        return context.getResources().getString(intResource);
    }
}
