package com.kyvlabs.brrr2.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogHelper {
    public static void showOkDialog(Context context, int title, int message) {
        DialogInterface.OnDismissListener onDismissListener = new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        };
        showOkDialog(context, title, message, onDismissListener);
    }

    public static void showOkDialog(Context context, int title, int message, DialogInterface.OnDismissListener dismissListener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(dismissListener);
        builder.setCancelable(false);
        builder.show();
    }
}
