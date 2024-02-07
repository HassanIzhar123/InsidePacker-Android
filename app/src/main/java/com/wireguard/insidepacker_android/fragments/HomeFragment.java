package com.wireguard.insidepacker_android.fragments;

import static com.wireguard.android.backend.Tunnel.State.DOWN;
import static com.wireguard.android.backend.Tunnel.State.UP;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._ACCESS_TOKEN;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._IS_TRUSTED_WIFI;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._PREFS_NAME;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._SELECTED_WIFI;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._USER_INFORMATION;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.wireguard.android.backend.Backend;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.Config;
import com.wireguard.config.InetEndpoint;
import com.wireguard.config.InetNetwork;
import com.wireguard.config.Interface;
import com.wireguard.config.Peer;
import com.wireguard.insidepacker_android.R;
import com.wireguard.insidepacker_android.ViewModels.HomeViewModel.HomeViewModel;
import com.wireguard.insidepacker_android.essentials.PersistentConnectionProperties;
import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacker_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacker_android.models.UserTenants.Item;
import com.wireguard.insidepacker_android.utils.PreferenceManager;

import needle.Needle;

public class HomeFragment extends Fragment {
    View view;
    AppCompatActivity mContext;
    HomeViewModel homeViewModel;
    BasicInformation basicInformation;
    PreferenceManager<String> stringPreferenceManager;
    PreferenceManager<Boolean> booleanPreferenceManager;
    Backend backend = PersistentConnectionProperties.getInstance().getBackend();
    boolean isTrustedWifi = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_layout, container, false);
        mContext = (AppCompatActivity) getContext();
        assert mContext != null;
        initializeSharedPreference();
        homeViewModel = new ViewModelProvider(mContext).get(HomeViewModel.class);
        initViewModel();
        return view;
    }

    private void initializeSharedPreference() {
        stringPreferenceManager = new PreferenceManager<>(mContext, _PREFS_NAME);
        booleanPreferenceManager = new PreferenceManager<>(mContext, _PREFS_NAME);
        isTrustedWifi = !(String.join(",", stringPreferenceManager.getValue(_SELECTED_WIFI, "")).isEmpty());
    }

    private void initViewModel() {
        getUserData();
//        homeViewModel.getUserList(mContext, stringPreferenceManager.getValue(_ACCESS_TOKEN, ""), basicInformation.getTenantName(), basicInformation.getUsername());
        homeViewModel.getConnectionMutableLiveData().observe(mContext, connectionModel -> {
            if (connectionModel != null) {
                if (!connectionModel.getUserTenants().getItems().isEmpty()) {
                    Item item = connectionModel.getUserTenants().getItems().get(0);
                    Log.e("CurrentItem", "" + new Gson().toJson(connectionModel.getUserTenants()));
                    initConnection(item, connectionModel.getConfigModel());
                }
            }
        });
        homeViewModel.getErrorUserListMutableList().observe(mContext, s -> {
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        });
    }

    private void getUserData() {
        String json = stringPreferenceManager.getValue(_USER_INFORMATION, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            basicInformation = gson.fromJson(json, BasicInformation.class);
        }
    }

    private void initConnection(Item item, ConfigModel configModel) {
        try {
            backend.getRunningTunnelNames();
        } catch (NullPointerException e) {
            // backend cannot be created without context
            PersistentConnectionProperties.getInstance().setBackend(new GoBackend(mContext));
            backend = PersistentConnectionProperties.getInstance().getBackend();
        }
        connect(mContext, item, configModel);
    }

    public void connect(Context mContext, Item item, ConfigModel configModel) {
        Tunnel tunnel = PersistentConnectionProperties.getInstance().getTunnel();

        Intent intentPrepare = GoBackend.VpnService.prepare(mContext);
        if (intentPrepare != null) {
            startActivityForResult(intentPrepare, 0);
        }
        Interface.Builder interfaceBuilder = new Interface.Builder();
        Peer.Builder peerBuilder = new Peer.Builder();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (backend.getState(PersistentConnectionProperties.getInstance().getTunnel()) == UP) {
                        backend.setState(tunnel, DOWN, null);
                    } else {
                        String allowedIp;
                        if (isTrustedWifi) {
                            allowedIp = configModel.getAllowedIps()
                                    .split(",")[0];
                        } else {
                            allowedIp = configModel.getUntrustedAllowedIps();
                        }
                        Config.Builder builder = new Config.Builder();
                        backend.setState(tunnel, UP, builder.setInterface(interfaceBuilder.addAddress(InetNetwork.parse(item.getTunnelIp())).parsePrivateKey(configModel.getTunnelPrivateKey()).build()).addPeer(peerBuilder.addAllowedIp(InetNetwork.parse(allowedIp)).setEndpoint(InetEndpoint.parse(configModel.getRemoteIp() + ":" + configModel.getRemotePort())).parsePublicKey(item.getPublicKey()).build()).build());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
