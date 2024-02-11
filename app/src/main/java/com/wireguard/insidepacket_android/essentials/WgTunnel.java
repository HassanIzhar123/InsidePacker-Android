package com.wireguard.insidepacket_android.essentials;

import androidx.annotation.NonNull;

import com.wireguard.android.backend.Tunnel;

public class WgTunnel implements Tunnel {
    @NonNull
    @Override
    public String getName() {
        return "wgpreconf";
    }

    @Override
    public void onStateChange(@NonNull State newState) {
    }
}