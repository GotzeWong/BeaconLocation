package com.kyvlabs.brrr2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Log;

import com.kyvlabs.brrr2.activities.AdvertActivity;
import com.kyvlabs.brrr2.data.BeaconIds;
import com.kyvlabs.brrr2.data.DBBeacon;
import com.kyvlabs.brrr2.data.DBHelper;
import com.kyvlabs.brrr2.data.DataKeys;
import com.kyvlabs.brrr2.views.HtmlTagHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import rx.functions.Action1;
import rx.functions.Func1;

//Class witch create and show notifications about beacons
public class BeaconNotifier {
    //TODO set from properties
    private static final long NOT_SHOW_INTERVAL = 1000 * 60;
    private AtomicInteger notificationCounter = new AtomicInteger(0);
    private Map<String, Integer> beaconNotificationIndexes = new HashMap<>();
    private Context applicationContext;
    private NotificationManager notificationManager;
    private DBHelper dbHelper;

    public BeaconNotifier(Context context) {
        applicationContext = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        dbHelper = new DBHelper();
    }

    //cancel all notifications
    public void cancelAll() {
        notificationManager.cancelAll();
    }

    //create notifications about all beacons
    public synchronized void sendNotificationAboutBeacons(Collection<BeaconIds> idsList) {
        SharedPreferences userDetails = Application.getAppContext().getSharedPreferences(Application.getAppContext().getString(R.string.saved_auth_key), Context.MODE_PRIVATE);
        String auth = userDetails.getString(Application.getAppContext().getString(R.string.saved_auth_key), "");

        //TODO fizme
        dbHelper.getBeaconsByIds(idsList, auth)
                .filter(new Func1<DBBeacon, Boolean>() {
                    @Override
                    public Boolean call(DBBeacon beacon) {
                        return beacon.getNextShowTime() < System.currentTimeMillis();
                    }
                })
                .subscribe(new Action1<DBBeacon>() {
                    @Override
                    public void call(DBBeacon beacon) {
                        Log.d("BEACON", beacon.toString());
                        beaconNotify(beacon);
                        dbHelper.setNextShowTime(beacon.getIds(), System.currentTimeMillis() + NOT_SHOW_INTERVAL);

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d("BEACON", throwable.getMessage(), throwable);
                    }
                });
    }

    //generate notification message about beacon
    private void beaconNotify(DBBeacon dbBeacon) {
        Intent intent = new Intent(applicationContext, AdvertActivity.class);
        Log.d("DATA", "title = " + dbBeacon.getTitle() + " picture = " + dbBeacon.getPicture() + " description = " + dbBeacon.getDescription());
        intent.putExtra(DataKeys.BEACON_IDS, dbBeacon.getIds());
        intent.putExtra(DataKeys.AD_TITLE_KEY, dbBeacon.getTitle());
        intent.putExtra(DataKeys.AD_PICTURE_KEY, dbBeacon.getPicture());
        intent.putExtra(DataKeys.AD_TIME_KEY, dbBeacon.getNextUpdateTime());
        intent.putExtra(DataKeys.AD_LINK_KEY, dbBeacon.getLink());
        intent.putExtra(DataKeys.AD_DESCRIPTION_KEY, dbBeacon.getDescription());
        //Says that advert called by walk.
        intent.putExtra(DataKeys.ADVERT_REASON_KEY, DataKeys.ADVERT_WALK_REASON);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        applicationContext,
                        getNotificationIndex(dbBeacon),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        Notification notification = new Notification.Builder(applicationContext)
                .setContentTitle(dbBeacon.getTitle())
                .setContentText(Html.fromHtml(dbBeacon.getDescription(), null, new HtmlTagHandler()))
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(applicationContext.getResources(), R.drawable.ic_launcher))
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(getNotificationIndex(dbBeacon), notification);
    }

    //counter. used for unificating notifications
    private int getNotificationIndex(DBBeacon beacon) {
        String stringIndex = beacon.getIds().toString();
        Integer index = beaconNotificationIndexes.get(stringIndex);
        if (index == null) {
            index = notificationCounter.incrementAndGet();
            beaconNotificationIndexes.put(stringIndex, index);
        }
        return index;
    }
}
