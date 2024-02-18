package com.wireguard.insidepacket_android.activities;

import static com.wireguard.insidepacket_android.utils.AppStrings._ACCESS_TOKEN;
import static com.wireguard.insidepacket_android.utils.AppStrings._PREFS_NAME;
import static com.wireguard.insidepacket_android.utils.AppStrings._USER_INFORMATION;
import static com.wireguard.insidepacket_android.utils.WifiUtils.saveConnectedWifi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.ViewModels.SignInViewModel.SignInViewModel;
import com.wireguard.insidepacket_android.essentials.SettingsSingleton;
import com.wireguard.insidepacket_android.models.AccessToken.AccessToken;
import com.wireguard.insidepacket_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacket_android.models.settings.SettingsModel;
import com.wireguard.insidepacket_android.utils.PreferenceManager;
import com.wireguard.insidepacket_android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    SignInViewModel signInViewModel;
    AppCompatActivity mContext;
    BasicInformation basicInformation = new BasicInformation();
    // Define a permission constant
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE,
    };
    //    Manifest.permission.FOREGROUND_SERVICE,
//    Manifest.permission.FOREGROUND_SERVICE_LOCATION,

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
//        new Utils().showToFullScreen(SplashActivity.this);
        mContext = this;
        signInViewModel = new ViewModelProvider(SplashActivity.this).get(SignInViewModel.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(mContext)
                    .withPermissions(PERMISSIONS)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            callAccessTokenApi();
                            initializeViewModels();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    }).check();
        } else {
            Dexter.withContext(mContext)
                    .withPermissions(PERMISSIONS)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            callAccessTokenApi();
                            initializeViewModels();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    }).check();
        }

        SettingsModel settingsModel = new Utils().getSettings(mContext);
        SettingsSingleton.getInstance().setSettings(settingsModel);
        saveConnectedWifi(mContext);

    }

    private void initializeViewModels() {
        signInViewModel.getAccessTokenMutableLiveData().observe(mContext, new Observer<AccessToken>() {
            @Override
            public void onChanged(AccessToken accessToken) {
                PreferenceManager stringPreferenceManager = new PreferenceManager(getApplicationContext(), _PREFS_NAME);
                stringPreferenceManager.saveValue(_USER_INFORMATION, basicInformation.toJson());
                stringPreferenceManager.saveValue(_ACCESS_TOKEN, accessToken.getAccess_token());
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashActivity.this, BottomNavigationActivity.class));
                        finish();
                    }
                }, 3000);
            }
        });
        signInViewModel.getErrorMutableLiveData().observe(mContext, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                try {
                    JSONObject object = new JSONObject(s);
                    Toast.makeText(SplashActivity.this, object.getString("detail"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                }
                navigateToMainActivity();
            }
        });
    }

    private void callAccessTokenApi() {
        final boolean result = checkIfInternetIsAvailable();
        if (result) {
            PreferenceManager stringPreferenceManager = new PreferenceManager(getApplicationContext(), _PREFS_NAME);
            String json = stringPreferenceManager.getValue(_USER_INFORMATION, "");
            if (!json.isEmpty()) {
                Gson gson = new Gson();
                BasicInformation basicInformation = gson.fromJson(json, BasicInformation.class);
                signInViewModel.getAccessToken(mContext, setBasicInformation(basicInformation.getUsername(), basicInformation.getTenantName(), basicInformation.getPassword()));
            } else {
                navigateToMainActivity();
            }
        } else {
            navigateToMainActivity();
        }
    }

    private BasicInformation setBasicInformation(String actualUserName, String demo, String password) {
        basicInformation.setUsername(actualUserName);
        basicInformation.setPassword(password);
        basicInformation.setTenantName(demo);
        return basicInformation;
    }

    private Boolean checkIfInternetIsAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED);
    }

    private void navigateToMainActivity() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 3000);
    }
}