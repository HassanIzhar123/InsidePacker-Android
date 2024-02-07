package com.wireguard.insidepacker_android.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wireguard.insidepacker_android.R;
import com.wireguard.insidepacker_android.ViewModels.HomeViewModel.HomeViewModel;
import com.wireguard.insidepacker_android.adapters.MultiSelectionRecyclerViewAdapter;
import com.wireguard.insidepacker_android.adapters.TunnelSelectionRecyclerViewAdapter;
import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacker_android.models.UserTenants.Item;
import com.wireguard.insidepacker_android.utils.PreferenceManager;

import java.util.ArrayList;

public class GeneralSettingsFragment extends Fragment {
    View view;
    AppCompatActivity mContext;
    HomeViewModel homeViewModel;
    PreferenceManager<String> stringPreferenceManager;
    BasicInformation basicInformation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.general_settings, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.tunnel_recyclerview);
        TextView emptyTextView = view.findViewById(R.id.empty_tunnels_text);
        mContext = (AppCompatActivity) getContext();
        assert mContext != null;
        homeViewModel = new ViewModelProvider(mContext).get(HomeViewModel.class);
        homeViewModel.getConnectionMutableLiveData().observe(mContext, connectionModel -> {
            if (connectionModel != null) {
                if (!connectionModel.getUserTenants().getItems().isEmpty()) {
                    emptyTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                    TunnelSelectionRecyclerViewAdapter adapter = new TunnelSelectionRecyclerViewAdapter(connectionModel.getUserTenants().getItems()); // Provide your data list here
                    recyclerView.setAdapter(adapter);
                } else {
                    emptyTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            } else {
                emptyTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
        return view;
    }
}
