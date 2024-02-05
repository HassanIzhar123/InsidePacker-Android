package com.wireguard.insidepacker_android.repository;


import android.content.Context;
import android.util.Log;

import com.wireguard.insidepacker_android.Api.ApiClient;
import com.wireguard.insidepacker_android.DataStructure.StaticData;
import com.wireguard.insidepacker_android.Interfaces.ViewModelCallBacks;
import com.wireguard.insidepacker_android.Interfaces.VolleyCallback;
import org.json.JSONObject;

public class HomeRepo {
    ApiClient apiClient;

    public void getConfiguration(Context context, ViewModelCallBacks callBacks) {
        apiClient = ApiClient.getInstance(context);
        Log.e("configUrl", StaticData.configUrl);
        apiClient.getRequest(StaticData.configUrl, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                callBacks.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                callBacks.onError(message);
            }
        });
    }

}


