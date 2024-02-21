package com.wireguard.insidepacket_android.fragments;

import static android.content.Context.RECEIVER_EXPORTED;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
import static androidx.core.app.ServiceCompat.stopForeground;
import static androidx.core.content.ContextCompat.registerReceiver;
import static androidx.core.content.ContextCompat.startForegroundService;
import static com.wireguard.android.backend.Tunnel.State.DOWN;
import static com.wireguard.android.backend.Tunnel.State.UP;
import static com.wireguard.insidepacket_android.utils.AppStrings.CHANNEL_ID;
import static com.wireguard.insidepacket_android.utils.AppStrings._ACCESS_TOKEN;
import static com.wireguard.insidepacket_android.utils.AppStrings._PREFS_NAME;
import static com.wireguard.insidepacket_android.utils.AppStrings._USER_INFORMATION;
import static com.wireguard.insidepacket_android.utils.Utils.bytesToKB;
import static com.wireguard.insidepacket_android.utils.Utils.formatDecimal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
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
import androidx.lifecycle.MutableLiveData;
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
import com.wireguard.insidepacket_android.models.settings.SettingsModel;
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
    boolean isAnyWifiTrusted = false, countdownTimerFinished = false;

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
        progressDialog = new Utils().showProgressDialog(mContext);
        progressDialog.show();
        wifiReceiver = new WifiReceiver(getActivity(), new WifiStateChangeListener() {
            @Override
            public void onWifiStateChanged(boolean isConnected, boolean isTrusted, ConfigModel configModel, Item item) {
                currentItem = item;
                Log.e("isConnected", "" + isConnected + " " + isTrusted);
                toggleViews(isConnected, isTrusted);
                if (progressDialog != null) {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onTrafficSent(String traffic, String publicIp, String tunnelIp) {
                trafficStatus.setText(traffic);
                publicIpStatus.setText(publicIp);
                tunnelIpStatus.setText(tunnelIp);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(mContext, "" + error, Toast.LENGTH_SHORT).show();
            }
        });
        IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        ContextCompat.registerReceiver(mContext, wifiReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
        SettingsModel settingsModel = new Utils().getSettings(mContext);
        List<TrustedWifi> trustedWifiList = settingsModel.getTrustedWifi();
        isAnyWifiTrusted = false;
        for (int i = 0; i < trustedWifiList.size(); i++) {
            if (trustedWifiList.get(i).getSelected()) {
                isAnyWifiTrusted = true;
                break;
            }
        }
        Log.e("isAnyWifiTrusted", "" + isAnyWifiTrusted);
        if (!isAnyWifiTrusted) {
            wifiReceiver.startVpn(mContext, false);
        } else {
            if (settingsModel.getAlwaysOnVpn()) {
                wifiReceiver.startVpn(mContext, false);
            }
        }
    }

    public void disconnectVpn() {
        try {
            wifiReceiver.getBackend().setState(wifiReceiver.getTunnel(), UP, null);
        } catch (Exception e) {
            Log.e("wifireciever", "" + e.toString());
        }
//                mContext.unregisterReceiver(wifiReceiver);
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void onClickListeners() {
        connectedButton.setOnClickListener(new DebouncedOnClickListener(100) {
            @Override
            public void onDebouncedClick(View v) {
                connectAlwaysOnVpn();
            }
        });

        disconnectButton.setOnClickListener(new DebouncedOnClickListener(100) {
            @Override
            public void onDebouncedClick(View v) {
                disconnectVpn();
                toggleViews(false, false);
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
    }

    private void initializeSharedPreference() {
        preferenceManager = new PreferenceManager(mContext, _PREFS_NAME);
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
        homeViewModel = new ViewModelProvider(mContext).get(HomeViewModel.class);
        getUserData();
        homeViewModel.getConnectionMutableLiveData().observe(mContext, connectionModel -> {
            progressDialog = new Utils().showProgressDialog(mContext);
            progressDialog.show();
            connectAlwaysOnVpn();
        });
        homeViewModel.getErrorUserListMutableList().observe(mContext, s -> {
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        });
        homeViewModel.getAccessOrganizationLiveData().observe(mContext, s -> {
            Log.e("timeLeft", "" + s);
            if (s != null && !s.equals("null")) {
                handleStartTimer(s);
            } else {
                Toast.makeText(mContext, "Failed accessing organization!", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();

        });
        homeViewModel.getTimeLeftErrorLiveData().observe(mContext, s -> {
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        });
        homeViewModel.getDataTransferMutableLiveData().observe(mContext, object -> {
            if (object != null) {
                if (object instanceof Boolean) {
                    boolean isTrustedMarked = (Boolean) object;
                    if (isTrustedMarked) {
                        boolean isServiceRunning = isServiceRunningInForeground(mContext, BroadcastService.class);
                        Log.e("isServiceRunning", "" + isServiceRunning);
                        if (isServiceRunning) {
                            handleCancelTimer();
                        }
                        toggleViews(true, true);
                    } else {
                        disconnectVpn();
                        toggleViews(false, false);
                    }
                }
            }
        });
    }

    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }

            }
        }
        return false;
    }

    private void getUserData() {
        String json = preferenceManager.getValue(_USER_INFORMATION, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            basicInformation = gson.fromJson(json, BasicInformation.class);
        }
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
        Intent intent = new Intent((Activity) mContext, BroadcastService.class);
        intent.putExtra("maxCountDownValue", milliseconds);
        startForegroundService(mContext, intent);
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
            countdownTimerFinished = intent.getBooleanExtra("countdownTimerFinished", true);
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
