package com.wireguard.insidepacket_android.services;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.wireguard.insidepacket_android.services.MyVpnService;

public class WifiReceiver extends BroadcastReceiver {

    private final MyVpnService vpnService;
    private String previousSSID = null;
    private boolean isFirstBroadcast = true;

    public WifiReceiver(MyVpnService vpnService) {
        this.vpnService = vpnService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            if (isFirstBroadcast) {
                isFirstBroadcast = false;
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String currentSSID = wifiInfo.getSSID();
                if (previousSSID == null || !previousSSID.equals(currentSSID)) {
                    vpnService.startVpn();
                    Log.e("wifiChanging", "Wifi is CHanged Connecting to VPN");
                    Toast.makeText(context, "wifi changed", Toast.LENGTH_SHORT).show();
                    previousSSID = currentSSID;
                }
            }
        }
    }
}