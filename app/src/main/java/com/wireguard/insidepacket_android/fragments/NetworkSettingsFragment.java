package com.wireguard.insidepacket_android.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.ViewModels.HomeViewModel.HomeViewModel;
import com.wireguard.insidepacket_android.adapters.MultiSelectionRecyclerViewAdapter;
import com.wireguard.insidepacket_android.essentials.SettingsSingleton;
import com.wireguard.insidepacket_android.models.settings.TrustedWifi;
import com.wireguard.insidepacket_android.utils.Utils;
import com.wireguard.insidepacket_android.utils.WifiUtils;

import java.util.ArrayList;
import java.util.List;

public class NetworkSettingsFragment extends Fragment {
    View view;
    Context context;
    SettingsSingleton settingsSingleton = SettingsSingleton.getInstance();
    RecyclerView recyclerView;
    TextView emptyWifiText;
    SwitchCompat alwaysOnVpnSwitch;
    Button resetAgentButton;
    MultiSelectionRecyclerViewAdapter adapter;
    List<TrustedWifi> wifiList;
    Boolean isAlwaysOnVpnSwitchTouched = false;
    HomeViewModel homeViewModel;
    List<TrustedWifi> selectWifi = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.network_settings, container, false);
        context = getContext();
        assert context != null;
        initializeViewModels();
        initializeComponents();
        setUi();
        setClickListeners();
        return view;
    }

    private void initializeViewModels() {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    private void setUi() {
        alwaysOnVpnSwitch.setChecked(settingsSingleton.getSettings().getAlwaysOnVpn());
        wifiList = WifiUtils.getPreviouslyConnectedWifiNames();
        selectWifi.clear();
        selectWifi = SettingsSingleton.getInstance().getSettings().getTrustedWifi();
        if (wifiList.isEmpty()) {
            emptyWifiText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyWifiText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new MultiSelectionRecyclerViewAdapter(wifiList);
            recyclerView.setAdapter(adapter);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setClickListeners() {
        alwaysOnVpnSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isAlwaysOnVpnSwitchTouched = true;
                return false;
            }
        });
        alwaysOnVpnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isAlwaysOnVpnSwitchTouched) {
                    isAlwaysOnVpnSwitchTouched = false;
                    settingsSingleton.getSettings().setAlwaysOnVpn(isChecked);
                    new Utils().saveSettings(getContext(), settingsSingleton.getSettings());
                }
            }
        });
        if (adapter != null) {
            adapter.setOnCheckedChangeListener(new MultiSelectionRecyclerViewAdapter.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(int position, boolean isChecked) {
                    //Log.e("isChecked", "onCheckedChanged: " + isChecked);
                    if (isChecked) {
                        wifiList.get(position).setSelected(true);
                        settingsSingleton.getSettings().setTrustedWifi(wifiList);
                    } else {
                        wifiList.get(position).setSelected(false);
                        settingsSingleton.getSettings().setTrustedWifi(wifiList);
                    }
                    new Utils().saveSettings(getContext(), settingsSingleton.getSettings());
                    HomeViewModel homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
                    homeViewModel.transferData(isChecked);
                }
            });
        }
        resetAgentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.resetAgent(getContext());
            }
        });
    }

    private void initializeComponents() {
        recyclerView = view.findViewById(R.id.tunnel_recyclerview);
        emptyWifiText = view.findViewById(R.id.empty_wifi_text);
        alwaysOnVpnSwitch = view.findViewById(R.id.always_on_vpn_switch);
        resetAgentButton = view.findViewById(R.id.reset_agent_button);
    }
}
