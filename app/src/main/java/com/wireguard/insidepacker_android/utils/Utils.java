package com.wireguard.insidepacker_android.utils;

import static com.wireguard.insidepacker_android.utils.SharedPrefsName._PREFS_NAME;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._SETTINGS;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.wireguard.insidepacker_android.R;
import com.wireguard.insidepacker_android.activities.SignInActivity;
import com.wireguard.insidepacker_android.activities.SplashActivity;
import com.wireguard.insidepacker_android.models.settings.Settings;

public class Utils {
    public Settings getSettings(Context context) {
        PreferenceManager pref = new PreferenceManager(context, _PREFS_NAME);
        String defaultJson = """
                {
                  "send_crash_reports": true,
                  "enable_on_launch": true,
                  "automatic_update": true,
                  "tunnels": {
                    "available_tunnels": [],
                    "selected_tunnels": ""
                  },
                  "always_on_vpn": true,
                  "trusted_wifi": []
                }""";
        String json = pref.getValue(_SETTINGS, defaultJson);
        Gson gson = new Gson();
        return gson.fromJson(json, Settings.class);
    }

    public void saveSettings(Context context, Settings settings) {
        PreferenceManager pref = new PreferenceManager(context, _PREFS_NAME);
        Gson gson = new Gson();
        String json = gson.toJson(settings);
        pref.saveValue(_SETTINGS, json);
    }

    public Notification showPermanentNotification(Context mContext, String channelId,String description, int id, PendingIntent pendingIntent) {
        NotificationManager mNotificationManager;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext.getApplicationContext(), channelId);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.app_icon);
        mBuilder.setOngoing(true);
        mBuilder.setContentTitle("InsidePacket");
        mBuilder.setContentText(description);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);
        mBuilder.setAutoCancel(false);
        mBuilder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        mBuilder.setLights(Color.RED, 3000, 3000);
        mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(channel);
        mBuilder.setChannelId(channelId);
        Notification notification = mBuilder.build();
        mNotificationManager.notify(id, notification);
        return notification;
    }
    public Dialog showProgressDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.loading_process_dialog);
        dialog.setCancelable(false);
        return dialog;
    }
}
