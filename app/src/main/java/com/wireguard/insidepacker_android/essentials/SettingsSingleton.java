package com.wireguard.insidepacker_android.essentials;

import com.wireguard.insidepacker_android.models.settings.Settings;

public class SettingsSingleton {
    private static SettingsSingleton mInstance = null;

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    private Settings settings;

    public static synchronized SettingsSingleton getInstance() {
        if (null == mInstance) {
            mInstance = new SettingsSingleton();
        }
        return mInstance;
    }
}
