package com.wireguard.insidepacket_android.models;

public class Diagnostics {
    private String rssi;
    private String connectionType;
    private String bssid;
    private String channel;
    private String ipAddress;
    private String ssid;
    private String portName;
    private String txRate;
    private String phyMode;
    private String routerip;
    private String tunnelStatus;
    private String name;
    private String noise;
    private String ssidSecurity;
    private String ssidTrustStatus;
    private long timestamp;

    public String getRssi() { return rssi; }
    public void setRssi(String value) { this.rssi = value; }

    public String getConnectionType() { return connectionType; }
    public void setConnectionType(String value) { this.connectionType = value; }

    public String getBssid() { return bssid; }
    public void setBssid(String value) { this.bssid = value; }

    public String getChannel() { return channel; }
    public void setChannel(String value) { this.channel = value; }

    public String getipAddress() { return ipAddress; }
    public void setipAddress(String value) { this.ipAddress = value; }

    public String getssid() { return ssid; }
    public void setssid(String value) { this.ssid = value; }

    public String getPortName() { return portName; }
    public void setPortName(String value) { this.portName = value; }

    public String getTxRate() { return txRate; }
    public void setTxRate(String value) { this.txRate = value; }

    public String getPhyMode() { return phyMode; }
    public void setPhyMode(String value) { this.phyMode = value; }

    public String getRouterip() { return routerip; }
    public void setRouterip(String value) { this.routerip = value; }

    public String getTunnelStatus() { return tunnelStatus; }
    public void setTunnelStatus(String value) { this.tunnelStatus = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getNoise() { return noise; }
    public void setNoise(String value) { this.noise = value; }

    public String getssidSecurity() { return ssidSecurity; }
    public void setssidSecurity(String value) { this.ssidSecurity = value; }

    public String getssidTrustStatus() { return ssidTrustStatus; }
    public void setssidTrustStatus(String value) { this.ssidTrustStatus = value; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long value) { this.timestamp = value; }
}
