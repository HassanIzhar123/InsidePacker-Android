package com.wireguard.insidepacket_android.models.settings;

import androidx.annotation.NonNull;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TrustedWifi implements Serializable {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("isSelected")
    @Expose
    private Boolean isSelected;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public TrustedWifi(String name, Boolean isSelected) {
        this.name = name;
        this.isSelected = isSelected;
    }

    @NonNull
    @Override
    public String toString() {
        return "TrustedWifi{" +
                "name='" + name + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

}
