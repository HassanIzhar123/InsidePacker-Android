package com.wireguard.insidepacker_android.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.wireguard.insidepacker_android.R;

import java.util.ArrayList;

import adapters.ViewPagerAdapter;
import fragments.SettingsFragment;
import fragments.SupportFragment;

import com.wireguard.insidepacker_android.fragments.HomeFragment;
import com.wireguard.insidepacker_android.models.FragmentModel.FragmentModel;

public class BottomNavigationActivity extends AppCompatActivity {
    RelativeLayout exitBtn;
    ViewPager viewPager;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.navigation);
        exitBtn = findViewById(R.id.exit_btn);
        onInitListener();
        initViewPager();
    }

    private ArrayList<FragmentModel> getAllFragments() {
        ArrayList<FragmentModel> fragments = new ArrayList<FragmentModel>();
        fragments.add(new FragmentModel("Home", new HomeFragment()));
        fragments.add(new FragmentModel("Support", new SupportFragment()));
        fragments.add(new FragmentModel("Settings", new SettingsFragment()));
        return fragments;
    }

    private void onInitListener() {
        exitBtn.setOnClickListener(v -> finishAndRemoveTask());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
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
    private void initViewPager() {
        ArrayList<FragmentModel> fragments = getAllFragments();
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), fragments));
    }
}