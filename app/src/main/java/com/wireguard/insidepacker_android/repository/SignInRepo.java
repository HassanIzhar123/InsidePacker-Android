package com.wireguard.insidepacker_android.repository;

import static com.wireguard.insidepacker_android.MyApp.getAppContext;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.wireguard.insidepacker_android.Api.ApiClient;
import com.wireguard.insidepacker_android.Interfaces.VolleyCallback;
import com.wireguard.insidepacker_android.models.AccessToken.AccessToken;
import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacker_android.utils.StateLiveData;

import org.json.JSONObject;

public class SignInRepo {
    StateLiveData<AccessToken> signInLiveData;
    ApiClient apiClient;

    public SignInRepo() {
        signInLiveData = new StateLiveData<>();
    }

    public StateLiveData<?> getAccessToken(BasicInformation basicInformation) {
        apiClient = ApiClient.getInstance(getAppContext());
        apiClient.getAccessToken(basicInformation, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                signInLiveData.postSuccess(new Gson().fromJson(String.valueOf(result), AccessToken.class));
            }

            @Override
            public void onError(String message) {
                signInLiveData.postError(message);
            }
        });
        return signInLiveData;
    }
}
