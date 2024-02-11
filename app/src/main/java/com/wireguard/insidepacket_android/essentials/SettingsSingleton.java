package com.wireguard.insidepacket_android.essentials;

import com.wireguard.insidepacket_android.models.settings.Settings;

public class SettingsSingleton {
    private static SettingsSingleton mInstance = null;

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    private Settings settings;

    public Boolean getTunnelConnected() {
        return isTunnelConnected;
    }

    public void setTunnelConnected(Boolean tunnelConnected) {
        isTunnelConnected = tunnelConnected;
    }

    private Boolean isTunnelConnected;

    public static synchronized SettingsSingleton getInstance() {
        if (null == mInstance) {
            mInstance = new SettingsSingleton();
        }
        return mInstance;
    }
}
