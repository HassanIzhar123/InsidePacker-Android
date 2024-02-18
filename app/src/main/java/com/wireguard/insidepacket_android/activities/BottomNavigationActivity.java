package com.wireguard.insidepacket_android.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.wireguard.insidepacket_android.R;

import java.util.ArrayList;

import com.wireguard.insidepacket_android.adapters.ViewPagerAdapter;
import com.wireguard.insidepacket_android.fragments.HomeFragment;
import com.wireguard.insidepacket_android.fragments.SettingsFragment;
import com.wireguard.insidepacket_android.fragments.SupportFragment;
import com.wireguard.insidepacket_android.models.FragmentModel.FragmentModel;
import com.wireguard.insidepacket_android.utils.Utils;

public class BottomNavigationActivity extends BaseActivity {
    RelativeLayout exitBtn;
    ViewPager viewPager;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        new Utils().showToFullScreen(BottomNavigationActivity.this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_bottom_navigation);
//        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
//        WindowInsetsControllerCompat windowInsetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
//        // Hide system bars
//        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
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
        viewPager.setOffscreenPageLimit(3);
    }
}