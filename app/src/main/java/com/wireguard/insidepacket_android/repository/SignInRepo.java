package com.wireguard.insidepacket_android.repository;

import android.content.Context;
import com.wireguard.insidepacket_android.Api.ApiClient;
import com.wireguard.insidepacket_android.DataStructure.StaticData;
import com.wireguard.insidepacket_android.Interfaces.ViewModelCallBacks;
import com.wireguard.insidepacket_android.Interfaces.VolleyCallback;
import com.wireguard.insidepacket_android.models.BasicInformation.BasicInformation;
import org.json.JSONObject;

public class SignInRepo {
    ApiClient apiClient;

    public void callAccessTokenApi(Context context, BasicInformation basicInformation, ViewModelCallBacks callBacks) {
        apiClient = ApiClient.getInstance(context);
        try {
            apiClient.postRequest(StaticData.accessTokenUrl, new JSONObject(basicInformation.toJson()), new VolleyCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    callBacks.onSuccess(result);
                }

                @Override
                public void onSuccess(JSONObject tenantListResult, JSONObject tunnelJson) {

                }

                @Override
                public void onError(String message) {
                    callBacks.onError(message);
                }
            });
        } catch (Exception e) {
            callBacks.onError(e.getMessage());
        }
    }

}


