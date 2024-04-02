package com.wireguard.insidepacket_android.models.BasicInformation;

import com.google.gson.Gson;

public class BasicInformation {
    private String username;
    private String password;

    public String getNewPassword() {
        return new_password;
    }

    public void setNewPassword(String new_password) {
        this.new_password = new_password;
    }

    private String new_password;
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
