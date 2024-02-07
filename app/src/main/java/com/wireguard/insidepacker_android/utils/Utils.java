package com.wireguard.insidepacker_android.utils;

import static com.wireguard.insidepacker_android.utils.SharedPrefsName._PREFS_NAME;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._SETTINGS;

import android.content.Context;

import com.google.gson.Gson;
import com.wireguard.insidepacker_android.models.settings.Settings;

public class Utils {
    public Settings getSettings(Context context) {
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
                  "selected_trusted_wifi": []
                }""";
        String json = pref.getValue(_SETTINGS, defaultJson);
        Gson gson = new Gson();
        return gson.fromJson(json, Settings.class);
    }
    public void saveSettings(Context context, Settings settings) {
        PreferenceManager pref = new PreferenceManager(context, _PREFS_NAME);
        Gson gson = new Gson();
        String json = gson.toJson(settings);
        pref.saveValue(_SETTINGS, json);
    }
}
