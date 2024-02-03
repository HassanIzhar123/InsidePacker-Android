package com.wireguard.insidepacker_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.wireguard.insidepacker_android.R;
import com.wireguard.insidepacker_android.view_models.SignInViewModel;

public class SignInActivity extends AppCompatActivity {
    private SignInViewModel signInViewModel;
    Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        signInButton = findViewById(R.id.sign_in_btn);
        signInViewModel = new ViewModelProvider(this).get(SignInViewModel.class);
        OnInit();
        OnInitListener();

    }

    private void OnInit() {
        signInViewModel.getData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String data) {
                Toast.makeText(SignInActivity.this, data, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void OnInitListener() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }
}