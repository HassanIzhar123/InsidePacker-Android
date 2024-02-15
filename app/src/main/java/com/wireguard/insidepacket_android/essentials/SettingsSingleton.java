package com.wireguard.insidepacket_android.essentials;

import com.wireguard.insidepacket_android.models.settings.SettingsModel;

public class SettingsSingleton {
    private static SettingsSingleton mInstance = null;

    public SettingsModel getSettings() {
        return settingsModel;
    }

    public void setSettings(SettingsModel settingsModel) {
        this.settingsModel = settingsModel;
    }

    private SettingsModel settingsModel;

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
