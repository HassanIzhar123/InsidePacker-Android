package com.wireguard.insidepacker_android.utils;

public class AppUrls {
    public AppUrls() {
    }

    String mainUrl = "https://naas.insidepacket.com/broker/";
    String accessTokenUrl = mainUrl + "users/get_token";

    public String getAccessTokenUrl() {
        return accessTokenUrl;
    }

    public void setAccessTokenUrl(String accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }

}
