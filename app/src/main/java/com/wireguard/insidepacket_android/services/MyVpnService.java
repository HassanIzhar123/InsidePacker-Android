package com.wireguard.insidepacket_android.services;

import static androidx.core.app.ActivityCompat.startActivityForResult;
import static com.wireguard.android.backend.Tunnel.State.DOWN;
import static com.wireguard.android.backend.Tunnel.State.UP;
import static com.wireguard.insidepacket_android.utils.AppStrings.CHANNEL_ID;
import static com.wireguard.insidepacket_android.utils.AppStrings._ACCESS_TOKEN;
import static com.wireguard.insidepacket_android.utils.AppStrings._PREFS_NAME;
import static com.wireguard.insidepacket_android.utils.AppStrings._USER_INFORMATION;
import static com.wireguard.insidepacket_android.utils.Utils.bytesToKB;
import static com.wireguard.insidepacket_android.utils.Utils.formatDecimal;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LifecycleService;
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
import com.wireguard.insidepacket_android.Api.ApiClient;
import com.wireguard.insidepacket_android.DataStructure.StaticData;
import com.wireguard.insidepacket_android.Interfaces.VolleyCallback;
import com.wireguard.insidepacket_android.ViewModels.HomeViewModel.HomeViewModel;
import com.wireguard.insidepacket_android.activities.SplashActivity;
import com.wireguard.insidepacket_android.essentials.PersistentConnectionProperties;
import com.wireguard.insidepacket_android.essentials.SettingsSingleton;
import com.wireguard.insidepacket_android.fragments.HomeFragment;
import com.wireguard.insidepacket_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacket_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacket_android.models.ConnectionModel.ConnectionModel;
import com.wireguard.insidepacket_android.models.UserTenants.Item;
import com.wireguard.insidepacket_android.models.UserTenants.UserTenants;
import com.wireguard.insidepacket_android.models.settings.SettingsModel;
import com.wireguard.insidepacket_android.models.settings.TrustedWifi;
import com.wireguard.insidepacket_android.utils.PreferenceManager;
import com.wireguard.insidepacket_android.utils.Utils;

import org.json.JSONObject;

import java.net.InetAddress;
import java.util.List;

public class MyVpnService extends VpnService {

    private static final int NOTIFICATION_ID = 101;
    public static String CallBack = "CallBack";
    private boolean vpnConnected = false;
    boolean isAnyWifiTrusted = false;
    Intent intent = new Intent(CallBack);
    WifiReceiver wifiReceiver;
    BasicInformation basicInformation;
    PreferenceManager preferenceManager;
    Item currentItem;
    Backend backend = PersistentConnectionProperties.getInstance().getBackend();
    final Handler handler = new Handler();
    final int delay = 1000;
    Tunnel tunnel;

    @Override
    public void onCreate() {
        super.onCreate();
        SettingsModel settingsModel = new Utils().getSettings(getApplicationContext());
        List<TrustedWifi> trustedWifiList = settingsModel.getTrustedWifi();
        isAnyWifiTrusted = false;
        for (int i = 0; i < trustedWifiList.size(); i++) {
            if (trustedWifiList.get(i).getSelected()) {
                isAnyWifiTrusted = true;
                break;
            }
        }
        if (!isAnyWifiTrusted) {
            startForeground("VPN is connected.");
        } else {
            if (settingsModel.getAlwaysOnVpn()) {
                startForeground("VPN is connected.");
            } else {
                startForeground("VPN is disconnected.");
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            boolean wantToDisconnect = intent.getBooleanExtra("wantToDisconnect", false); // Extract the data from the Intent
            Log.e("wantToDisconnect", "" + wantToDisconnect);
            if (wantToDisconnect) {
                try {
                    backend.getRunningTunnelNames();
                } catch (NullPointerException e) {
                    PersistentConnectionProperties.getInstance().setBackend(new GoBackend(getApplicationContext()));
                    backend = PersistentConnectionProperties.getInstance().getBackend();
                }
                AsyncTask.execute(() -> {
                    try {
                        backend = PersistentConnectionProperties.getInstance().getBackend();
                        backend.setState(PersistentConnectionProperties.getInstance().getTunnel(), DOWN, null);
                        SettingsSingleton.getInstance().setTunnelConnected(false);
                        handler.removeCallbacksAndMessages(null);
                        vpnConnected = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                wifiReceiver = new WifiReceiver(this);
                IntentFilter intentFilter = new IntentFilter("android.net.wifi.STATE_CHANGE");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(wifiReceiver, intentFilter, RECEIVER_EXPORTED);
                } else {
                    registerReceiver(wifiReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
                }
                startVpn();
            }
        } else {
            wifiReceiver = new WifiReceiver(this);
            IntentFilter intentFilter = new IntentFilter("android.net.wifi.STATE_CHANGE");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(wifiReceiver, intentFilter, RECEIVER_EXPORTED);
            } else {
                registerReceiver(wifiReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
            }
            startVpn();
        }
        return START_STICKY;
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

    private void connectVpn(Item item, ConfigModel configModel) {
        try {
            backend.getRunningTunnelNames();
            intent.putExtra("isConnected", !(backend.getRunningTunnelNames().isEmpty()));
            intent.putExtra("isTrusted", checkIfAnyWifiIsTrusted());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } catch (NullPointerException e) {
            PersistentConnectionProperties.getInstance().setBackend(new GoBackend(getApplicationContext()));
            backend = PersistentConnectionProperties.getInstance().getBackend();
        }
        connect(getApplicationContext(), item, configModel);
    }

    public void connect(Context mContext, Item item, ConfigModel configModel) {
        tunnel = PersistentConnectionProperties.getInstance().getTunnel();

        Intent intentPrepare = GoBackend.VpnService.prepare(mContext);
        if (intentPrepare != null) {
            startActivityForResult((Activity) getApplicationContext(), intentPrepare, 0, null);
        }
        Interface.Builder interfaceBuilder = new Interface.Builder();
        Peer.Builder peerBuilder = new Peer.Builder();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (backend.getState(PersistentConnectionProperties.getInstance().getTunnel()) == UP) {
                        backend.setState(tunnel, DOWN, null);
                        intent.putExtra("isConnected", false);
                        intent.putExtra("isTrusted", checkIfAnyWifiIsTrusted());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//                        handler.removeCallbacksAndMessages(null);
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
//                        publicIpStatus.setText(configModel.getRemoteIp());
                        SettingsSingleton.getInstance().setTunnelConnected(true);
                        Log.e("WireGaurdNewPackage", "Tunnel IP: " + item.getTunnelIp());
                        Log.e("WireGaurdNewPackage", "Tunnel Private Key: " + configModel.getTunnelPrivateKey());
                        Log.e("WireGaurdNewPackage", "Remote IP: " + configModel.getRemoteIp());
                        Log.e("WireGaurdNewPackage", "Remote Port: " + configModel.getRemotePort());
                        Log.e("WireGaurdNewPackage", "Public Key: " + item.getPublicKey() + " " + configModel.getPublicKey());
                        Log.e("WireGaurdNewPackage", "Allowed IPs: " + allowedIp);
                        intent.putExtra("isConnected", true);
                        intent.putExtra("isTrusted", checkIfAnyWifiIsTrusted());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
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
                    intent.putExtra("trafficData", traffic);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                } catch (Exception e) {
                    Log.e("Exceptuionemake", "" + e.toString());
                    throw new RuntimeException(e);
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
        intent.putExtra("tunnelIp", item.getTunnelIp());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
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

    private void getUserData() {
        preferenceManager = new PreferenceManager(this, _PREFS_NAME);
        String json = preferenceManager.getValue(_USER_INFORMATION, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            basicInformation = gson.fromJson(json, BasicInformation.class);
        }
    }

    void startVpn() {
        Log.e("wantToDisconnect1", "" + vpnConnected);
        if (vpnConnected) {
            return;
        }
        Log.e("wifiChanging", "Wifi is CHanged Connecting to VPN");
        getUserData();
        String tenantName = basicInformation.getTenantName();
        String username = basicInformation.getUsername();
        String accessToken = preferenceManager.getValue(_ACCESS_TOKEN, "");
        ApiClient apiClient = ApiClient.getInstance(this);
        String userListUrl = StaticData.getUserListUrl(tenantName, username);
        Log.e("userListUrl", "" + userListUrl);
        apiClient.homeGetRequest(userListUrl, tenantName, username, accessToken, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject tenantListResult) {

            }

            @Override
            public void onSuccess(JSONObject tenantListResult, JSONObject configResult) {
                Log.e("onSuccessview", "" + tenantListResult.toString() + " tunnelJson: " + configResult);
                UserTenants userTenants = new Gson().fromJson(tenantListResult.toString(), UserTenants.class);
                ConfigModel configModel = new Gson().fromJson(configResult.toString(), ConfigModel.class);
                ConnectionModel connectionModel = new ConnectionModel();
                connectionModel.setConfigModel(configModel);
                connectionModel.setUserTenants(userTenants);


                List<Item> items = connectionModel.getUserTenants().getItems();
                currentItem = getSelectedTunnel(items);
                Intent ii = new Intent(getApplicationContext(), SplashActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, ii, PendingIntent.FLAG_IMMUTABLE);
                new Utils().showPermanentNotification(getApplicationContext(), CHANNEL_ID, "Vpn is Running!", NOTIFICATION_ID, pendingIntent);
                connectVpn(currentItem, connectionModel.getConfigModel());
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MyVpnService.this, "Diagnostics failed!", Toast.LENGTH_SHORT).show();
            }
        });
        vpnConnected = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
        stopVpn();
    }

    private void stopVpn() {
        if (vpnConnected) {
            Log.e("wifiChanging", "VPN is disconnecting");
            // Code to stop VPN connection
            intent.putExtra("isConnected", false);
            intent.putExtra("isTrusted", false);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            vpnConnected = false;
        }
    }

    private void startForeground(String notificationText) {
        Notification notification = new Utils().makeForegroundNotification(getApplicationContext(), notificationText);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(NOTIFICATION_ID, notification);
        } else {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        }
    }

}