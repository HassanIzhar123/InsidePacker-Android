package com.wireguard.insidepacker_android.fragments;

import static com.wireguard.insidepacker_android.utils.SharedPrefsName._PREFS_NAME;
import static com.wireguard.insidepacker_android.utils.SharedPrefsName._SELECTED_WIFI;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wireguard.insidepacker_android.R;
import com.wireguard.insidepacker_android.adapters.MultiSelectionRecyclerViewAdapter;
import com.wireguard.insidepacker_android.utils.PreferenceManager;
import com.wireguard.insidepacker_android.utils.WifiUtils;

import org.json.JSONArray;

import java.util.Arrays;
import java.util.List;

public class NetworkSettingsFragment extends Fragment {
    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.network_settings, container, false);
        List<String> items = WifiUtils.getPreviouslyConnectedWifiNames(getContext());
        Log.e("WifiNames",""+items);
        RecyclerView recyclerView = view.findViewById(R.id.tunnel_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MultiSelectionRecyclerViewAdapter adapter = new MultiSelectionRecyclerViewAdapter(items);
        recyclerView.setAdapter(adapter);

        Button submitButton = view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> {
            SparseBooleanArray selectedItems = adapter.getSelectedItems();
            for (int i = 0; i < selectedItems.size(); i++) {
                int position = selectedItems.keyAt(i);
                if (selectedItems.get(position)) {
                    String selectedItem = items.get(position);
                    Log.e("Selected Item", selectedItem);
                    JSONArray jsonArray = new JSONArray(Arrays.asList(selectedItem));
                    PreferenceManager<String> stringPreferenceManager = new PreferenceManager<>(getContext(), _PREFS_NAME);
//                    stringPreferenceManager.saveValue(_SELECTED_WIFI, jsonArray.toString());
                }
            }
        });
        return view;
    }
}
