package com.wireguard.insidepacket_android.Interfaces;

import org.json.JSONObject;

public interface VolleyCallback {
    void onSuccess(JSONObject result);
    void onSuccess(JSONObject tenantListResult,JSONObject tunnelJson);

    void onError(String message);
}
