
package com.wireguard.insidepacker_android.models.settings;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tunnels implements Serializable {

    @NonNull
    @Override
    public String toString() {
        return "Tunnels{" +
                "availableTunnels=" + availableTunnels +
                ", selectedTunnels=" + selectedTunnels +
                '}';
    }

    @SerializedName("available_tunnels")
    @Expose
    private List<AvailableTunnel> availableTunnels;
    @SerializedName("selected_tunnels")
    @Expose
    private String selectedTunnels;
    private final static long serialVersionUID = 248177766530067660L;

    public List<AvailableTunnel> getAvailableTunnels() {
        return availableTunnels;
    }

    public void setAvailableTunnels(List<AvailableTunnel> availableTunnels) {
        this.availableTunnels = availableTunnels;
    }

    public String getSelectedTunnels() {
        return selectedTunnels;
    }

    public void setSelectedTunnels(String selectedTunnels) {
        this.selectedTunnels = selectedTunnels;
    }

}
