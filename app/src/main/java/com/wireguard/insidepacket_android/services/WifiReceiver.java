package com.wireguard.insidepacket_android.services;

import static androidx.core.app.ActivityCompat.startActivityForResult;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.wireguard.android.backend.Tunnel.State.DOWN;
import static com.wireguard.android.backend.Tunnel.State.UP;
import static com.wireguard.insidepacket_android.utils.AppStrings.CHANNEL_ID;
import static com.wireguard.insidepacket_android.utils.AppStrings._ACCESS_TOKEN;
import static com.wireguard.insidepacket_android.utils.AppStrings._PREFS_NAME;
import static com.wireguard.insidepacket_android.utils.AppStrings._USER_INFORMATION;
import static com.wireguard.insidepacket_android.utils.Utils.bytesToKB;
import static com.wireguard.insidepacket_android.utils.Utils.formatDecimal;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.os.BuildCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.karumi.dexter.BuildConfig;
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
import com.wireguard.insidepacket_android.Interfaces.WifiStateChangeListener;
import com.wireguard.insidepacket_android.activities.SplashActivity;
import com.wireguard.insidepacket_android.essentials.PersistentConnectionProperties;
import com.wireguard.insidepacket_android.essentials.SettingsSingleton;
import com.wireguard.insidepacket_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacket_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacket_android.models.ConnectionModel.ConnectionModel;
import com.wireguard.insidepacket_android.models.UserTenants.Item;
import com.wireguard.insidepacket_android.models.UserTenants.UserTenants;
import com.wireguard.insidepacket_android.models.settings.TrustedWifi;
import com.wireguard.insidepacket_android.services.MyVpnService;
import com.wireguard.insidepacket_android.utils.PreferenceManager;
import com.wireguard.insidepacket_android.utils.Utils;

import org.json.JSONObject;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

public class WifiReceiver extends BroadcastReceiver {

    //    private final MyVpnService vpnService;
    private static final int NOTIFICATION_ID = 101;
    private String previousSSID = null;
    private boolean isFirstBroadcast = true;
    Backend backend = PersistentConnectionProperties.getInstance().getBackend();
    Item currentItem;
    BasicInformation basicInformation;
    PreferenceManager preferenceManager;
    Tunnel tunnel;
    final Handler handler = new Handler();
    final int delay = 1000;
    private WifiStateChangeListener callback;
    Context mContext;

    public WifiReceiver(WifiStateChangeListener callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        try {
            String action = intent.getAction();
            if (action != null && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();
                    Log.e("wifiChanging", ssid + " Wifi is Changed Connecting to VPN");
                    startVpn(context);
                }
            }
        } catch (Exception e) {
            Log.e("Exception", "" + e.toString());
            cancelAllNotifications();
        }
//        if (intent != null) {
//            if (Objects.equals(intent.getAction(), WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
//                if (isFirstBroadcast) {
//                    isFirstBroadcast = false;
//                    return;
//                }
//                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//                if (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
//                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//                    String currentSSID = wifiInfo.getSSID();
//                    if (previousSSID == null || !previousSSID.equals(currentSSID)) {
//                        Log.e("wifiChanging", "Wifi is CHanged Connecting to VPN");
//                        startVpn(context);
//                        previousSSID = currentSSID;
//                    }
//                }
//            }
//        }
    }

    private void cancelAllNotifications() {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private boolean isConnectedToInternet(ConnectivityManager connManager) {
        Network network = connManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    void startVpn(Context context) {
        getUserData(context);
        String tenantName = basicInformation.getTenantName();
        String username = basicInformation.getUsername();
        String accessToken = preferenceManager.getValue(_ACCESS_TOKEN, "");
        ApiClient apiClient = ApiClient.getInstance(context);
        String userListUrl = StaticData.getUserListUrl(tenantName, username);
        Log.e("userListUrl123", "" + userListUrl);
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
                Intent ii = new Intent(context, SplashActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, ii, PendingIntent.FLAG_IMMUTABLE);
                new Utils().showPermanentNotification(context, CHANNEL_ID, "Vpn is Running!", NOTIFICATION_ID, pendingIntent);
                connectVpn(context, currentItem, connectionModel.getConfigModel());
            }

            @Override
            public void onError(String message) {
//                Toast.makeText(context, "Diagnostics failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserData(Context context) {
        preferenceManager = new PreferenceManager(context, _PREFS_NAME);
        String json = preferenceManager.getValue(_USER_INFORMATION, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            basicInformation = gson.fromJson(json, BasicInformation.class);
        }
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

    private void connectVpn(Context context, Item item, ConfigModel configModel) {
        try {
            PersistentConnectionProperties.getInstance().setBackend(new GoBackend(context));
            backend = PersistentConnectionProperties.getInstance().getBackend();
            backend.getRunningTunnelNames();
//            intent.putExtra("isConnected", !(backend.getRunningTunnelNames().isEmpty()));
//            intent.putExtra("isTrusted", checkIfAnyWifiIsTrusted());
//            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } catch (NullPointerException e) {
            PersistentConnectionProperties.getInstance().setBackend(new GoBackend(context));
            backend = PersistentConnectionProperties.getInstance().getBackend();
        }
        connect(context, item, configModel);
        handler.postDelayed(new Runnable() {
            public void run() {
                try {
                    if (tunnel != null) {
                        if (backend.getState(PersistentConnectionProperties.getInstance().getTunnel()) == UP) {
                            String txInMb = formatDecimal(bytesToKB(backend.getStatistics(tunnel).totalRx()));
                            String rxInMb = formatDecimal(bytesToKB(backend.getStatistics(tunnel).totalTx()));
                            String traffic = txInMb + "/" + rxInMb;
                            Log.e("trafficData", "" + traffic);
                            callback.onTrafficSent(traffic);
//                            intent.putExtra("trafficData", traffic);
//                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Exceptuionemake", "" + e.toString());
                    throw new RuntimeException(e);
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void connect(Context mContext, Item item, ConfigModel configModel) {
        tunnel = PersistentConnectionProperties.getInstance().getTunnel();
        Intent intentPrepare = GoBackend.VpnService.prepare(mContext);
        if (intentPrepare != null) {
//            startForegroundService(intentPrepare);
//            ContextCompat.startForegroundService(mContext, intentPrepare);
//            Context context = this;
            startActivityForResult((Activity) mContext, intentPrepare, 0, null);
        }
        Interface.Builder interfaceBuilder = new Interface.Builder();
        Peer.Builder peerBuilder = new Peer.Builder();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (backend.getState(PersistentConnectionProperties.getInstance().getTunnel()) == UP) {
                        backend.setState(tunnel, DOWN, null);
//                        intent.putExtra("isConnected", false);
//                        intent.putExtra("isTrusted", checkIfAnyWifiIsTrusted());
//                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//                        handler.removeCallbacksAndMessages(null);
                    } else {
//                        handler.removeCallbacksAndMessages(null);
                        List<InetNetwork> allowedIp;
                        Log.e("checkIfAnyWifiIsTrusted", "" + checkIfAnyWifiIsTrusted());
                        if (checkIfAnyWifiIsTrusted()) {
                            allowedIp = new Utils().parseAllowedIPs(configModel.getAllowedIps());
                        } else {
                            allowedIp = new Utils().parseAllowedIPs(configModel.getUntrustedAllowedIps());
                        }
                        Config.Builder builder = new Config.Builder();
                        SettingsSingleton.getInstance().setTunnelConnected(true);
                        Log.e("WireGaurdNewPackage", "Tunnel IP: " + item.getTunnelIp());
                        Log.e("WireGaurdNewPackage", "Tunnel Private Key: " + configModel.getTunnelPrivateKey());
                        Log.e("WireGaurdNewPackage", "Remote IP: " + configModel.getRemoteIp());
                        Log.e("WireGaurdNewPackage", "Remote Port: " + configModel.getRemotePort());
                        Log.e("WireGaurdNewPackage", "Public Key: " + item.getPublicKey() + " " + configModel.getPublicKey());
                        Log.e("WireGaurdNewPackage", "Allowed IPs: " + allowedIp);
//                        backend.setState(tunnel, UP, builder.setInterface(interfaceBuilder.addAddress(InetNetwork.parse(item.getTunnelIp())).parsePrivateKey(configModel.getTunnelPrivateKey()).addDnsServer(InetAddress.getByName(configModel.getTunnelDNS())).build()).addPeer(peerBuilder.addAllowedIps(allowedIp).setEndpoint(InetEndpoint.parse(configModel.getRemoteIp() + ":" + configModel.getRemotePort()))
////                              .parsePublicKey(item.getPublicKey())
//                                .parsePreSharedKey(configModel.getPsk()).parsePublicKey(configModel.getPublicKey()).build()).build());
                        Log.e("backendState", "" + (backend != null) + ", " + (tunnel != null));
                        backend.setState(
                                tunnel,
                                UP,
                                builder.setInterface(
                                                interfaceBuilder
                                                        .addAddress(InetNetwork.parse(item.getTunnelIp()))
                                                        .parsePrivateKey(configModel.getTunnelPrivateKey())
                                                        .addDnsServer(InetAddress.getByName(configModel.getTunnelDNS()))
                                                        .build()
                                        )
                                        .addPeer(
                                                peerBuilder
                                                        .addAllowedIps(allowedIp)
                                                        .setEndpoint(InetEndpoint.parse(configModel.getRemoteIp() + ":" + configModel.getRemotePort()))
                                                        .parsePreSharedKey(configModel.getPsk())
                                                        .parsePublicKey(configModel.getPublicKey())
                                                        .parsePersistentKeepalive("7")
                                                        .build()
                                        )
                                        .build()
                        );
                        callback.onWifiStateChanged(true);
//                        publicIpStatus.setText(configModel.getRemoteIp());
//                        intent.putExtra("isConnected", true);
//                        intent.putExtra("isTrusted", checkIfAnyWifiIsTrusted());
//                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        handler.postDelayed(new Runnable() {
            public void run() {
                try {
                    if (tunnel != null) {
                        if (backend.getState(PersistentConnectionProperties.getInstance().getTunnel()) == UP) {
                            String txInMb = formatDecimal(bytesToKB(backend.getStatistics(tunnel).totalRx()));
                            String rxInMb = formatDecimal(bytesToKB(backend.getStatistics(tunnel).totalTx()));
                            String traffic = txInMb + "/" + rxInMb;
//                            intent.putExtra("trafficData", traffic);
//                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Exceptuionemake", "" + e.toString());
                    throw new RuntimeException(e);
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
//        intent.putExtra("tunnelIp", item.getTunnelIp());
//        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
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
}