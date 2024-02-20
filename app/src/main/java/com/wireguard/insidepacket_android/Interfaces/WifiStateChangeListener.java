package com.wireguard.insidepacket_android.Interfaces;

public interface WifiStateChangeListener {
    void onWifiStateChanged(boolean isConnected);

    void onTrafficSent(String traffic);
}