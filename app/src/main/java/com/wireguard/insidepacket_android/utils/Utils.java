package com.wireguard.insidepacket_android.utils;

import static com.wireguard.insidepacket_android.utils.SharedPrefsName._PREFS_NAME;
import static com.wireguard.insidepacket_android.utils.SharedPrefsName._SETTINGS;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.wireguard.config.InetNetwork;
import com.wireguard.config.ParseException;
import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.models.settings.SettingsModel;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

    /**
     * Convert a IPv4 address from an integer to an InetAddress.
     *
     * @param hostAddress an int corresponding to the IPv4 address in network byte order
     */
    public InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    public List<InetNetwork> parseAllowedIPs(String allowedIPs) throws ParseException {
        List<InetNetwork> allowedIPRanges = new ArrayList<>();
        String[] ipRanges = allowedIPs.split(",");
        for (String ipRange : ipRanges) {
            allowedIPRanges.add(InetNetwork.parse(ipRange.trim()));
        }
        return allowedIPRanges;
    }

    public void askForPermission(Activity activity) {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                Manifest.permission.RECEIVE_BOOT_COMPLETED
        };

        List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        String[] permissionsArray = new String[permissionsToRequest.size()];
        permissionsArray = permissionsToRequest.toArray(permissionsArray);
        if (permissionsArray.length > 0) {
            ActivityCompat.requestPermissions(activity, permissionsArray, 100);
        }
    }

    public void showToFullScreen(Activity activity) {
        Window window = activity.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
}
