package com.wireguard.insidepacket_android.activities;

import static com.wireguard.insidepacket_android.utils.AppStrings._ACCESS_TOKEN;
import static com.wireguard.insidepacket_android.utils.AppStrings._PREFS_NAME;
import static com.wireguard.insidepacket_android.utils.AppStrings._USER_INFORMATION;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.ViewModels.SignInViewModel.SignInViewModel;
import com.wireguard.insidepacket_android.models.AccessToken.AccessToken;
import com.wireguard.insidepacket_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacket_android.utils.PreferenceManager;
import com.wireguard.insidepacket_android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ResetPasswordActivity extends AppCompatActivity {
    Button signInButton;
    ImageButton unseenBtn, confirmUnseenBtn;
    EditText userNameEditText, passwordEditText, confirmPasswordEditText;
    AppCompatActivity mContext;
    SignInViewModel signInViewModel;
    Dialog progressDialog;
    BasicInformation basicInformation = new BasicInformation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        new Utils().showToFullScreen(ResetPasswordActivity.this);
        mContext = this;
        signInViewModel = new ViewModelProvider(mContext).get(SignInViewModel.class);
        signInButton = findViewById(R.id.sign_in_btn);
        unseenBtn = findViewById(R.id.unseen_btn);
        confirmUnseenBtn = findViewById(R.id.confirm_unseen_btn);
        userNameEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edittext);
        OnInitListener();
        initializeViewModels();
    }

    private void OnInitListener() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userNameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();
                if (username.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "Please enter username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (confirmPassword.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "Please enter confirm password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!username.matches("^[a-zA-Z0-9]+@[a-zA-Z0-9]+(\\.[a-zA-Z]{1,})?$")) {
                    Toast.makeText(ResetPasswordActivity.this, "Please enter valid username", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    String actualUserName = username.split("@")[0];
                    String demo = username.split("@")[1];
                    resetPassword(actualUserName, demo, password, confirmPassword);
                } catch (Exception e) {
                    Toast.makeText(ResetPasswordActivity.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        unseenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordEditText.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    passwordEditText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
                } else {
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
        unseenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordEditText.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    passwordEditText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
                } else {
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
        confirmUnseenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmPasswordEditText.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    confirmPasswordEditText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
                } else {
                    confirmPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

    }

    private BasicInformation setBasicInformation(String actualUserName, String demo, String password, String confirmPassword) {
        basicInformation.setUsername(actualUserName);
        basicInformation.setPassword(password);
        basicInformation.setTenantName(demo);
        basicInformation.setNewPassword(confirmPassword);
        return basicInformation;
    }

    private void initializeViewModels() {
        progressDialog = new Utils().showProgressDialog(ResetPasswordActivity.this);
        signInViewModel.getAccessTokenMutableLiveData().observe(mContext, new Observer<AccessToken>() {
            @Override
            public void onChanged(AccessToken accessToken) {
                PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext(), _PREFS_NAME);
                preferenceManager.saveValue(_USER_INFORMATION, basicInformation.toJson());
                preferenceManager.saveValue(_ACCESS_TOKEN, accessToken.getAccess_token());
                progressDialog.dismiss();
                Toast.makeText(ResetPasswordActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ResetPasswordActivity.this, BottomNavigationActivity.class));
                finish();
            }
        });
        signInViewModel.getErrorMutableLiveData().observe(mContext, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                try {
                    JSONObject object = new JSONObject(s);
                    String exceptionDetail = object.getString("detail");
                    Toast.makeText(ResetPasswordActivity.this, exceptionDetail, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
//                    throw new RuntimeException(e);
                }
                progressDialog.dismiss();
            }
        });
    }

    private void resetPassword(String actualUserName, String demo, String password, String confirmPassword) {
        progressDialog.show();
        signInViewModel.resetPassword(mContext, setBasicInformation(actualUserName, demo, password, confirmPassword));
    }
}