package com.wireguard.insidepacker_android.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.wireguard.insidepacker_android.essentials.SettingsSingleton;
import com.wireguard.insidepacker_android.models.settings.TrustedWifi;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WifiUtils {

    public static void saveConnectedWifi(Context context) {
        TrustedWifi connectedWifi = getConnectedWifiSSID(context);
        if (connectedWifi != null) {
            List<TrustedWifi> wifiList = getPreviouslyConnectedWifiNames();
            if (!checkIfWifiAlreadyExists(wifiList, connectedWifi)) {
                wifiList.add(connectedWifi);
                saveWifiList(context, wifiList);
            }
        }
    }

    private static boolean checkIfWifiAlreadyExists(List<TrustedWifi> wifiList, TrustedWifi connectedWifi) {
        for (TrustedWifi wifi : wifiList) {
            if (wifi.getName().equals(connectedWifi.getName())) {
                return true;
            }
        }
        return false;
    }

    private static TrustedWifi getConnectedWifiSSID(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert networkInfo != null;
        if (networkInfo.isConnected()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return new TrustedWifi(wifiInfo.getSSID().replace("\"", ""), false);
        }
        return null;
    }

    public static List<TrustedWifi> getPreviouslyConnectedWifiNames() {
        return SettingsSingleton.getInstance().getSettings().getTrustedWifi();
    }

    public static void saveWifiList(Context context, List<TrustedWifi> wifiList) {
        SettingsSingleton.getInstance().getSettings().setTrustedWifi(wifiList);
        new Utils().saveSettings(context, SettingsSingleton.getInstance().getSettings());
    }

    /**
     * Determines if the context calling has the required permission
     *
     * @param context    - the IPC context
     * @param permission - The permissions to check
     * @return true if the IPC has the granted permission
     */
    public static boolean hasPermission(Context context, String permission) {

        int res = context.checkCallingOrSelfPermission(permission);

        Log.e("TAG", "permission: " + permission + " = \t\t" +
                (res == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));

        return res == PackageManager.PERMISSION_GRANTED;

    }

    /**
     * Determines if the context calling has the required permissions
     *
     * @param context     - the IPC context
     * @param permissions - The permissions to check
     * @return true if the IPC has the granted permission
     */
    public static boolean hasPermissions(Context context, String... permissions) {

        boolean hasAllPermissions = true;

        for (String permission : permissions) {
            //you can return false instead of assigning, but by assigning you can log all permission values
            if (!hasPermission(context, permission)) {
                hasAllPermissions = false;
            }
        }

        return hasAllPermissions;

    }
}
