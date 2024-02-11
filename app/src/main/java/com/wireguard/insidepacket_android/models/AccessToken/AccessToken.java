package com.wireguard.insidepacket_android.models.AccessToken;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AccessToken implements Serializable {
    @SerializedName("access_token")
    @Expose
    private String access_token;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
