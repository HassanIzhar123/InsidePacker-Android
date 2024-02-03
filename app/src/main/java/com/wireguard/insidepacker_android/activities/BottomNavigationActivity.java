package com.wireguard.insidepacker_android.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.wireguard.insidepacker_android.R;

import java.util.ArrayList;
import java.util.List;

import adapters.ViewPagerAdapter;
import fragments.HomeFragment;
import fragments.SettingsFragment;
import fragments.SupportFragment;
import models.FragmentModel;

public class BottomNavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        ViewPager viewPager = findViewById(R.id.view_pager);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        ArrayList<FragmentModel> fragments = getAllFragments();
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), fragments));
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    bottomNavigationView.setSelectedItemId(R.id.menu_home);
                } else if (position == 1) {
                    bottomNavigationView.setSelectedItemId(R.id.menu_support);
                } else if (position == 2) {
                    bottomNavigationView.setSelectedItemId(R.id.menu_settings);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_home) {
                    viewPager.setCurrentItem(0);
                } else if (item.getItemId() == R.id.menu_support) {
                    viewPager.setCurrentItem(1);

                } else if (item.getItemId() == R.id.menu_settings) {
                    viewPager.setCurrentItem(2);

                }
                return false;
            }
        });
    }

    private ArrayList<FragmentModel> getAllFragments() {
        ArrayList<FragmentModel> fragments = new ArrayList<FragmentModel>();
        fragments.add(new FragmentModel("Home", new HomeFragment()));
        fragments.add(new FragmentModel("Support", new SupportFragment()));
        fragments.add(new FragmentModel("Settings", new SettingsFragment()));
        return fragments;
    }
}