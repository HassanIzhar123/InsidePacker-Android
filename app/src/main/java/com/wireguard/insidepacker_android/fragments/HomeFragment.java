package com.wireguard.insidepacker_android.fragments;

import static com.wireguard.android.backend.Tunnel.State.DOWN;
import static com.wireguard.android.backend.Tunnel.State.UP;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._ACCESS_TOKEN;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._PREFS_NAME;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._USER_INFORMATION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.wireguard.android.backend.Backend;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Statistics;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.Config;
import com.wireguard.config.InetEndpoint;
import com.wireguard.config.InetNetwork;
import com.wireguard.config.Interface;
import com.wireguard.config.Peer;
import com.wireguard.insidepacker_android.R;
import com.wireguard.insidepacker_android.ViewModels.HomeViewModel.HomeViewModel;
import com.wireguard.insidepacker_android.essentials.PersistentConnectionProperties;
import com.wireguard.insidepacker_android.essentials.SettingsSingleton;
import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacker_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacker_android.models.UserTenants.Item;
import com.wireguard.insidepacker_android.utils.PreferenceManager;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import needle.Needle;

public class HomeFragment extends Fragment {
    View view;
    AppCompatActivity mContext;
    HomeViewModel homeViewModel;
    BasicInformation basicInformation;
    PreferenceManager preferenceManager;
    Backend backend = PersistentConnectionProperties.getInstance().getBackend();
    boolean isTrustedWifi = false;
    Button connectedButton, disconnectButton, accessResourcesBtn;
    TextView networkStatus;
    ImageView disconnectedLayout, connectedLayout, disconnectedCloudImage, connectedCloudImage;
    RelativeLayout notTrustedCloudImage, trustedTextLayout;
    LinearLayout wifiStatusLayout;
    TextView wifiNameText, trustedNetworkText, timeLeftText;
    Item currentItem;

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
        initViews();
        initViewModel(mContext);
        onClickListeners();
        return view;
    }

    private void onClickListeners() {
        connectedButton.setOnClickListener(v -> {
            homeViewModel.getUserList(mContext, preferenceManager.getValue(_ACCESS_TOKEN, ""), basicInformation.getTenantName(), basicInformation.getUsername());

        });

        disconnectButton.setOnClickListener(v -> {
            try {
                backend.getRunningTunnelNames();
            } catch (NullPointerException e) {
                PersistentConnectionProperties.getInstance().setBackend(new GoBackend(mContext));
                backend = PersistentConnectionProperties.getInstance().getBackend();
            }
            AsyncTask.execute(() -> {
                try {
                    backend.setState(PersistentConnectionProperties.getInstance().getTunnel(), DOWN, null);
                    toggleViews(false, isTrustedWifi);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        accessResourcesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentItem != null) {
                    homeViewModel.accessOrganization(mContext,
                            preferenceManager.getValue(_ACCESS_TOKEN, ""),
                            currentItem.getTenantName(),
                            currentItem.getTunnelId(),
                            basicInformation.getUsername()
                    );
                }
            }
        });
    }

    private void initViews() {
        connectedButton = view.findViewById(R.id.connected_button);
        disconnectButton = view.findViewById(R.id.disconnected_button);
        networkStatus = view.findViewById(R.id.network_status);
        disconnectedLayout = view.findViewById(R.id.disconnected_layout);
        connectedLayout = view.findViewById(R.id.connected_layout);
        connectedCloudImage = view.findViewById(R.id.connected_cloud_image);
        disconnectedCloudImage = view.findViewById(R.id.disconnected_cloud_image);
        notTrustedCloudImage = view.findViewById(R.id.not_trusted_cloud_image);
        wifiStatusLayout = view.findViewById(R.id.wifi_status_layout);
        accessResourcesBtn = view.findViewById(R.id.access_resources_btn);
        trustedTextLayout = view.findViewById(R.id.trusted_text_layout);
        trustedNetworkText = view.findViewById(R.id.trusted_network_text);
        wifiNameText = view.findViewById(R.id.wifi_name_text);
        timeLeftText = view.findViewById(R.id.time_left_text);
        toggleViews(false, false);
    }

    private void initializeSharedPreference() {
        preferenceManager = new PreferenceManager(mContext, _PREFS_NAME);
        isTrustedWifi = !(String.join(",", SettingsSingleton.getInstance().getSettings().getSelectedTrustedWifiNamesInListString()).isEmpty());
    }

    @SuppressLint("SetTextI18n")
    private void initViewModel(AppCompatActivity mContext) {
        homeViewModel = new ViewModelProvider(mContext).get(HomeViewModel.class);
        getUserData();
//        homeViewModel.getUserList(mContext, preferenceManager.getValue(_ACCESS_TOKEN, ""), basicInformation.getTenantName(), basicInformation.getUsername());
        homeViewModel.getConnectionMutableLiveData().observe(mContext, connectionModel -> {
            if (connectionModel != null) {
                if (!connectionModel.getUserTenants().getItems().isEmpty()) {
                    List<Item> items = connectionModel.getUserTenants().getItems();
                    currentItem = getSelectedTunnel(items);
                    initConnection(currentItem, connectionModel.getConfigModel());
                }
            }
        });
        homeViewModel.getErrorUserListMutableList().observe(mContext, s -> {
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        });
        homeViewModel.getTimeLeftMutableLiveData().observe(mContext, s -> {
            long givenMillis = TimeUnit.SECONDS.toMillis(Long.parseLong(s));
            Calendar currentCal = Calendar.getInstance();
            Calendar givenCal = Calendar.getInstance(Locale.ENGLISH);
            givenCal.setTimeInMillis(givenMillis);
            long remainingMillis = givenMillis - currentCal.getTimeInMillis();
            long days = TimeUnit.MILLISECONDS.toDays(remainingMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60;
            String formattedTime = String.format(Locale.ENGLISH, "%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
            Log.e("Remaining time: ", formattedTime);
            timeLeftText.setText("Time left: " + formattedTime);
        });
        homeViewModel.getErrorTimeLeftMutableList().observe(mContext, s -> {
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        });
    }

    private Item getSelectedTunnel(List<Item> items) {
        Item item = null;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getTunnelIp().equals(SettingsSingleton.getInstance().getSettings().getTunnels().getSelectedTunnels())) {
                item = items.get(i);
                break;
            }
        }
        if (item == null) {
            item = items.get(0);
        }
        return item;
    }

    private void getUserData() {
        String json = preferenceManager.getValue(_USER_INFORMATION, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            basicInformation = gson.fromJson(json, BasicInformation.class);
        }
    }

    private void initConnection(Item item, ConfigModel configModel) {
        try {
            backend.getRunningTunnelNames();
        } catch (NullPointerException e) {
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
                        toggleViews(false, isTrustedWifi);
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
                        Statistics stats = backend.getStatistics(tunnel);
                        Log.e("stats", stats.toString());
                        toggleViews(true, isTrustedWifi);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void toggleViews(boolean isConnected, boolean isTrustedWifi) {
        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                Log.e("netWorkStatus", "" + isConnected + " " + isTrustedWifi);
                if (isConnected) {
                    WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo;

                    wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                        wifiNameText.setText(wifiInfo.getSSID());
                    }
                    if (isTrustedWifi) {
                        connectedButton.setVisibility(View.GONE);
                        disconnectButton.setVisibility(View.VISIBLE);
                        connectedLayout.setVisibility(View.VISIBLE);
                        disconnectedLayout.setVisibility(View.GONE);
                        connectedCloudImage.setVisibility(View.VISIBLE);
                        disconnectedCloudImage.setVisibility(View.GONE);
                        notTrustedCloudImage.setVisibility(View.GONE);
                        networkStatus.setText("CONNECTED");
                        networkStatus.setTextColor(Color.parseColor("#12B204"));
                        trustedNetworkText.setText("Trusted Network");
                    } else {
                        connectedButton.setVisibility(View.GONE);
                        disconnectButton.setVisibility(View.VISIBLE);
                        connectedLayout.setVisibility(View.VISIBLE);
                        disconnectedLayout.setVisibility(View.GONE);
                        connectedCloudImage.setVisibility(View.GONE);
                        disconnectedCloudImage.setVisibility(View.GONE);
                        notTrustedCloudImage.setVisibility(View.VISIBLE);
                        networkStatus.setText("CONNECTED");
                        networkStatus.setTextColor(Color.parseColor("#12B204"));
                        trustedNetworkText.setText("Untrusted Network");
                    }
                    trustedTextLayout.setVisibility(View.VISIBLE);
                    wifiStatusLayout.setVisibility(View.VISIBLE);
                } else {
                    connectedButton.setVisibility(View.VISIBLE);
                    disconnectButton.setVisibility(View.GONE);
                    connectedLayout.setVisibility(View.GONE);
                    disconnectedLayout.setVisibility(View.VISIBLE);
                    connectedCloudImage.setVisibility(View.GONE);
                    disconnectedCloudImage.setVisibility(View.VISIBLE);
                    notTrustedCloudImage.setVisibility(View.GONE);
                    networkStatus.setText("DISCONNECTED");
                    networkStatus.setTextColor(Color.parseColor("#E82424"));
                    wifiStatusLayout.setVisibility(View.GONE);
                    trustedTextLayout.setVisibility(View.GONE);
                }
            }
        });
    }
}
