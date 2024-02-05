package com.wireguard.insidepacker_android.fragments;

import static com.wireguard.android.backend.Tunnel.State.UP;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

public class HomeFragment extends Fragment {
    View view;
    AppCompatActivity mContext;
    HomeViewModel homeViewModel;

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
        homeViewModel = new ViewModelProvider(mContext).get(HomeViewModel.class);
        initViewModel();
        return view;
    }

    private void initViewModel() {
        homeViewModel.getConfig(mContext);
        homeViewModel.getConfigMutableLiveData().observe(mContext, configModel -> {
            if (configModel != null) {
                Toast.makeText(mContext, ""+configModel.toString(), Toast.LENGTH_SHORT).show();
//                initConnection();
            }
        });
        homeViewModel.getErrorMutableLiveData().observe(mContext, s -> {
            // handle error
        });
    }

    private void initConnection() {
        if (getContext() != null) {
            Tunnel tunnel = new Tunnel() {
                @NonNull
                @Override
                public String getName() {
                    return "wgpreconf";
                }

                @Override
                public void onStateChange(State newState) {

                }
            };
            Intent intentPrepare = GoBackend.VpnService.prepare(getContext());
            if (intentPrepare != null) {
                startActivityForResult(intentPrepare, 0);
            }
            Interface.Builder interfaceBuilder = new Interface.Builder();
            Peer.Builder peerBuilder = new Peer.Builder();
            Backend backend = new GoBackend(getContext());

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        backend.setState(tunnel, UP, new Config.Builder().setInterface(interfaceBuilder.addAddress(InetNetwork.parse("10.0.0.2/32")).parsePrivateKey("privatekeybase64").build()).addPeer(peerBuilder.addAllowedIp(InetNetwork.parse("0.0.0.0/0")).setEndpoint(InetEndpoint.parse("yourhost:51820")).parsePublicKey("pubkeybase64").build()).build());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
