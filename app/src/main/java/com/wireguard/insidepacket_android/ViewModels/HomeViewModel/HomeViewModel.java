package com.wireguard.insidepacket_android.ViewModels.HomeViewModel;

import static com.wireguard.android.backend.Tunnel.State.DOWN;
import static com.wireguard.android.backend.Tunnel.State.UP;
import static com.wireguard.insidepacket_android.utils.AppStrings._PREFS_NAME;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.wireguard.android.backend.Backend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.insidepacket_android.activities.MainActivity;
import com.wireguard.insidepacket_android.activities.SplashActivity;
import com.wireguard.insidepacket_android.essentials.PersistentConnectionProperties;
import com.wireguard.insidepacket_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacket_android.Interfaces.ViewModelCallBacks;
import com.wireguard.insidepacket_android.models.ConnectedTunnelModel.ConnectedTunnelModel;
import com.wireguard.insidepacket_android.models.ConnectionModel.ConnectionModel;
import com.wireguard.insidepacket_android.models.UserTenants.UserTenants;
import com.wireguard.insidepacket_android.repository.HomeRepo;
import com.wireguard.insidepacket_android.utils.Utils;

import org.json.JSONObject;

import needle.Needle;

public class HomeViewModel extends AndroidViewModel {
    MutableLiveData<ConfigModel> configMutableLiveData;
    MutableLiveData<ConnectionModel> ConnectionMutableLiveData;
    MutableLiveData<String> accessOrganizationLiveData;
    MutableLiveData<String> errorConfigMutableLiveData;
    MutableLiveData<String> errorUserListMutableList;
    MutableLiveData<String> timeLeftErrorLiveData;
    MutableLiveData<Object> dataTransferMutableLiveData;
    MutableLiveData<String> ipAddressErrorMutableList;
    HomeRepo mainHomeRepo;

    public MutableLiveData<Object> getDataTransferMutableLiveData() {
        return dataTransferMutableLiveData;
    }

    public void setDataTransferMutableLiveData(MutableLiveData<Object> dataTransferMutableLiveData) {
        this.dataTransferMutableLiveData = dataTransferMutableLiveData;
    }

    public MutableLiveData<String> getTimeLeftErrorLiveData() {
        return timeLeftErrorLiveData;
    }

    public void setTimeLeftErrorLiveData(MutableLiveData<String> timeLeftErrorLiveData) {
        this.timeLeftErrorLiveData = timeLeftErrorLiveData;
    }


    public MutableLiveData<String> getAccessOrganizationLiveData() {
        return accessOrganizationLiveData;
    }

    public void setAccessOrganizationLiveData(MutableLiveData<String> accessOrganizationLiveData) {
        this.accessOrganizationLiveData = accessOrganizationLiveData;
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

    public MutableLiveData<String> getIpAddressErrorMutableList() {
        return ipAddressErrorMutableList;
    }

    public void setIpAddressErrorMutableList(MutableLiveData<String> ipAddressErrorMutableList) {
        this.ipAddressErrorMutableList = ipAddressErrorMutableList;
    }

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mainHomeRepo = new HomeRepo();
        ConnectionMutableLiveData = new MutableLiveData<>();
        errorUserListMutableList = new MutableLiveData<>();
        configMutableLiveData = new MutableLiveData<>();
        errorConfigMutableLiveData = new MutableLiveData<>();
        accessOrganizationLiveData = new MutableLiveData<>();
        timeLeftErrorLiveData = new MutableLiveData<>();
        dataTransferMutableLiveData = new MutableLiveData<>();
        ipAddressErrorMutableList = new MutableLiveData<>();
    }

    public void getUserList(Context context, String accessToken, String tunnel, String username) {
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                mainHomeRepo.getHomeUserList(context, accessToken, tunnel, username, new ViewModelCallBacks() {
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
                        accessOrganizationLiveData.postValue(result.optString("end_time"));
                    }

                    @Override
                    public void onSuccess(JSONObject tenantListResult, JSONObject configResult) {
                    }

                    @Override
                    public void onError(String message) {
                        timeLeftErrorLiveData.postValue(message);
                    }
                });
            }
        });
    }

    public void transferData(Object data) {
        dataTransferMutableLiveData.postValue(data);
    }

    public void resetAgent(Context context) {
        _disconnectVpn(context);
    }

    public void _disconnectVpn(Context context) {
        try {
            Backend backend = PersistentConnectionProperties.getInstance().getBackend();
            Tunnel tunnel = PersistentConnectionProperties.getInstance().getTunnel();
            backend.setState(tunnel, DOWN, null);
            ConnectedTunnelModel connectedTunnelModel = new ConnectedTunnelModel();
            connectedTunnelModel.setConnected(false);
            new Utils().saveConnectedTunnel(context, connectedTunnelModel);
            SharedPreferences sharedPreferences = context.getSharedPreferences(_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            editor.commit();
            context.startActivity(new Intent(context, MainActivity.class));
        } catch (Exception e) {
            Log.e("wifireciever", e.toString() + "" + e.getStackTrace().toString());
        }
//                mContext.unregisterReceiver(wifiReceiver);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}
