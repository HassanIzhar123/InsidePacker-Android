
package com.wireguard.insidepacker_android.models.ConfigModel;

import java.io.Serializable;

public class ConfigModel implements Serializable {
    private String publicKey;
    private String tunnelid;
    private String tunnelPrivateKey;
    private String remoteip;
    private String allowedips;
    private String tunnelDNS;
    private String remotePort;
    private String psk;
    private String untrustedAllowedips;
    private String tunnelip;

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String value) { this.publicKey = value; }

    public String getTunnelid() { return tunnelid; }
    public void setTunnelid(String value) { this.tunnelid = value; }

    public String getTunnelPrivateKey() { return tunnelPrivateKey; }
    public void setTunnelPrivateKey(String value) { this.tunnelPrivateKey = value; }

    public String getRemoteip() { return remoteip; }
    public void setRemoteip(String value) { this.remoteip = value; }

    public String getAllowedips() { return allowedips; }
    public void setAllowedips(String value) { this.allowedips = value; }

    public String getTunnelDNS() { return tunnelDNS; }
    public void setTunnelDNS(String value) { this.tunnelDNS = value; }

    public String getRemotePort() { return remotePort; }
    public void setRemotePort(String value) { this.remotePort = value; }

    public String getPsk() { return psk; }
    public void setPsk(String value) { this.psk = value; }

    public String getUntrustedAllowedips() { return untrustedAllowedips; }
    public void setUntrustedAllowedips(String value) { this.untrustedAllowedips = value; }

    public String getTunnelip() { return tunnelip; }
    public void setTunnelip(String value) { this.tunnelip = value; }
}
