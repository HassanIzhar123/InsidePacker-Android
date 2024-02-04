package com.wireguard.insidepacker_android.Interfaces;

import org.json.JSONObject;

public interface VolleyCallback {
    void onSuccess(JSONObject result);

    void onError(String message);
}
