package com.wireguard.insidepacker_android.models.ConfigModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ConfigModel implements Serializable {

    @SerializedName("tunnel_id")
    @Expose
    private String tunnelId;
    @SerializedName("tunnel_ip")
    @Expose
    private String tunnelIp;
    @SerializedName("tunnel_private_key")
    @Expose
    private String tunnelPrivateKey;
    @SerializedName("tunnel_DNS")
    @Expose
    private String tunnelDNS;
    @SerializedName("public_key")
    @Expose
    private String publicKey;
    @SerializedName("psk")
    @Expose
    private String psk;
    @SerializedName("allowed_ips")
    @Expose
    private String allowedIps;
    @SerializedName("untrusted_allowed_ips")
    @Expose
    private String untrustedAllowedIps;
    @SerializedName("remote_ip")
    @Expose
    private String remoteIp;
    @SerializedName("remote_port")
    @Expose
    private String remotePort;

    public String getTunnelId() {
        return tunnelId;
    }

    public void setTunnelId(String tunnelId) {
        this.tunnelId = tunnelId;
    }

    public String getTunnelIp() {
        return tunnelIp;
    }

    public void setTunnelIp(String tunnelIp) {
        this.tunnelIp = tunnelIp;
    }

    public String getTunnelPrivateKey() {
        return tunnelPrivateKey;
    }

    public void setTunnelPrivateKey(String tunnelPrivateKey) {
        this.tunnelPrivateKey = tunnelPrivateKey;
    }

    public String getTunnelDNS() {
        return tunnelDNS;
    }

    public void setTunnelDNS(String tunnelDNS) {
        this.tunnelDNS = tunnelDNS;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPsk() {
        return psk;
    }

    public void setPsk(String psk) {
        this.psk = psk;
    }

    public String getAllowedIps() {
        return allowedIps;
    }

    public void setAllowedIps(String allowedIps) {
        this.allowedIps = allowedIps;
    }

    public String getUntrustedAllowedIps() {
        return untrustedAllowedIps;
    }

    public void setUntrustedAllowedIps(String untrustedAllowedIps) {
        this.untrustedAllowedIps = untrustedAllowedIps;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(String remotePort) {
        this.remotePort = remotePort;
    }
}