package com.wireguard.insidepacket_android.services;

import static com.wireguard.insidepacket_android.utils.AppStrings.CHANNEL_ID;
import static com.wireguard.insidepacket_android.utils.AppStrings.CHANNEL_NAME;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.utils.Utils;

public class BroadcastService extends Service {
    private final static String TAG = "BroadcastService";
    public static final String COUNTDOWN_BR = "ACCESS_ORGANIZING_SERVICE";
    Intent bi = new Intent(COUNTDOWN_BR);
    CountDownTimer cdt = null;

    @Override
    public void onCreate() {
        bi.putExtra("countdownTimerFinished", false);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (cdt != null) cdt.cancel();
        Log.i(TAG, "Timer cancelled");
        bi.putExtra("countdownTimerFinished", true);
        sendBroadcast(bi);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String maxCountString = intent.getStringExtra("maxCountDownValue");
        Log.e("maxCountString", "" + maxCountString);
        if (maxCountString != null) {
            long maxCount = Long.parseLong(maxCountString);
            cdt = new CountDownTimer(maxCount, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                    bi.putExtra("countdown", millisUntilFinished);
                    bi.putExtra("countdownTimerFinished", false);
                    sendBroadcast(bi);
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "Timer finished");
                    bi.putExtra("countdownTimerFinished", true);
                    sendBroadcast(bi);
                    stopForeground(true);
                    stopSelf();
                }
            };
            cdt.start();
        }
        Log.i(TAG, "Starting timer...");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, BroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Utils().showPermanentNotification(this, CHANNEL_ID, "Accessing organization...", 1, pendingIntent);
        startForeground(1, notification);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }
}