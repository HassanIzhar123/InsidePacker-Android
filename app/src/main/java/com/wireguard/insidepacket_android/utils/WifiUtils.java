package com.wireguard.insidepacket_android.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.wireguard.insidepacket_android.essentials.SettingsSingleton;
import com.wireguard.insidepacket_android.models.settings.TrustedWifi;

import java.util.ArrayList;
import java.util.List;

public class WifiUtils {

    public static void saveConnectedWifi(Context context) {
        final String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE};
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }
        Log.e("deniedpermission", "" + deniedPermissions.size());
        if (deniedPermissions.isEmpty()) {
            TrustedWifi connectedWifi = getConnectedWifiSSID(context);
            if (connectedWifi != null) {
                List<TrustedWifi> wifiList = getPreviouslyConnectedWifiNames();
                if (!checkIfWifiAlreadyExists(wifiList, connectedWifi)) {
                    wifiList.add(connectedWifi);
                    saveWifiList(context, wifiList);
                }
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

        //Log.e("TAG", "permission: " + permission + " = \t\t" +
//                (res == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));

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

    public static WifiInfo getWifiInfo(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            return wifiManager.getConnectionInfo();
        }
        return null;
    }

    public static String getConnectionType(Context context) {
        String connectionType = "unknown";

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    connectionType = "wifi";
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    connectionType = "mobile";
                }
            }
        }

        return connectionType;
    }

    public static String getBSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                return wifiInfo.getBSSID();
            }
        }
        return null;
    }

    public static int getRSSI(Context context) {
        WifiInfo wifiInfo = getWifiInfo(context);
        if (wifiInfo != null) {
            return wifiInfo.getRssi();
        }
        return 0;
    }

//    public static int getNoise(Context context) {
//        WifiInfo wifiInfo = getWifiInfo(context);
//        if (wifiInfo != null) {
//            return wifiInfo.getNoise();
//        }
//        return 0;
//    }

    public static String getPhyMode(Context context) {
        WifiInfo wifiInfo = getWifiInfo(context);
        if (wifiInfo != null) {
            int frequency = wifiInfo.getFrequency();
            if (frequency >= 2412 && frequency <= 2484) {
                return "802.11b/g";
            } else if (frequency >= 5170 && frequency <= 5825) {
                return "802.11a";
            } else if (frequency >= 5180 && frequency <= 5805) {
                return "802.11ac";
            } else {
                return "Unknown";
            }
        }
        return "Unknown";
    }

    public static String getSSID(Context context) {
        WifiInfo wifiInfo = getWifiInfo(context);
        if (wifiInfo != null) {
            return wifiInfo.getSSID();
        }
        return null;
    }

    public static int getWifiChannel(Context context) {
        WifiInfo wifiInfo = getWifiInfo(context);
        if (wifiInfo != null) {
            int frequency = wifiInfo.getFrequency();
            return convertFrequencyToChannel(frequency);
        }
        return -1; // Indicating failure to get the channel
    }

    private static int convertFrequencyToChannel(int frequency) {
        if (frequency >= 2412 && frequency <= 2484) {
            return (frequency - 2412) / 5 + 1;
        } else if (frequency >= 5170 && frequency <= 5825) {
            return (frequency - 5170) / 5 + 34;
        } else if (frequency >= 5180 && frequency <= 5805) {
            return (frequency - 5180) / 5 + 36;
        } else {
            return -1; // Invalid frequency
        }
    }

    public static int getNoise(Context context) {
        WifiInfo wifiInfo = getWifiInfo(context);
        if (wifiInfo != null) {
            int rssi = wifiInfo.getRssi();
            int snr = wifiInfo.getLinkSpeed(); // For demonstration purposes, you can use getLinkSpeed as SNR
            return snr - rssi;
        }
        return 0; // Default value if unable to get noise
    }

    public static String getWifiSecurity(Context context, String ssid) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return "";
            }
            for (WifiConfiguration wifiConfig : wifiManager.getConfiguredNetworks()) {
                if (wifiConfig.SSID != null && wifiConfig.SSID.equals("\"" + ssid + "\"")) {
                    return getSecurityType(wifiConfig);
                }
            }
        }
        return "Unknown"; // Default value if security type cannot be determined
    }

    private static String getSecurityType(WifiConfiguration wifiConfig) {
        if (wifiConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return "WPA";
        }
        if (wifiConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                wifiConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return "WPA_EAP";
        }
        if (wifiConfig.wepKeys[0] != null) {
            return "WEP";
        }
        return "Open";
    }
}
