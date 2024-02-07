package com.wireguard.insidepacker_android.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class WifiUtils {

    // Method to get a list of configured WiFi networks
    public static List<String> getPreviouslyConnectedWifiNames(Context context) {
        List<String> connectedWifiNames = new ArrayList<>();
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("permissions","Permission not granted");
            return null;
        }
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

        if (configuredNetworks != null) {
            for (WifiConfiguration config : configuredNetworks) {
                Log.e("wifiStatus", "" + config.status);
                if (config.status == WifiConfiguration.Status.CURRENT) {
                    connectedWifiNames.add(config.SSID);
                }
            }
        }

        return connectedWifiNames;
    }
}