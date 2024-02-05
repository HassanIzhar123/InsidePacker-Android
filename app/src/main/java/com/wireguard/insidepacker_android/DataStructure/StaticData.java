package com.wireguard.insidepacker_android.DataStructure;

public class StaticData {
    //    public static String baseUrl = "https://naas.insidepacket.com/broker/users/";
    public static String baseUrl = "https://naas.insidepacket.com/broker/";
    public static String tunnel = "wg_tunnel/";
    public static String user = "users/";
    public static String token = "get_token";
    public static String config = "get_conf/";
    public static String accessTokenUrl = StaticData.baseUrl + StaticData.user + StaticData.token;
    public static String configUrl = StaticData.baseUrl + "/service/" + StaticData.tunnel + StaticData.user + "/get_token";

    public static String getTunnelUrl(String tunnel,String username,) {
        return StaticData.baseUrl + "service/" + tunnel + "/" + StaticData.tunnel + StaticData.config + username+"/"
    }

    ;
}