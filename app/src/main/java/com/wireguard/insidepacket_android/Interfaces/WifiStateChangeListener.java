package com.wireguard.insidepacket_android.Interfaces;

import com.wireguard.insidepacket_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacket_android.models.UserTenants.Item;

public interface WifiStateChangeListener {
    void onWifiStateChanged(boolean isConnected, boolean isTrusted, ConfigModel configModel, Item item);

    void onTrafficSent(String traffic, String publicIp, String tunnelIp);

    void onError(String error);
}