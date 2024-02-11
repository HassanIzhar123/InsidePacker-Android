package com.wireguard.insidepacket_android.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.models.UserTenants.Item;

import java.util.List;

public class TunnelSelectionRecyclerViewAdapter extends RecyclerView.Adapter<TunnelSelectionRecyclerViewAdapter.ExampleViewHolder> {

    private final List<Item> dataList;
    private int selectedItem = 0; // Initially no item selected
    TunnelSelectionListener tunnelSelectionListener;

    public TunnelSelectionRecyclerViewAdapter(List<Item> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tunnel_item_layout, parent, false);
        return new ExampleViewHolder(view, tunnelSelectionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder holder, int position) {
        Item data = dataList.get(position);
        String name = data.getName() + " - " + data.getTunnelIp();
        holder.tunnelText.setText(name);
        holder.radioButton.setChecked(position == selectedItem);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void onCLickListener(TunnelSelectionListener tunnelSelectionListener) {
        this.tunnelSelectionListener = tunnelSelectionListener;

    }

    public class ExampleViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;
        TextView tunnelText;


        public ExampleViewHolder(@NonNull View itemView, TunnelSelectionListener tunnelSelectionListener) {
            super(itemView);
            tunnelText = itemView.findViewById(R.id.textView);
            radioButton = itemView.findViewById(R.id.radio_button);
            radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedItem = getAdapterPosition();
                    if (tunnelSelectionListener != null) {
                        tunnelSelectionListener.onTunnelSelected(selectedItem);
                    }
                }
            });
        }
    }

    public interface TunnelSelectionListener {
        void onTunnelSelected(int position);
    }
}
