package com.wireguard.insidepacker_android.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.wireguard.insidepacker_android.R;
import com.wireguard.insidepacker_android.ViewModels.SignInViewModel;
import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;

public class SignInActivity extends AppCompatActivity {
    Button signInButton;
    EditText userNameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        signInButton = findViewById(R.id.sign_in_btn);
        userNameEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        OnInitListener();

    }

    private Dialog showProgressDialog() {
        Dialog dialog = new Dialog(SignInActivity.this);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.loading_process_dialog);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }


    private void OnInitListener() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userNameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String actualUserName = username.split("@")[0];
                String demo = username.split("@")[1];
                if (username.isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Please enter username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(username.matches(".*@.*@.*")){
                    Toast.makeText(SignInActivity.this, "Please enter valid username", Toast.LENGTH_SHORT).show();
                    return;
                }
                OnInit(actualUserName, demo, password);
            }
        });
    }

    private void OnInit(String actualUserName, String demo, String password) {
        Dialog progressDialog = showProgressDialog();
        SignInViewModel signInViewModel = new ViewModelProvider(SignInActivity.this).get(SignInViewModel.class);
        signInViewModel.getAccessToken(setBasicInformation(actualUserName, demo, password)).observe(SignInActivity.this, accessToken -> {
            if (accessToken != null) {
                Toast.makeText(SignInActivity.this, "Success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SignInActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        });
    }

    private BasicInformation setBasicInformation(String actualUserName, String demo, String password) {
        BasicInformation basicInformation = new BasicInformation();
        basicInformation.setUsername(actualUserName);
        basicInformation.setPassword(password);
        basicInformation.setTenantName(demo);
        return basicInformation;
    }

}