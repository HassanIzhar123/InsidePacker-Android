package com.wireguard.insidepacket_android.models.ConnectedTunnelModel;

import com.wireguard.insidepacket_android.essentials.SettingsSingleton;

public class ConnectedTunnelModel {
    boolean isConnected;
    String tunnelIp;
    String publicIp;
    String gateway;
    private static ConnectedTunnelModel mInstance = null;

    public static synchronized ConnectedTunnelModel getInstance() {
        if (null == mInstance) {
            mInstance = new ConnectedTunnelModel();
        }
        return mInstance;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getTunnelIp() {
        return tunnelIp;
    }

    public void setTunnelIp(String tunnelIp) {
        this.tunnelIp = tunnelIp;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }
}
