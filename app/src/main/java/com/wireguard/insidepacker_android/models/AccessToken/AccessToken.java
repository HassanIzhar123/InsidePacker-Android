package com.wireguard.insidepacker_android.models.AccessToken;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AccessToken implements Serializable {
    @SerializedName("access_token")
    @Expose
    private Boolean access_token;

    public Boolean getAccess_token() {
        return access_token;
    }

    public void setAccess_token(Boolean access_token) {
        this.access_token = access_token;
    }
}
