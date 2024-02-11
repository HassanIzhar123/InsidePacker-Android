package com.wireguard.insidepacket_android.models.ConnectionModel;

import com.wireguard.insidepacket_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacket_android.models.UserTenants.UserTenants;

public class ConnectionModel {
    UserTenants userTenants;

    public UserTenants getUserTenants() {
        return userTenants;
    }

    public void setUserTenants(UserTenants userTenants) {
        this.userTenants = userTenants;
    }

    public ConfigModel getConfigModel() {
        return configModel;
    }

    public void setConfigModel(ConfigModel configModel) {
        this.configModel = configModel;
    }

    ConfigModel configModel;
}
