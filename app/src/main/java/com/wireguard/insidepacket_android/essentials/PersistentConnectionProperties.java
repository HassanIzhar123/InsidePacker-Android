package com.wireguard.insidepacket_android.essentials;

import android.util.Log;

import com.wireguard.android.backend.GoBackend;
public class PersistentConnectionProperties {
    private static PersistentConnectionProperties mInstance = null;

    private WgTunnel tunnel;
    private GoBackend backend;


    public WgTunnel getTunnel() {
        try {
            tunnel.getName();
        } catch (NullPointerException e) {
            tunnel = new WgTunnel();
        }
        //Log.e("tunnel naem",""+tunnel);
        return tunnel;
    }

    public GoBackend getBackend() {
        return backend;
    }

    public void setBackend(GoBackend backend) {
        this.backend = backend;
    }

    public static synchronized PersistentConnectionProperties getInstance() {
        if (null == mInstance) {
            mInstance = new PersistentConnectionProperties();
        }
        return mInstance;
    }
}