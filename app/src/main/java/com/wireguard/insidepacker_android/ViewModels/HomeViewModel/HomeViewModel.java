package com.wireguard.insidepacker_android.ViewModels.HomeViewModel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.wireguard.insidepacker_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacker_android.Interfaces.ViewModelCallBacks;
import com.wireguard.insidepacker_android.models.ConnectionModel.ConnectionModel;
import com.wireguard.insidepacker_android.models.UserTenants.UserTenants;
import com.wireguard.insidepacker_android.repository.HomeRepo;

import org.json.JSONObject;

public class HomeViewModel extends AndroidViewModel {
    MutableLiveData<ConfigModel> configMutableLiveData;
    MutableLiveData<ConnectionModel> ConnectionMutableLiveData;
    MutableLiveData<String> errorConfigMutableLiveData, errorUserListMutableList;
    HomeRepo mainHomeRepo;

    public MutableLiveData<ConnectionModel> getConnectionMutableLiveData() {
        return ConnectionMutableLiveData;
    }

    public void setConnectionMutableLiveData(MutableLiveData<ConnectionModel> connectionMutableLiveData) {
        this.ConnectionMutableLiveData = connectionMutableLiveData;
    }

    public MutableLiveData<String> getErrorUserListMutableList() {
        return errorUserListMutableList;
    }

    public void setErrorUserListMutableList(MutableLiveData<String> errorUserListMutableList) {
        this.errorUserListMutableList = errorUserListMutableList;
    }

    public MutableLiveData<ConfigModel> getConfigMutableLiveData() {
        return configMutableLiveData;
    }

    public MutableLiveData<String> getErrorConfigMutableLiveData() {
        return errorConfigMutableLiveData;
    }

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mainHomeRepo = new HomeRepo();
        ConnectionMutableLiveData = new MutableLiveData<>();
        errorUserListMutableList = new MutableLiveData<>();
        configMutableLiveData = new MutableLiveData<>();
        errorConfigMutableLiveData = new MutableLiveData<>();
    }

    public void getUserList(Context context, String accessToken, String tunnel, String username) {
        mainHomeRepo.getUserList(context, accessToken, tunnel, username, new ViewModelCallBacks() {
            @Override
            public void onSuccess(JSONObject result) {

            }

            @Override
            public void onSuccess(JSONObject tenantListResult, JSONObject configResult) {
                UserTenants userTenants = new Gson().fromJson(tenantListResult.toString(), UserTenants.class);
                ConfigModel configModel = new Gson().fromJson(configResult.toString(), ConfigModel.class);
                ConnectionModel connectionModel = new ConnectionModel();
                connectionModel.setConfigModel(configModel);
                connectionModel.setUserTenants(userTenants);
                ConnectionMutableLiveData.postValue(connectionModel);
            }

            @Override
            public void onError(String message) {
                errorUserListMutableList.postValue(message);
            }
        });
    }
}
