package com.wireguard.insidepacket_android.Interfaces;

import org.json.JSONObject;

public interface ViewModelCallBacks {
    void onSuccess(JSONObject result);

    void onSuccess(JSONObject tenantListResult, JSONObject configResult);

    void onError(String message);
}
