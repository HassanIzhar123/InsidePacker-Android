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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
import com.wireguard.insidepacket_android.Interfaces.WifiStateChangeListener;
import com.wireguard.insidepacket_android.activities.SplashActivity;
import com.wireguard.insidepacket_android.essentials.PersistentConnectionProperties;
import com.wireguard.insidepacket_android.essentials.SettingsSingleton;
import com.wireguard.insidepacket_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacket_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacket_android.models.ConnectedTunnelModel.ConnectedTunnelModel;
import com.wireguard.insidepacket_android.models.ConnectionModel.ConnectionModel;
import com.wireguard.insidepacket_android.models.Diagnostics;
import com.wireguard.insidepacket_android.models.UserTenants.Item;
import com.wireguard.insidepacket_android.models.UserTenants.UserTenants;
import com.wireguard.insidepacket_android.models.settings.TrustedWifi;
import com.wireguard.insidepacket_android.utils.PreferenceManager;
import com.wireguard.insidepacket_android.utils.Utils;
import com.wireguard.insidepacket_android.utils.WifiUtils;

import org.json.JSONObject;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class WifiReceiver extends BroadcastReceiver {

    //    private final MyVpnService vpnService;
    private static final int NOTIFICATION_ID = 101;
    private String previousSSID = null;
    private boolean isFirstBroadcast = true;
    Backend backend = PersistentConnectionProperties.getInstance().getBackend();
    Item currentItem;
    ConfigModel config;
    BasicInformation basicInformation;
    PreferenceManager preferenceManager;
    Tunnel tunnel;
    final int delay = 1000;
    private WifiStateChangeListener callback;
    Context mContext;
    Activity activity;

    public WifiReceiver() {
    }

    public WifiReceiver(Activity activity, WifiStateChangeListener callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public Tunnel getTunnel() {
        return tunnel;
    }

    public Backend getBackend() {
        return backend;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        try {
//            String action = intent.getAction();
//            if (action != null && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
//                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//                if (networkInfo != null && networkInfo.isConnected()) {
//                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//                    String ssid = wifiInfo.getSSID();
//                    Log.e("wifiChanging", ssid + " Wifi is Changed Connecting to VPN");
//                    startVpn(context);
//                }
//            }
            if (intent != null) {
                if (Objects.equals(intent.getAction(), WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    if (isFirstBroadcast) {
                        isFirstBroadcast = false;
                        return;
                    }
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String currentSSID = wifiInfo.getSSID();
                        if (previousSSID == null || !previousSSID.equals(currentSSID)) {
                            Log.e("wifiChanging", currentSSID + " Wifi is Changed Connecting to VPN");
                            startVpn(context, true);
                            previousSSID = currentSSID;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Exception", "" + e.toString());
            cancelAllNotifications();
        }
    }

    private void postDiagnosticApi(Context context, boolean isWifiChanged) {
        if (isWifiChanged) {
            ApiClient apiClient = ApiClient.getInstance(context);
            try {
                String ssid = WifiUtils.getSSID(context);
                Diagnostics diagnostics = new Diagnostics();
                diagnostics.setTimestamp(Calendar.getInstance().getTimeInMillis());
                diagnostics.setConnectionType(WifiUtils.getConnectionType(context));
                diagnostics.setBssid(WifiUtils.getBSSID(context));
                diagnostics.setRssi(String.valueOf(WifiUtils.getRSSI(context)));
                diagnostics.setPhyMode(WifiUtils.getPhyMode(context));
                diagnostics.setssid(ssid);
                diagnostics.setipAddress(config.getTunnelIp());
                diagnostics.setRouterip(currentItem.getTunnelIp());
                diagnostics.setssidTrustStatus(checkIfAnyWifiIsTrusted() ? "trusted" : "untrusted");
                diagnostics.setChannel(String.valueOf(WifiUtils.getWifiChannel(context)));
                diagnostics.setTxRate(String.valueOf(getBackend().getStatistics(getTunnel()).totalTx()));
                diagnostics.setNoise(String.valueOf(WifiUtils.getNoise(context)));
                diagnostics.setssidSecurity(WifiUtils.getWifiSecurity(context, ssid));
                diagnostics.setTunnelStatus("Connected");
                diagnostics.setPortName("Wi-fi");
                diagnostics.setName("Wi-fi");
                String diagnosticsUrl = "https://naas.insidepacket.com/broker/service/" + basicInformation.getTenantName() + "/wg_tunnel/" + basicInformation.getUsername() + "/" + currentItem.getTunnelId() + "/device_data";
                Log.e("diagnosticsUrl", "" + diagnosticsUrl);
                JSONObject object = new JSONObject();
                apiClient.postRequest(diagnosticsUrl, object, new VolleyCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        Toast.makeText(context, "diagnostics successful!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(JSONObject tenantListResult, JSONObject tunnelJson) {

                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(context, "diagnostics failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(context, "diagnostics failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void cancelAllNotifications() {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public void startVpn(Context context, boolean isWifiChanged) {
        boolean isConnected = checkIfTunnelIsConnected();
        Log.e("isConnectedtunnel", "" + isConnected);
        if (isConnected) {
            try {
                String txInMb = formatDecimal(bytesToKB(backend.getStatistics(tunnel).totalRx()));
                String rxInMb = formatDecimal(bytesToKB(backend.getStatistics(tunnel).totalTx()));
                String traffic = txInMb + "/" + rxInMb;
                callback.onTrafficSent(traffic, config.getRemoteIp(), currentItem.getTunnelIp());
            } catch (Exception e) {
//                cancelAllNotifications();
            }
        } else {
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
                    config = connectionModel.getConfigModel();
                    Intent ii = new Intent(context, SplashActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, ii, PendingIntent.FLAG_IMMUTABLE);
                    new Utils().showPermanentNotification(context, CHANNEL_ID, "Vpn is Running!", NOTIFICATION_ID, pendingIntent);
                    connectVpn(context, currentItem, connectionModel.getConfigModel());
                    postDiagnosticApi(context, isWifiChanged);
                }

                @Override
                public void onError(String message) {
//                Toast.makeText(context, "Diagnostics failed!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean checkIfTunnelIsConnected() {
        Backend backend = PersistentConnectionProperties.getInstance().getBackend();
        if (backend != null) {
            try {
                return backend.getState(PersistentConnectionProperties.getInstance().getTunnel()) == UP;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
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
//            PersistentConnectionProperties.getInstance().setBackend(new GoBackend(context));
//            backend = PersistentConnectionProperties.getInstance().getBackend();
            backend.getRunningTunnelNames();
        } catch (NullPointerException e) {
            PersistentConnectionProperties.getInstance().setBackend(new GoBackend(context));
            backend = PersistentConnectionProperties.getInstance().getBackend();
        }
        connect(context, item, configModel);
        ConnectedTunnelModel connectedTunnelModel = new ConnectedTunnelModel();
        connectedTunnelModel.setConnected(true);
        connectedTunnelModel.setTunnelIp(item.getTunnelIp());
        connectedTunnelModel.setPublicIp(configModel.getRemoteIp());
        connectedTunnelModel.setGateway("Dallas");
        new Utils().saveConnectedTunnel(context, connectedTunnelModel);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                try {
                    if (tunnel != null) {
                        if (backend.getState(PersistentConnectionProperties.getInstance().getTunnel()) == UP) {
                            String txInMb = formatDecimal(bytesToKB(backend.getStatistics(tunnel).totalRx()));
                            String rxInMb = formatDecimal(bytesToKB(backend.getStatistics(tunnel).totalTx()));
                            String traffic = txInMb + "/" + rxInMb;
                            //Log.e("trafficData", "" + traffic);
                            callback.onTrafficSent(traffic, configModel.getRemoteIp(), item.getTunnelIp());
                        }
                    }
                } catch (Exception e) {
                    Log.e("Exceptuionemake", "" + e.toString());
                    callback.onError(e.toString());
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void connect(Context mContext, Item item, ConfigModel configModel) {
        tunnel = PersistentConnectionProperties.getInstance().getTunnel();
        //send result to activity to show the traffic
        callback.onConnectionStart();

        Interface.Builder interfaceBuilder = new Interface.Builder();
        Peer.Builder peerBuilder = new Peer.Builder();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (backend.getState(PersistentConnectionProperties.getInstance().getTunnel()) == UP) {
                        backend.setState(tunnel, DOWN, null);
                    } else {
                        List<InetNetwork> allowedIp;
                        if (checkIfAnyWifiIsTrusted()) {
                            allowedIp = new Utils().parseAllowedIPs(configModel.getAllowedIps());
                        } else {
                            allowedIp = new Utils().parseAllowedIPs(configModel.getUntrustedAllowedIps());
                        }
                        Config.Builder builder = new Config.Builder();
                        SettingsSingleton.getInstance().setTunnelConnected(true);
//                        try {
                        Log.e("isBackendNull", "" + (backend != null));

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
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onWifiStateChanged(true, checkIfAnyWifiIsTrusted(), configModel, item);
                            }
                        });
//                        } catch (Exception e) {
//                            Log.e("Exception", "" + e.toString() + " " + Arrays.toString(e.getStackTrace()));
//                            activity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    callback.onError(e.toString());
//                                }
//                            });
//                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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