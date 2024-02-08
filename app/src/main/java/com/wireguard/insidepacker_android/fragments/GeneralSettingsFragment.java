package com.wireguard.insidepacker_android.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wireguard.insidepacker_android.R;
import com.wireguard.insidepacker_android.ViewModels.HomeViewModel.HomeViewModel;
import com.wireguard.insidepacker_android.adapters.TunnelSelectionRecyclerViewAdapter;
import com.wireguard.insidepacker_android.essentials.SettingsSingleton;
import com.wireguard.insidepacker_android.models.UserTenants.Item;
import com.wireguard.insidepacker_android.models.settings.Settings;
import com.wireguard.insidepacker_android.models.settings.Tunnels;
import com.wireguard.insidepacker_android.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class GeneralSettingsFragment extends Fragment {
    View view;
    AppCompatActivity mContext;
    SettingsSingleton settingsSingleton = SettingsSingleton.getInstance();
    RecyclerView recyclerView;
    TextView emptyTextView;
    SwitchCompat enableOnLaunchSwitch, automaticUpdatesSwitch;
    HomeViewModel homeViewModel;
    Boolean isEnableOnLaunchSwitchTouched = false;
    Boolean isAutomaticUpdateSwitchTouched = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.general_settings, container, false);
        recyclerView = view.findViewById(R.id.tunnel_recyclerview);
        emptyTextView = view.findViewById(R.id.empty_tunnels_text);
        enableOnLaunchSwitch = view.findViewById(R.id.enable_on_launch_switch);
        automaticUpdatesSwitch = view.findViewById(R.id.automatic_updates_switch);
        mContext = (AppCompatActivity) getContext();
        assert mContext != null;
        homeViewModel = new ViewModelProvider(mContext).get(HomeViewModel.class);
        setUi();
        setClickListeners();
        setHomeViewModel(recyclerView, emptyTextView);

        return view;
    }

    private void setUi() {
        enableOnLaunchSwitch.setChecked(settingsSingleton.getSettings().getEnableOnLaunch());
        automaticUpdatesSwitch.setChecked(settingsSingleton.getSettings().getAutomaticUpdate());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setClickListeners() {
        enableOnLaunchSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isEnableOnLaunchSwitchTouched = true;
                return false;
            }
        });
        automaticUpdatesSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isAutomaticUpdateSwitchTouched = true;
                return false;
            }
        });
        enableOnLaunchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isEnableOnLaunchSwitchTouched) {
                    isEnableOnLaunchSwitchTouched = false;
                    settingsSingleton.getSettings().setEnableOnLaunch(isChecked);
                    new Utils().saveSettings(getContext(), settingsSingleton.getSettings());
                }
            }
        });
        automaticUpdatesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isAutomaticUpdateSwitchTouched) {
                    isAutomaticUpdateSwitchTouched = false;
                    settingsSingleton.getSettings().setAlwaysOnVpn(isChecked);
                    new Utils().saveSettings(getContext(), settingsSingleton.getSettings());
                }
            }
        });
    }

    private void setHomeViewModel(RecyclerView recyclerView, TextView emptyTextView) {
        homeViewModel.getConnectionMutableLiveData().observe(mContext, connectionModel -> {
            if (connectionModel != null) {
                if (!connectionModel.getUserTenants().getItems().isEmpty()) {
                    emptyTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    List<Item> items = connectionModel.getUserTenants().getItems();
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                    TunnelSelectionRecyclerViewAdapter adapter = new TunnelSelectionRecyclerViewAdapter(connectionModel.getUserTenants().getItems()); // Provide your data list here
                    recyclerView.setAdapter(adapter);
                    adapter.onCLickListener(new TunnelSelectionRecyclerViewAdapter.TunnelSelectionListener() {
                        @Override
                        public void onTunnelSelected(int position) {
                            recyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    Tunnels tunnels = new Tunnels();
                                    tunnels.setSelectedTunnels(items.get(position).getTunnelIp());
                                    SettingsSingleton.getInstance().getSettings().setTunnels(tunnels);
                                    new Utils().saveSettings(getContext(), settingsSingleton.getSettings());

                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                } else {
                    emptyTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            } else {
                emptyTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }
}
