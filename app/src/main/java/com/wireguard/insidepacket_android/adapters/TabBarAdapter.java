package com.wireguard.insidepacket_android.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.wireguard.insidepacket_android.fragments.GeneralSettingsFragment;
import com.wireguard.insidepacket_android.fragments.NetworkSettingsFragment;

public class TabBarAdapter extends FragmentPagerAdapter {

    int totalTabs;
  
    public TabBarAdapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        this.totalTabs = totalTabs;
    }  
  
    @NonNull
    @Override
    public Fragment getItem(int position) {
        return switch (position) {
            case 0 -> new GeneralSettingsFragment();
            case 1 -> new NetworkSettingsFragment();
            default -> null;
        };
    }  
    @Override
    public int getCount() {  
        return totalTabs;  
    }  
}  