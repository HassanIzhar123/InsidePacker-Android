package com.wireguard.insidepacker_android.Interfaces;

import org.json.JSONObject;

public interface ViewModelCallBacks {
    void onSuccess(JSONObject result);

    void onError(String message);
}
