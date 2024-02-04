package com.wireguard.insidepacker_android.models.BasicInformation;

import com.google.gson.Gson;

public class BasicInformation {
    private String username;
    private String password;
    private String tenant_name;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getTenantName() {
        return tenant_name;
    }

    public void setTenantName(String tenant_name) {
        this.tenant_name = tenant_name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
