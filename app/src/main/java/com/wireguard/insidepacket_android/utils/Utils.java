package com.wireguard.insidepacket_android.utils;

import static com.wireguard.insidepacket_android.utils.AppStrings.CHANNEL_ID;
import static com.wireguard.insidepacket_android.utils.AppStrings.CHANNEL_NAME;
import static com.wireguard.insidepacket_android.utils.AppStrings._PREFS_NAME;
import static com.wireguard.insidepacket_android.utils.AppStrings._SETTINGS;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.wireguard.config.InetNetwork;
import com.wireguard.config.ParseException;
import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.activities.SplashActivity;
import com.wireguard.insidepacket_android.models.settings.SettingsModel;
import com.wireguard.insidepacket_android.services.MyVpnService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public SettingsModel getSettings(Context context) {
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
        return gson.fromJson(json, SettingsModel.class);
    }

    public void saveSettings(Context context, SettingsModel settingsModel) {
        PreferenceManager pref = new PreferenceManager(context, _PREFS_NAME);
        Gson gson = new Gson();
        String json = gson.toJson(settingsModel);
        pref.saveValue(_SETTINGS, json);
    }

    public Notification showPermanentNotification(Context mContext, String channelId, String description, int id, PendingIntent pendingIntent) {
        NotificationManager mNotificationManager;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext.getApplicationContext(), channelId);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.app_icon);
        mBuilder.setOngoing(true);
        mBuilder.setContentTitle("InsidePacket");
        mBuilder.setContentText(description);
        mBuilder.setPriority(Notification.PRIORITY_MIN);
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
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(channel);
        mBuilder.setChannelId(channelId);
        Notification notification = mBuilder.build();
        mNotificationManager.notify(id, notification);
        return notification;
    }
    public Notification makeForegroundNotification( Context context, String notificationText) {
        Intent ii = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, ii, PendingIntent.FLAG_IMMUTABLE);
        createNotificationChannel(context);
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setSmallIcon(R.drawable.app_icon);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setContentTitle("InsidePacket");
        notificationBuilder.setContentText(notificationText);
        notificationBuilder.setPriority(Notification.PRIORITY_MIN);
        notificationBuilder.setStyle(bigText);
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        notificationBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        notificationBuilder.setLights(Color.RED, 3000, 3000);
        return notificationBuilder.setOngoing(true)
                .build();
    }

    private void createNotificationChannel(Context context) {
        NotificationChannel chan = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
    }
    public Dialog showProgressDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.loading_process_dialog);
        dialog.setCancelable(false);
        return dialog;
    }

    public List<InetNetwork> parseAllowedIPs(String allowedIPs) throws ParseException {
        List<InetNetwork> allowedIPRanges = new ArrayList<>();
        String[] ipRanges = allowedIPs.split(",");
        for (String ipRange : ipRanges) {
            allowedIPRanges.add(InetNetwork.parse(ipRange.trim()));
        }
        return allowedIPRanges;
    }

    public void showToFullScreen(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    public void sendEmailWithLogs(Context context, String logs) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "your_email"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "WireGuard Logs");
            intent.putExtra(Intent.EXTRA_TEXT, logs);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No Email application found!", Toast.LENGTH_SHORT).show();
        }
    }

    public static double bytesToKB(long bytes) {
        return (double) bytes / 1024;
    }

    public static String formatDecimal(double number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(number);
    }
}
