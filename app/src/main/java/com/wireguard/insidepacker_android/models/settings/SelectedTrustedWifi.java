package com.wireguard.insidepacker_android.models.settings;

import androidx.annotation.NonNull;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SelectedTrustedWifi implements Serializable {

    @SerializedName("id")
    @Expose
    private Integer id;

    @NonNull
    @Override
    public String toString() {
        return "SelectedTrustedWifi{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @SerializedName("name")
    @Expose
    private String name;
    private final static long serialVersionUID = 6419391212798902112L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
