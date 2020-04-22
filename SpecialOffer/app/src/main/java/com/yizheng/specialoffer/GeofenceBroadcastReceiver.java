package com.yizheng.specialoffer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel";
    private static final String TAG = "GeofenceBroadcastReceiv";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error: " + geofencingEvent.getErrorCode());
            return;
        }
        
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        for (Geofence g : triggeringGeofences) {

            // Here use the ID to get details stored.
            FenceData fd = FenceManager.getFenceData(g.getRequestId());
            sendNotification(context, fd);
        }
    }

    public void sendNotification(Context context, FenceData fd) {

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            String name = context.getString(R.string.app_name);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    name, NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(channel);
        }


        

        Intent intent = new Intent(context, ActivityFromNotification.class);
        intent.putExtra("fence_data", fd);

        PendingIntent pi = PendingIntent.getActivity(context, getUniqueId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.fence_notif2)
                .setContentTitle("Notification from '" + fd.getId() + "' Geofence")
                .setSubText(fd.getId()) // small text at top left
                .setContentText(fd.getAddress()) // Detail info
                .setVibrate(new long[] {1, 1, 1})
                .setAutoCancel(true)
                .setLights(0xff0000ff, 300, 1000) // blue color
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();

        notificationManager.notify(getUniqueId(), notification);
    }

    private static int getUniqueId() {
        return(int) (System.currentTimeMillis() % 10000);
    }
}
