package com.wireguard.insidepacket_android.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.essentials.SettingsSingleton;
import com.wireguard.insidepacket_android.utils.Utils;

public class SupportFragment extends Fragment {
    View view;
    SettingsSingleton settingsSingleton = SettingsSingleton.getInstance();
    SwitchCompat switchButton;
Boolean isSendOnCrashReportTouched = false;
    public SupportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.support_fragment, container, false);
        switchButton = view.findViewById(R.id.send_crash_switch);
        setUi();
        setClickListeners();
        return view;
    }

    private void setUi() {
        switchButton.setChecked(settingsSingleton.getSettings().getSendCrashReports());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setClickListeners() {
        switchButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isSendOnCrashReportTouched = true;
                return false;
            }
        });
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isSendOnCrashReportTouched) {
                    isSendOnCrashReportTouched = false;
                    settingsSingleton.getSettings().setSendCrashReports(isChecked);
                    new Utils().saveSettings(getContext(), settingsSingleton.getSettings());
                }
            }
        });
    }
}
