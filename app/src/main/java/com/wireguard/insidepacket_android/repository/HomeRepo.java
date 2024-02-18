package com.wireguard.insidepacket_android.repository;


import android.content.Context;
import android.util.Log;

import com.wireguard.insidepacket_android.Api.ApiClient;
import com.wireguard.insidepacket_android.DataStructure.StaticData;
import com.wireguard.insidepacket_android.Interfaces.ViewModelCallBacks;
import com.wireguard.insidepacket_android.Interfaces.VolleyCallback;

import org.json.JSONObject;

public class HomeRepo {
    ApiClient apiClient;

    public void getHomeUserList(Context context, String accessToken, String tunnel, String username, ViewModelCallBacks callBacks) {
        apiClient = ApiClient.getInstance(context);
        String userListUrl = StaticData.getUserListUrl(tunnel, username);
        //Log.e("userListUrl", userListUrl + " " + accessToken);
        apiClient.homeGetRequest(userListUrl, tunnel, username, accessToken, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject tenantListResult) {

            }

            @Override
            public void onSuccess(JSONObject tenantListResult, JSONObject tunnelJson) {
                callBacks.onSuccess(tenantListResult, tunnelJson);
            }

            @Override
            public void onError(String message) {
                callBacks.onError(message);
            }
        });
    }

    public void accessOrganization(Context context, String accessToken, String username, String tunnel, String tunnelId, ViewModelCallBacks accessOrg) {
        apiClient = ApiClient.getInstance(context);
        String accessOrgUrl = StaticData.getAccessOrgUrl(tunnel, username, tunnelId);
        //Log.e("accessOrgUrl", accessOrgUrl);
        apiClient.homeGetRequest(accessOrgUrl, accessToken, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject tenantListResult) {
                accessOrg.onSuccess(tenantListResult);
            }

            @Override
            public void onSuccess(JSONObject tenantListResult, JSONObject tunnelJson) {

            }

            @Override
            public void onError(String message) {
                accessOrg.onError(message);
            }
        });
    }

    public void getUserList(Context context, String accessToken, String tunnel, String username, ViewModelCallBacks callBacks) {
        apiClient = ApiClient.getInstance(context);
        String userListUrl = StaticData.getUserListUrl(tunnel, username);
        apiClient.getRequest(userListUrl, accessToken, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject tenantListResult) {
                Log.e("userListUrl", "" + tenantListResult);
                callBacks.onSuccess(tenantListResult);
            }

            @Override
            public void onSuccess(JSONObject tenantListResult, JSONObject tunnelJson) {
            }

            @Override
            public void onError(String message) {
                callBacks.onError(message);
            }
        });
    }
}


