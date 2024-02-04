package com.wireguard.insidepacker_android.repository;

import static com.wireguard.insidepacker_android.MyApp.getAppContext;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.wireguard.insidepacker_android.Api.ApiClient;
import com.wireguard.insidepacker_android.Interfaces.VolleyCallback;
import com.wireguard.insidepacker_android.models.AccessToken.AccessToken;
import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;

import org.json.JSONObject;

public class SignInRepo {
    MutableLiveData<AccessToken> signInLiveData;
    ApiClient apiClient;

    public SignInRepo() {
        signInLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<AccessToken> getAccessToken(BasicInformation basicInformation) {
        apiClient = ApiClient.getInstance(getAppContext());
        apiClient.getAccessToken(basicInformation, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                Gson gson = new Gson();
                signInLiveData.postValue(gson.fromJson(String.valueOf(result), AccessToken.class));
            }

            @Override
            public void onError(String message) {
                signInLiveData.postValue(null);
            }
        });
        return signInLiveData;
    }
}
