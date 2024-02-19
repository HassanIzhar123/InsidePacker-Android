package com.wireguard.insidepacket_android.fragments;

import static android.content.Context.RECEIVER_EXPORTED;
import static androidx.core.content.ContextCompat.registerReceiver;
import static com.wireguard.android.backend.Tunnel.State.DOWN;
import static com.wireguard.android.backend.Tunnel.State.UP;
import static com.wireguard.insidepacket_android.utils.AppStrings.CHANNEL_ID;
import static com.wireguard.insidepacket_android.utils.AppStrings._ACCESS_TOKEN;
import static com.wireguard.insidepacket_android.utils.AppStrings._PREFS_NAME;
import static com.wireguard.insidepacket_android.utils.AppStrings._USER_INFORMATION;
import static com.wireguard.insidepacket_android.utils.Utils.bytesToKB;
import static com.wireguard.insidepacket_android.utils.Utils.formatDecimal;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.wireguard.android.backend.Backend;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.Config;
import com.wireguard.config.InetEndpoint;
import com.wireguard.config.InetNetwork;
import com.wireguard.config.Interface;
import com.wireguard.config.Peer;
import com.wireguard.insidepacket_android.Interfaces.WifiStateChangeListener;
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
import com.wireguard.insidepacket_android.services.MyVpnService;
import com.wireguard.insidepacket_android.services.WifiReceiver;
import com.wireguard.insidepacket_android.utils.DebouncedOnClickListener;
import com.wireguard.insidepacket_android.utils.PreferenceManager;
import com.wireguard.insidepacket_android.utils.Utils;

import org.json.JSONObject;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import needle.Needle;

public class HomeFragment extends Fragment {
    private static final int NOTIFICATION_ID = 00010;
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
    TextView wifiNameText, trustedNetworkText, timeLeftText, trafficStatus, tunnelIpStatus, publicIpStatus, gatewayStatus;
    Item currentItem;
    Dialog progressDialog;
    final Handler handler = new Handler();
    final int delay = 1000;
    String ssid;
    MyVpnService vpnService;
    private WifiReceiver wifiReceiver;

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
        connectAlwaysOnVpn();
        onClickListeners();
        return view;
    }

    private void connectAlwaysOnVpn() {
        wifiReceiver = new WifiReceiver(new WifiStateChangeListener() {
            @Override
            public void onWifiStateChanged(boolean isConnected) {
                Toast.makeText(mContext, "isConnected: " + isConnected, Toast.LENGTH_SHORT).show();
                toggleViews(isConnected, checkIfAnyWifiIsTrusted());
            }
        });
        IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(mContext, wifiReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
        } else {
            ContextCompat.registerReceiver(mContext, wifiReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
        }
//        vpnService = new MyVpnService();
//        Intent vpnIntent = new Intent(getActivity(), MyVpnService.class);
//        vpnIntent.putExtra("activityContextClass", HomeFragment.class.getName());
//        vpnIntent.putExtra("wantToDisconnect", false);
////        mContext.startForegroundService(vpnIntent);
//        mContext.startService(vpnIntent);
//
//        LocalBroadcastManager.getInstance(mContext).registerReceiver(toggleBroadcastReceiver,
//                new IntentFilter(MyVpnService.CallBack));
    }

    final private BroadcastReceiver toggleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            String traffic = intent.getStringExtra("trafficData");
            String tunnelIp = intent.getStringExtra("tunnelIp");
            Log.e("trafficVdeivuiew", "" + traffic + " " + tunnelIp);
            if (traffic != null) {
                trafficStatus.setText(traffic);
            }
            if (tunnelIp != null) {
                tunnelIpStatus.setText(tunnelIp);
            }
            toggleViews(intent.getBooleanExtra("isConnected", false), intent.getBooleanExtra("isTrusted", false));
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(toggleBroadcastReceiver);
    }
//    private void connectAlwaysOnVpn() {
//        wifiReceiver = new WifiReceiver();
//        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        mContext.registerReceiver(wifiReceiver, intentFilter, RECEIVER_EXPORTED);
////        } else {
////            mContext.registerReceiver(wifiReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
////        }
//        startVpnService();
//    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (getContext() != null) {
//            getContext().unregisterReceiver(wifiReceiver);
//        }
//    }
//    private void startVpnService() {
//        if (getContext() != null) {
//            Intent vpnIntent = new Intent(getActivity(), MyVpnService.class);
//            getContext().startService(vpnIntent);
//        }
//    }

    private void onClickListeners() {
        connectedButton.setOnClickListener(new DebouncedOnClickListener(100) {
            @Override
            public void onDebouncedClick(View v) {
                progressDialog = new Utils().showProgressDialog(mContext);
                progressDialog.show();
                connectAlwaysOnVpn();
//                homeViewModel.getUserList(mContext, preferenceManager.getValue(_ACCESS_TOKEN, ""), basicInformation.getTenantName(), basicInformation.getUsername());
            }
        });

        disconnectButton.setOnClickListener(new DebouncedOnClickListener(100) {
            @Override
            public void onDebouncedClick(View v) {
                toggleViews(false, false);
                Intent intent = new Intent(getActivity(), MyVpnService.class);
                intent.putExtra("wantToDisconnect", true);
                mContext.startService(intent);
//                try {
//                    backend.getRunningTunnelNames();
//                } catch (NullPointerException e) {
//                    PersistentConnectionProperties.getInstance().setBackend(new GoBackend(mContext));
//                    backend = PersistentConnectionProperties.getInstance().getBackend();
//                }
//                AsyncTask.execute(() -> {
//                    try {
//                        backend = PersistentConnectionProperties.getInstance().getBackend();
//                        backend.setState(PersistentConnectionProperties.getInstance().getTunnel(), DOWN, null);
//                        SettingsSingleton.getInstance().setTunnelConnected(false);
//                        toggleViews(false, checkIfAnyWifiIsTrusted());
//                        handler.removeCallbacksAndMessages(null);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
            }
        });
        accessResourcesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentItem != null) {
                    progressDialog = new Utils().showProgressDialog(mContext);
                    progressDialog.show();
                    homeViewModel.accessOrganization(mContext, preferenceManager.getValue(_ACCESS_TOKEN, ""), currentItem.getTenantName(), currentItem.getTunnelId(), basicInformation.getUsername());
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
        tunnelIpStatus = view.findViewById(R.id.tunnel_ip_status);
        publicIpStatus = view.findViewById(R.id.public_ip_status);
        gatewayStatus = view.findViewById(R.id.gateway_status);
        if (backend != null) {
            toggleViews((!(backend.getRunningTunnelNames().isEmpty())), checkIfAnyWifiIsTrusted());
            getTunnelInformation();
        } else {
            toggleViews(false, checkIfAnyWifiIsTrusted());
        }
    }

    private void getTunnelInformation() {
        PreferenceManager stringPreferenceManager = new PreferenceManager(mContext, _PREFS_NAME);
        gatewayStatus.setText(stringPreferenceManager.getValue("Gateway", "-"));
        publicIpStatus.setText(stringPreferenceManager.getValue("public_ip", "-"));
        tunnelIpStatus.setText(stringPreferenceManager.getValue("tunnel_ip", "-"));
    }

    private void saveTunnelInformation() {
        PreferenceManager stringPreferenceManager = new PreferenceManager(mContext, _PREFS_NAME);
        stringPreferenceManager.saveValue("Gateway", gatewayStatus.getText());
        stringPreferenceManager.saveValue("public_ip", publicIpStatus.getText());
        stringPreferenceManager.saveValue("tunnel_ip", tunnelIpStatus.getText());
    }

    private void removeTunnelInformation() {
        PreferenceManager stringPreferenceManager = new PreferenceManager(mContext, _PREFS_NAME);
        stringPreferenceManager.remove("Gateway");
        stringPreferenceManager.remove("public_ip");
        stringPreferenceManager.remove("tunnel_ip");
    }

    private void initializeSharedPreference() {
        preferenceManager = new PreferenceManager(mContext, _PREFS_NAME);
//        isTrustedWifi = checkIfAnyWifiIsTrusted();
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

    private void startForegroundNotification(String notificationText) {
        Notification notification = new Utils().makeForegroundNotification(getActivity(), notificationText);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
//            startForeground(NOTIFICATION_ID, notification);
//        } else {
//            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
//        }
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
                    Intent ii = new Intent(mContext.getApplicationContext(), SplashActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, ii, PendingIntent.FLAG_IMMUTABLE);
                    new Utils().showPermanentNotification(mContext, CHANNEL_ID, "Vpn is Running!", NOTIFICATION_ID, pendingIntent);
//                    startForegroundNotification("Vpn running");
                    connectVpn(currentItem, connectionModel.getConfigModel());
                    progressDialog.dismiss();
                }
            }
        });
        homeViewModel.getErrorUserListMutableList().observe(mContext, s -> {
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        });
        homeViewModel.getAccessOrganizationLiveData().observe(mContext, s -> {
            handleStartTimer(s);
            progressDialog.dismiss();

        });
        homeViewModel.getTimeLeftErrorLiveData().observe(mContext, s -> {
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        });
        homeViewModel.getDataTransferMutableLiveData().observe(mContext, object -> {
            if (object != null) {
                if (object instanceof Boolean) {
                    boolean isTrustedMarked = (Boolean) object;
                    if (backend != null) {
                        if (!(backend.getRunningTunnelNames().isEmpty())) {
                            if (isTrustedMarked) {
                                toggleViews((!(backend.getRunningTunnelNames().isEmpty())), true);
                                handleCancelTimer();
                                timeLeftText.setVisibility(View.GONE);
                            } else {
                                if (backend != null) {
                                    AsyncTask.execute(() -> {
                                        try {
                                            backend.setState(PersistentConnectionProperties.getInstance().getTunnel(), DOWN, null);
                                            toggleViews((!(backend.getRunningTunnelNames().isEmpty())), isTrustedWifi);
                                            handler.removeCallbacksAndMessages(null);
                                            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                                            notificationManager.cancel(NOTIFICATION_ID);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }
                            }
                            saveTunnelInformation();
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
            PersistentConnectionProperties.getInstance().setBackend(new GoBackend(mContext));
            backend = PersistentConnectionProperties.getInstance().getBackend();
            toggleViews(!(backend.getRunningTunnelNames().isEmpty()), checkIfAnyWifiIsTrusted());
        } catch (NullPointerException e) {
            Log.e("tunnetuneel", "hi i ma here");
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
                        toggleViews(false, checkIfAnyWifiIsTrusted());
                        handler.removeCallbacksAndMessages(null);
                    } else {
//                        handler.removeCallbacksAndMessages(null);
                        List<InetNetwork> allowedIp;
                        if (checkIfAnyWifiIsTrusted()) {
                            allowedIp = new Utils().parseAllowedIPs(configModel.getAllowedIps());
                        } else {
                            allowedIp = new Utils().parseAllowedIPs(configModel.getUntrustedAllowedIps());
                        }
                        Config.Builder builder = new Config.Builder();
                        backend.setState(tunnel, UP, builder.setInterface(interfaceBuilder.addAddress(InetNetwork.parse(item.getTunnelIp())).parsePrivateKey(configModel.getTunnelPrivateKey()).addDnsServer(InetAddress.getByName(configModel.getTunnelDNS())).build()).addPeer(peerBuilder.addAllowedIps(allowedIp).setEndpoint(InetEndpoint.parse(configModel.getRemoteIp() + ":" + configModel.getRemotePort()))
//                                                        .parsePublicKey(item.getPublicKey())
                                .parsePreSharedKey(configModel.getPsk()).parsePublicKey(configModel.getPublicKey()).build()).build());
                        publicIpStatus.setText(configModel.getRemoteIp());
                        SettingsSingleton.getInstance().setTunnelConnected(true);
                        toggleViews(true, checkIfAnyWifiIsTrusted());

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        handler.postDelayed(new Runnable() {
            public void run() {
                try {
                    String txInMb = formatDecimal(bytesToKB(backend.getStatistics(tunnel).totalRx()));
                    String rxInMb = formatDecimal(bytesToKB(backend.getStatistics(tunnel).totalTx()));
                    String traffic = txInMb + "/" + rxInMb;
                    trafficStatus.setText(traffic);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
        tunnelIpStatus.setText(item.getTunnelIp());
    }

    @SuppressLint("SetTextI18n")
    public void toggleViews(boolean isConnected, boolean isTrustedWifi) {
        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo;

                    wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                        ssid = wifiInfo.getSSID();
                        wifiNameText.setText(ssid);
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

    //    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext.registerReceiver(receiver, new IntentFilter(BroadcastService.COUNTDOWN_BR), RECEIVER_EXPORTED);
        } else {
            mContext.registerReceiver(receiver, new IntentFilter(BroadcastService.COUNTDOWN_BR), Context.RECEIVER_NOT_EXPORTED);
        }
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
            Date date = new Date(millisUntilFinished);
            SimpleDateFormat sdf;
            if (millisUntilFinished >= 3600000) {
                sdf = new SimpleDateFormat("HH:mm:ss");
            } else {
                sdf = new SimpleDateFormat("mm:ss");
            }
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Set the time zone if needed
            timeLeftText.setText(sdf.format(date));
            boolean countdownTimerFinished = intent.getBooleanExtra("countdownTimerFinished", true);
            if (countdownTimerFinished) {
                timeLeftText.setVisibility(View.GONE);
                toggleViews(true, checkIfAnyWifiIsTrusted());
            } else {
                timeLeftText.setVisibility(View.VISIBLE);
                toggleViews(true, true);
            }
        }
    }
}
