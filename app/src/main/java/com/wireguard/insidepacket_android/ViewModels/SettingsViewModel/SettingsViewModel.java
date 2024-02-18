package com.wireguard.insidepacket_android.ViewModels.SettingsViewModel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.wireguard.insidepacket_android.Interfaces.ViewModelCallBacks;
import com.wireguard.insidepacket_android.models.ConnectionModel.ConnectionModel;
import com.wireguard.insidepacket_android.models.UserTenants.UserTenants;
import com.wireguard.insidepacket_android.repository.HomeRepo;

import org.json.JSONObject;

import needle.Needle;

public class SettingsViewModel extends AndroidViewModel {
    MutableLiveData<UserTenants> userListMutableLiveData;
    MutableLiveData<String> errorUserListMutableList;
    HomeRepo mainHomeRepo;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        mainHomeRepo = new HomeRepo();
        userListMutableLiveData = new MutableLiveData<>();
        errorUserListMutableList = new MutableLiveData<>();
    }

    public MutableLiveData<UserTenants> getUserListMutableLiveData() {
        return userListMutableLiveData;
    }

    public void getUserList(Context context, String accessToken, String tunnel, String username) {
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                mainHomeRepo.getUserList(context, accessToken, tunnel, username, new ViewModelCallBacks() {
                    @Override
                    public void onSuccess(JSONObject connectionModel) {
                        UserTenants connectionModel1 = new Gson().fromJson(connectionModel.toString(), UserTenants.class);
                        userListMutableLiveData.postValue(connectionModel1);
                    }

                    @Override
                    public void onSuccess(JSONObject tenantListResult, JSONObject configResult) {
                    }

                    @Override
                    public void onError(String message) {
                        errorUserListMutableList.postValue(message);
                    }
                });
            }
        });
    }
}