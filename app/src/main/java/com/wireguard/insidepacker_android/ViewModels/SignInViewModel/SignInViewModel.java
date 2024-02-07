package com.wireguard.insidepacker_android.ViewModels.SignInViewModel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.wireguard.insidepacker_android.models.AccessToken.AccessToken;
import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacker_android.Interfaces.ViewModelCallBacks;
import com.wireguard.insidepacker_android.repository.SignInRepo;

import org.json.JSONObject;

public class SignInViewModel extends AndroidViewModel {
    MutableLiveData<AccessToken> accessTokenMutableLiveData;
    MutableLiveData<String> errorMutableLiveData;
    SignInRepo mainHomeRepo;

    public SignInViewModel(@NonNull Application application) {
        super(application);
        mainHomeRepo = new SignInRepo();
        accessTokenMutableLiveData = new MutableLiveData<>();
        errorMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<AccessToken> getAccessTokenMutableLiveData() {
        return accessTokenMutableLiveData;
    }

    public MutableLiveData<String> getErrorMutableLiveData() {
        return errorMutableLiveData;
    }

    public void getAccessToken(Context context, BasicInformation basicInformation) {
        mainHomeRepo.callAccessTokenApi(context, basicInformation, new ViewModelCallBacks() {
            @Override
            public void onSuccess(JSONObject result) {
                accessTokenMutableLiveData.postValue(new Gson().fromJson(result.toString(), AccessToken.class));
            }

            @Override
            public void onSuccess(JSONObject tenantListResult, JSONObject configResult) {

            }

            @Override
            public void onError(String message) {
                errorMutableLiveData.postValue(message);
            }
        });
    }
}
