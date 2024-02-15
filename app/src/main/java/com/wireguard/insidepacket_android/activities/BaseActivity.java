package com.wireguard.insidepacket_android.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.utils.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class BaseActivity extends AppCompatActivity implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler _androidUncaughtExceptionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        _androidUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
//        Thread.setDefaultUncaughtExceptionHandler(this);
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
//                Log.e("Alert", "Lets See if it Works !!!");
//                Toast.makeText(BaseActivity.this, "Lets See if it Works !!!", Toast.LENGTH_SHORT).show();

//            }
//        });
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
//            new Utils().sendEmailWithLogs(getApplicationContext(),ex.toString());

    }

}
