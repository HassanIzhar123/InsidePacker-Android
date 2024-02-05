package com.wireguard.insidepacker_android.activities;

import static com.wireguard.insidepacker_android.utils.SharedPrefsName._ACCESS_TOKEN;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._PREFS_NAME;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._USER_INFORMATION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.wireguard.insidepacker_android.R;
import com.wireguard.insidepacker_android.ViewModels.SignInViewModel.SignInViewModel;
import com.wireguard.insidepacker_android.models.AccessToken.AccessToken;
import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacker_android.utils.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    SignInViewModel signInViewModel;
    AppCompatActivity mContext;
    BasicInformation basicInformation = new BasicInformation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = this;
        signInViewModel = new ViewModelProvider(mContext).get(SignInViewModel.class);
        initializeViewModels();
        callAccessTokenApi();
    }

    private void initializeViewModels() {
        signInViewModel.getAccessTokenMutableLiveData().observe(mContext, new Observer<AccessToken>() {
            @Override
            public void onChanged(AccessToken accessToken) {
                PreferenceManager<String> stringPreferenceManager = new PreferenceManager<>(getApplicationContext(), _PREFS_NAME);
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
            PreferenceManager<String> stringPreferenceManager = new PreferenceManager<>(getApplicationContext(), _PREFS_NAME);
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