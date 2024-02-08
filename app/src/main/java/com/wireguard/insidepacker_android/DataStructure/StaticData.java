package com.wireguard.insidepacker_android.DataStructure;

public class StaticData {
    //    public static String baseUrl = "https://naas.insidepacket.com/broker/users/";
    public static String baseUrl = "https://naas.insidepacket.com/broker/";
    public static String tunnel = "wg_tunnel/";
    public static String trusted_access = "trusted_access/";
    public static String user = "users/";
    public static String token = "get_token";
    public static String config = "get_conf/";
    public static String userList = "get_user_list/";
    public static String accessTokenUrl = StaticData.baseUrl + StaticData.user + StaticData.token;
    public static String configUrl = StaticData.baseUrl + "/service/" + StaticData.tunnel + StaticData.user + "/get_token";

    public static String getTunnelUrl(String tunnel, String username, String tenantId) {
        return StaticData.baseUrl + "service/" + tunnel + "/" + StaticData.tunnel + StaticData.config + username + "/" + tenantId;
    }

    public static String getUserListUrl(String tunnel, String username) {
        return StaticData.baseUrl + "service/" + tunnel + "/" + StaticData.tunnel + userList + username;
    }

    public static String getAccessOrgUrl(String tunnel, String username, String tunnelId) {
        return StaticData.baseUrl + "service/" + tunnel + "/" + StaticData.trusted_access + username + "/" + tunnelId ;
    }
}