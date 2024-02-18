package com.wireguard.insidepacket_android.fragments;

import static com.wireguard.insidepacket_android.utils.AppStrings._ACCESS_TOKEN;
import static com.wireguard.insidepacket_android.utils.AppStrings._PREFS_NAME;
import static com.wireguard.insidepacket_android.utils.AppStrings._USER_INFORMATION;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.ViewModels.SettingsViewModel.SettingsViewModel;
import com.wireguard.insidepacket_android.adapters.TunnelSelectionRecyclerViewAdapter;
import com.wireguard.insidepacket_android.essentials.SettingsSingleton;
import com.wireguard.insidepacket_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacket_android.models.UserTenants.Item;
import com.wireguard.insidepacket_android.models.UserTenants.UserTenants;
import com.wireguard.insidepacket_android.models.settings.Tunnels;
import com.wireguard.insidepacket_android.utils.PreferenceManager;
import com.wireguard.insidepacket_android.utils.Utils;

import java.util.List;

public class GeneralSettingsFragment extends Fragment {
    View view;
    AppCompatActivity mContext;
    SettingsSingleton settingsSingleton = SettingsSingleton.getInstance();
    RecyclerView recyclerView;
    TextView emptyTextView;
    SwitchCompat enableOnLaunchSwitch, automaticUpdatesSwitch;
    SettingsViewModel settingsViewModel;
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
        PreferenceManager preferenceManager = new PreferenceManager(mContext, _PREFS_NAME);
        settingsViewModel = new ViewModelProvider(mContext).get(SettingsViewModel.class);
        String json = preferenceManager.getValue(_USER_INFORMATION, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            BasicInformation basicInformation = gson.fromJson(json, BasicInformation.class);
            settingsViewModel.getUserList(mContext, preferenceManager.getValue(_ACCESS_TOKEN, ""), basicInformation.getTenantName(), basicInformation.getUsername());
        }
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
        settingsViewModel.getUserListMutableLiveData().observe(getViewLifecycleOwner(), new Observer<UserTenants>() {
            @Override
            public void onChanged(UserTenants connectionModel) {
                if (connectionModel != null) {
                    if (connectionModel.getItems() != null) {
                        emptyTextView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        List<Item> items = connectionModel.getItems();
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        TunnelSelectionRecyclerViewAdapter adapter = new TunnelSelectionRecyclerViewAdapter(connectionModel.getItems()); // Provide your data list here
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
            }
        });
    }

}
