package com.wireguard.insidepacket_android.models.settings;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SettingsModel implements Serializable {
    @SerializedName("send_crash_reports")
    @Expose
    private Boolean sendCrashReports;
    @SerializedName("enable_on_launch")
    @Expose
    private Boolean enableOnLaunch;
    @SerializedName("automatic_update")
    @Expose
    private Boolean automaticUpdate;
    @SerializedName("tunnels")
    @Expose
    private Tunnels tunnels;
    @SerializedName("always_on_vpn")
    @Expose
    private Boolean alwaysOnVpn;
    @SerializedName("trusted_wifi")
    @Expose
    private List<TrustedWifi> trustedWifi;

    public Boolean getSendCrashReports() {
        return sendCrashReports;
    }

    public void setSendCrashReports(Boolean sendCrashReports) {
        this.sendCrashReports = sendCrashReports;
    }

    public Boolean getEnableOnLaunch() {
        return enableOnLaunch;
    }

    public void setEnableOnLaunch(Boolean enableOnLaunch) {
        this.enableOnLaunch = enableOnLaunch;
    }

    public Boolean getAutomaticUpdate() {
        return automaticUpdate;
    }

    public void setAutomaticUpdate(Boolean automaticUpdate) {
        this.automaticUpdate = automaticUpdate;
    }

    public Tunnels getTunnels() {
        return tunnels;
    }

    public void setTunnels(Tunnels tunnels) {
        this.tunnels = tunnels;
    }

    public Boolean getAlwaysOnVpn() {
        return alwaysOnVpn;
    }

    public void setAlwaysOnVpn(Boolean alwaysOnVpn) {
        this.alwaysOnVpn = alwaysOnVpn;
    }

    public List<TrustedWifi> getTrustedWifi() {
        return trustedWifi;
    }

    public void setTrustedWifi(List<TrustedWifi> trustedWifi) {
        this.trustedWifi = trustedWifi;
    }


    @NonNull
    @Override
    public String toString() {
        return "Settings{" +
                "sendCrashReports=" + sendCrashReports +
                ", enableOnLaunch=" + enableOnLaunch +
                ", automaticUpdate=" + automaticUpdate +
                ", tunnels=" + tunnels +
                ", alwaysOnVpn=" + alwaysOnVpn +
                ", selectedTrustedWifi=" + trustedWifi +
                '}';
    }
}
