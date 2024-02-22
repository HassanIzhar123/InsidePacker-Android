package com.wireguard.insidepacket_android.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.wireguard.insidepacket_android.R;

@SuppressLint("CustomSplashScreen")
public class MainActivity extends AppCompatActivity {
    Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signInButton = findViewById(R.id.sign_in_btn);
        ImageView personImg = findViewById(R.id.person_gif);
        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.person_standing_gif)
                .placeholder(R.drawable.person_standing_gif)
                .into(personImg);
        signInButtonClick();
    }

    private void signInButtonClick() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                finish();
            }
        });
    }
}