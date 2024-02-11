package com.wireguard.insidepacket_android.fragments;

import static com.wireguard.android.backend.Tunnel.State.DOWN;
import static com.wireguard.android.backend.Tunnel.State.UP;
import static com.wireguard.insidepacket_android.utils.SharedPrefsName._ACCESS_TOKEN;
import static com.wireguard.insidepacket_android.utils.SharedPrefsName._PREFS_NAME;
import static com.wireguard.insidepacket_android.utils.SharedPrefsName._USER_INFORMATION;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.text.format.Formatter;
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

import androidx.annotation.RequiresApi;
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
import com.wireguard.config.ParseException;
import com.wireguard.config.Peer;
import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.ViewModels.HomeViewModel.HomeViewModel;
import com.wireguard.insidepacket_android.activities.SplashActivity;
import com.wireguard.insidepacket_android.essentials.PersistentConnectionProperties;
import com.wireguard.insidepacket_android.essentials.SettingsSingleton;
import com.wireguard.insidepacket_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacket_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacket_android.models.UserTenants.Item;
import com.wireguard.insidepacket_android.models.settings.TrustedWifi;
import com.wireguard.insidepacket_android.services.BroadcastService;
import com.wireguard.insidepacket_android.utils.PreferenceManager;
import com.wireguard.insidepacket_android.utils.Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
    TextView wifiNameText, trustedNetworkText, timeLeftText, trafficStatus;
    Item currentItem;
    Dialog progressDialog;
    final Handler handler = new Handler();
    final int delay = 1000;

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
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);
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
                    SettingsSingleton.getInstance().setTunnelConnected(false);
                    toggleViews(false, isTrustedWifi);
                    handler.removeCallbacksAndMessages(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        accessResourcesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentItem != null) {
                    progressDialog = new Utils().showProgressDialog(mContext);
                    progressDialog.show();
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
        trafficStatus = view.findViewById(R.id.traffic_status);
        toggleViews(false, isTrustedWifi);
    }

    private void initializeSharedPreference() {
        preferenceManager = new PreferenceManager(mContext, _PREFS_NAME);
        isTrustedWifi = checkIfAnyWifiIsTrusted();
    }

    private boolean checkIfAnyWifiIsTrusted() {
        List<TrustedWifi> trustedWifi = SettingsSingleton.getInstance().getSettings().getTrustedWifi();
        for (int i = 0; i < trustedWifi.size(); i++) {
            if (trustedWifi.get(i).getSelected()) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    private void initViewModel(AppCompatActivity mContext) {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        getUserData();
//        homeViewModel.getUserList(mContext, preferenceManager.getValue(_ACCESS_TOKEN, ""), basicInformation.getTenantName(), basicInformation.getUsername());
        homeViewModel.getConnectionMutableLiveData().observe(mContext, connectionModel -> {
            if (connectionModel != null) {
                if (!connectionModel.getUserTenants().getItems().isEmpty()) {
                    List<Item> items = connectionModel.getUserTenants().getItems();
                    currentItem = getSelectedTunnel(items);
                    Intent ii = new Intent(mContext.getApplicationContext(), SplashActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, ii, PendingIntent.FLAG_IMMUTABLE);
                    new Utils().showPermanentNotification(mContext, "notify_001", "Vpn is Running!", 0, pendingIntent);
                    connectVpn(currentItem, connectionModel.getConfigModel());
                }
            }
        });
        homeViewModel.getErrorUserListMutableList().observe(mContext, s -> {
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        });
        homeViewModel.getTimeLeftMutableLiveData().observe(mContext, s -> {
            //has to set trusted here and untrusted when disconnected
            handleStartTimer(s);
            progressDialog.dismiss();

        });
        homeViewModel.getErrorTimeLeftMutableList().observe(mContext, s -> {
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        });
        homeViewModel.getDataTransferMutableLiveData().observe(mContext, object -> {
            if (object != null) {
                if (object instanceof Boolean) {
                    boolean isTrustedMarked = (Boolean) object;
                    if (isTrustedMarked) {
                        toggleViews(true, true);
                    } else {
                        if (backend != null) {
                            AsyncTask.execute(() -> {
                                try {
                                    backend.setState(PersistentConnectionProperties.getInstance().getTunnel(), DOWN, null);
                                    toggleViews(false, isTrustedWifi);
                                    handler.removeCallbacksAndMessages(null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                }
            }
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

    private void connectVpn(Item item, ConfigModel configModel) {
        try {
            backend.getRunningTunnelNames();
//            backend.getStatistics(PersistentConnectionProperties.getInstance().getTunnel());
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
                        handler.removeCallbacksAndMessages(null);
                    } else {
                        List<InetNetwork> allowedIp;
//                        if (isTrustedWifi) {
//                        allowedIp = parseAllowedIPs(configModel.getAllowedIps());
//                        } else {
//                            allowedIp =parseAllowedIPs(configModel.getUntrustedAllowedIps());
//                        }
                        item.setTunnelIp("10.9.6.27");
                        configModel.setTunnelPrivateKey("eBeqf7IU9tIZNiecdlcXng5qwA5Dhri0zMWW0gue21Y=");
                        allowedIp = parseAllowedIPs("0.0.0.0/0");
                        configModel.setRemoteIp("139.178.81.253");
                        configModel.setRemotePort("52078");
                        configModel.setPublicKey("SYHr+NMIPZt3EWgabfsbw2fbw7rKUVONxMpJG5s/DA8=");
                        Log.e("allowedIp", String.valueOf(allowedIp));
                        Config.Builder builder = new Config.Builder();
                        backend.setState(
                                tunnel,
                                UP,
                                builder
                                        .setInterface(
                                                interfaceBuilder
                                                        .addAddress(InetNetwork.parse(item.getTunnelIp()))
                                                        .parsePrivateKey(configModel.getTunnelPrivateKey())
                                                        .addDnsServer(InetAddress.getByName("1.1.1.1"))
                                                        .build()
                                        )
                                        .addPeer(
                                                peerBuilder
                                                        .addAllowedIps(allowedIp)
                                                        .setEndpoint(InetEndpoint.parse(configModel.getRemoteIp() + ":" + configModel.getRemotePort()))
//                                                        .parsePublicKey(item.getPublicKey())
                                                        .parsePreSharedKey(configModel.getPsk())
                                                        .parsePublicKey(configModel.getPublicKey())
                                                        .build()
                                        )
                                        .build()
                        );
                        Log.e("WireGaurdNewPackage", "Tunnel IP: " + item.getTunnelIp());
                        Log.e("WireGaurdNewPackage", "Tunnel Private Key: " + configModel.getTunnelPrivateKey());
                        Log.e("WireGaurdNewPackage", "Remote IP: " + configModel.getRemoteIp());
                        Log.e("WireGaurdNewPackage", "Remote Port: " + configModel.getRemotePort());
                        Log.e("WireGaurdNewPackage", "Public Key: " + item.getPublicKey() + " " + configModel.getPublicKey());
                        Log.e("WireGaurdNewPackage", "Allowed IPs: " + allowedIp);
                        SettingsSingleton.getInstance().setTunnelConnected(true);
                        toggleViews(true, isTrustedWifi);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        handler.postDelayed(new Runnable() {
            public void run() {
                String s_dns1;
                String s_dns2;
                String s_gateway;
                String s_ipAddress;
                String s_leaseDuration;
                String s_netmask;
                String s_serverAddress;
                TextView info;
                DhcpInfo d;
                WifiManager wifii = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                try {
                    trafficStatus.setText("RX: " + backend.getStatistics(PersistentConnectionProperties.getInstance().getTunnel()).totalRx() + " TX: " + backend.getStatistics(tunnel).totalTx());
                    d = wifii.getDhcpInfo();

                    s_dns1 = "DNS 1: " + d.dns1;
                    s_dns2 = "DNS 2: " + d.dns2;
                    s_gateway = "Default Gateway: " + intToInetAddress(d.gateway).getHostAddress();
                    s_ipAddress = "IP Address: " + Formatter.formatIpAddress(d.ipAddress);
                    s_leaseDuration = "Lease Time: " + d.leaseDuration;
                    s_netmask = "Subnet Mask: " + d.netmask;
                    s_serverAddress = "Server IP: " + Formatter.formatIpAddress(d.serverAddress);

                    Log.e("data", "Network Info\n" + s_dns1 + "\n" + s_dns2 + "\n" + s_gateway + "\n" + s_ipAddress + "\n" + s_leaseDuration + "\n" + s_netmask + "\n" + s_serverAddress);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }
    /**
     * Convert a IPv4 address from an integer to an InetAddress.
     * @param hostAddress an int corresponding to the IPv4 address in network byte order
     */
    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }
    private List<InetNetwork> parseAllowedIPs(String allowedIPs) throws ParseException {
        List<InetNetwork> allowedIPRanges = new ArrayList<>();
        String[] ipRanges = allowedIPs.split(",");
        for (String ipRange : ipRanges) {
            allowedIPRanges.add(InetNetwork.parse(ipRange.trim()));
        }
        return allowedIPRanges;
    }

    @SuppressLint("SetTextI18n")
    public void toggleViews(boolean isConnected, boolean isTrustedWifi) {
        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                Log.e("netWorkStatus", isConnected + " " + isTrustedWifi);
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

    public void handleStartTimer(String milliseconds) {
        Intent intent = new Intent(mContext, BroadcastService.class);
        intent.putExtra("maxCountDownValue", milliseconds);
        mContext.startForegroundService(intent);
    }

    public void handleCancelTimer() {
        Intent intent = new Intent(mContext, BroadcastService.class);
        mContext.stopService(intent);
    }

    /* CountDown */
    final private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGUI(intent);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onResume() {
        super.onResume();
        mContext.registerReceiver(receiver, new IntentFilter(BroadcastService.COUNTDOWN_BR), Context.RECEIVER_EXPORTED);
    }

    @Override
    public void onPause() {
        super.onPause();
        mContext.unregisterReceiver(receiver);
    }

    @Override
    public void onStop() {
        try {
            mContext.unregisterReceiver(receiver);
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
        }
        super.onStop();
    }

    private void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {
            long millisUntilFinished = intent.getLongExtra("countdown", 0);
            long seconds = (millisUntilFinished / 1000) % 60;
            long minutes = (millisUntilFinished / (1000 * 60)) % 60;
            long hours = (millisUntilFinished / (1000 * 60 * 60)) % 60;
            String time = (hours + " : " + minutes + " : " + seconds);
            timeLeftText.setText(time);
            boolean countdownTimerFinished = intent.getBooleanExtra("countdownTimerFinished", true);
            if (countdownTimerFinished) {
                timeLeftText.setVisibility(View.GONE);
                toggleViews(true, false);
            } else {
                timeLeftText.setVisibility(View.VISIBLE);
                toggleViews(true, true);
            }
        }
    }
}
