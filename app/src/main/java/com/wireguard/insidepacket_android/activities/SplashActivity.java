package com.wireguard.insidepacket_android.activities;

import static com.wireguard.insidepacket_android.utils.SharedPrefsName._ACCESS_TOKEN;
import static com.wireguard.insidepacket_android.utils.SharedPrefsName._PREFS_NAME;
import static com.wireguard.insidepacket_android.utils.SharedPrefsName._USER_INFORMATION;
import static com.wireguard.insidepacket_android.utils.WifiUtils.saveConnectedWifi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
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

import java.util.ArrayList;
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
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
    };
//    Manifest.permission.FOREGROUND_SERVICE,
//    Manifest.permission.FOREGROUND_SERVICE_LOCATION,
    // Permission request code
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Utils().showToFullScreen(SplashActivity.this);
        mContext = this;
        signInViewModel = new ViewModelProvider(SplashActivity.this).get(SignInViewModel.class);
        // Check if permission is granted, if not, request it
        if (!arePermissionsGranted()) {
            requestPermissions();
        } else {
            // All permissions already granted, proceed with your task
            performTask();
        }
//        new Utils().askForPermission(SplashActivity.this);
        SettingsModel settingsModel = new Utils().getSettings(mContext);
        SettingsSingleton.getInstance().setSettings(settingsModel);
        saveConnectedWifi(mContext);

    }

    // Activity result launcher for permission request
    private final ActivityResultLauncher<String[]> requestMultiplePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                List<String> deniedPermissions = new ArrayList<>();
                for (String permission : PERMISSIONS) {
                    if (Boolean.FALSE.equals(isGranted.get(permission))) {
                        deniedPermissions.add(permission);
                    }
                }
                if (deniedPermissions.isEmpty()) {
                    // All permissions granted, proceed with your task
                    performTask();
                } else {
                    // Some permissions are denied
                    for (String deniedPermission : deniedPermissions) {
                        if (shouldShowRequestPermissionRationale(deniedPermission)) {
                            // Explain to the user why the permission is needed and ask again
                            showPermissionRationale(deniedPermission);
                        } else {
                            // Permission is permanently denied
                            showPermissionDeniedDialog();
                            return;
                        }
                    }
                }
            });

    // Method to request permissions
    private void requestPermissions() {
        requestMultiplePermissionLauncher.launch(PERMISSIONS);
    }

    // Method to check if all permissions are granted
    private boolean arePermissionsGranted() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // Method to perform your task after permissions are granted
    private void performTask() {
        callAccessTokenApi();
        initializeViewModels();
    }

    // Method to show permission rationale for a specific permission
    private void showPermissionRationale(String permission) {
        // Show rationale to the user why the permission is needed for the specific permission
        // You can show a dialog or toast message explaining the need for the permission
    }

    // Method to show permission denied dialog
    private void showPermissionDeniedDialog() {
        // Show a dialog to the user explaining that the app cannot function properly without the permissions
        // Provide an option to open app settings to manually grant the permissions
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This app requires some permissions to function properly. Please grant the permissions in settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    openAppSettings();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Handle cancellation
                    Toast.makeText(SplashActivity.this, "Permissions denied", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    // Method to open app settings
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, PERMISSION_REQUEST_CODE);
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // All permissions granted, proceed with your task
                performTask();
            } else {
                // Permissions denied
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 100) {
//            boolean allPermissionsGranted = true;
//            for (int result : grantResults) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    allPermissionsGranted = false;
//                    break;
//                }
//            }
//            if (!allPermissionsGranted) {
//                Toast.makeText(mContext, "Please grant all permissions in the settings", Toast.LENGTH_SHORT).show();
//            }
////            callAccessTokenApi();
////            initializeViewModels();
//        }
//    }

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
//                    throw new RuntimeException(e);
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