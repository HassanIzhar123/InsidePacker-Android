package com.wireguard.insidepacket_android.ViewModels.HomeViewModel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.wireguard.insidepacket_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacket_android.Interfaces.ViewModelCallBacks;
import com.wireguard.insidepacket_android.models.ConnectionModel.ConnectionModel;
import com.wireguard.insidepacket_android.models.UserTenants.UserTenants;
import com.wireguard.insidepacket_android.repository.HomeRepo;

import org.json.JSONObject;

import needle.Needle;

public class HomeViewModel extends AndroidViewModel {
    MutableLiveData<ConfigModel> configMutableLiveData;
    MutableLiveData<ConnectionModel> ConnectionMutableLiveData;
    MutableLiveData<String> timeLeftMutableLiveData;
    MutableLiveData<String> errorConfigMutableLiveData;
    MutableLiveData<String> errorUserListMutableList;
    MutableLiveData<String> errorTimeLeftMutableList;
    MutableLiveData<Object> dataTransferMutableLiveData;
    MutableLiveData<String> ipAddressMutableLiveData;
    MutableLiveData<Object> ipAddressErrorMutableList;
    HomeRepo mainHomeRepo;

    public MutableLiveData<Object> getDataTransferMutableLiveData() {
        return dataTransferMutableLiveData;
    }

    public void setDataTransferMutableLiveData(MutableLiveData<Object> dataTransferMutableLiveData) {
        this.dataTransferMutableLiveData = dataTransferMutableLiveData;
    }

    public MutableLiveData<String> getErrorTimeLeftMutableList() {
        return errorTimeLeftMutableList;
    }

    public void setErrorTimeLeftMutableList(MutableLiveData<String> errorTimeLeftMutableList) {
        this.errorTimeLeftMutableList = errorTimeLeftMutableList;
    }


    public MutableLiveData<String> getTimeLeftMutableLiveData() {
        return timeLeftMutableLiveData;
    }

    public void setTimeLeftMutableLiveData(MutableLiveData<String> timeLeftMutableLiveData) {
        this.timeLeftMutableLiveData = timeLeftMutableLiveData;
    }

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

    public MutableLiveData<String> getIpAddressMutableLiveData() {
        return ipAddressMutableLiveData;
    }

    public void setIpAddressMutableLiveData(MutableLiveData<String> ipAddressMutableLiveData) {
        this.ipAddressMutableLiveData = ipAddressMutableLiveData;
    }

    public MutableLiveData<Object> getIpAddressErrorMutableList() {
        return ipAddressErrorMutableList;
    }

    public void setIpAddressErrorMutableList(MutableLiveData<Object> ipAddressErrorMutableList) {
        this.ipAddressErrorMutableList = ipAddressErrorMutableList;
    }

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mainHomeRepo = new HomeRepo();
        ConnectionMutableLiveData = new MutableLiveData<>();
        errorUserListMutableList = new MutableLiveData<>();
        configMutableLiveData = new MutableLiveData<>();
        errorConfigMutableLiveData = new MutableLiveData<>();
        timeLeftMutableLiveData = new MutableLiveData<>();
        errorTimeLeftMutableList = new MutableLiveData<>();
        dataTransferMutableLiveData = new MutableLiveData<>();
        ipAddressMutableLiveData = new MutableLiveData<>();
        ipAddressErrorMutableList = new MutableLiveData<>();
        int i = 1 / 0;
    }

    public void getUserList(Context context, String accessToken, String tunnel, String username) {
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    public void accessOrganization(Context context, String accessToken, String tunnel, String tunnelId, String username) {
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                mainHomeRepo.accessOrganization(context, accessToken, username, tunnel, tunnelId, new ViewModelCallBacks() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        //Log.e("accessOrganization", result.toString());
                        timeLeftMutableLiveData.postValue(result.optString("end_time"));
                    }

                    @Override
                    public void onSuccess(JSONObject tenantListResult, JSONObject configResult) {
                    }

                    @Override
                    public void onError(String message) {
                        //for testing only
                        timeLeftMutableLiveData.postValue("180000");
                        errorTimeLeftMutableList.postValue(message);
                    }
                });
            }
        });
    }

    public void getIpAddress(Context context) {
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                mainHomeRepo.getIpAddress(context, new ViewModelCallBacks() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        ipAddressMutableLiveData.postValue(result.optString("ip"));
                    }

                    @Override
                    public void onSuccess(JSONObject tenantListResult, JSONObject configResult) {
                    }

                    @Override
                    public void onError(String message) {
                        ipAddressErrorMutableList.postValue(message);
                    }
                });
            }
        });
    }

    public void transferData(Object data) {
        dataTransferMutableLiveData.postValue(data);
    }
}
