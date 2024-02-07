package com.wireguard.insidepacker_android.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wireguard.insidepacker_android.R;
import com.wireguard.insidepacker_android.adapters.MultiSelectionRecyclerViewAdapter;
import com.wireguard.insidepacker_android.essentials.SettingsSingleton;
import com.wireguard.insidepacker_android.utils.Utils;
import com.wireguard.insidepacker_android.utils.WifiUtils;

import java.util.List;

public class NetworkSettingsFragment extends Fragment {
    View view;
    Context context;
    SettingsSingleton settingsSingleton = SettingsSingleton.getInstance();
    RecyclerView recyclerView;
    TextView emptyWifiText;
    Button submitButton;
    SwitchCompat alwaysOnVpnSwitch;
    MultiSelectionRecyclerViewAdapter adapter;
    List<String> wifiList;
    Boolean isAlwaysOnVpnSwitchTouched = false;

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
        initializeComponents();
        setUi();
        setClickListeners();
        return view;
    }

    private void setUi() {
        Log.e("AlwaysOnVpn", "" + settingsSingleton.getSettings().getAlwaysOnVpn());
        alwaysOnVpnSwitch.setChecked(settingsSingleton.getSettings().getAlwaysOnVpn());
        wifiList = WifiUtils.getPreviouslyConnectedWifiNames(context);
        Log.e("WifiNames", "" + wifiList);
        if (wifiList != null) {
            if (wifiList.isEmpty()) {
                emptyWifiText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
            } else {
                emptyWifiText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.VISIBLE);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                adapter = new MultiSelectionRecyclerViewAdapter(wifiList);
                recyclerView.setAdapter(adapter);
            }
        } else {
            emptyWifiText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            submitButton.setVisibility(View.GONE);
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
        submitButton.setOnClickListener(v -> {
            SparseBooleanArray selectedItems = adapter.getSelectedItems();
            for (int i = 0; i < selectedItems.size(); i++) {
                int position = selectedItems.keyAt(i);
                if (selectedItems.get(position)) {
                    String selectedItem = wifiList.get(position);
                    Log.e("Selected Item", selectedItem);
                }
            }
        });
    }

    private void initializeComponents() {
        recyclerView = view.findViewById(R.id.tunnel_recyclerview);
        emptyWifiText = view.findViewById(R.id.empty_wifi_text);
        submitButton = view.findViewById(R.id.submitButton);
        alwaysOnVpnSwitch = view.findViewById(R.id.always_on_vpn_switch);
        submitButton = view.findViewById(R.id.submitButton);
    }
}
