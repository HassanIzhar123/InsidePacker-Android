package com.wireguard.insidepacker_android.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.wireguard.insidepacker_android.R;
import com.wireguard.insidepacker_android.models.UserTenants.Item;

import java.util.List;

public class TunnelSelectionRecyclerViewAdapter extends RecyclerView.Adapter<TunnelSelectionRecyclerViewAdapter.ExampleViewHolder> {

    private final List<Item> dataList;
    private int selectedItem = 0; // Initially no item selected

    public TunnelSelectionRecyclerViewAdapter(List<Item> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tunnel_item_layout, parent, false);
        return new ExampleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder holder, int position) {
        Item data = dataList.get(position);
        holder.tunnelText.setText(data.getName());
        holder.radioButton.setChecked(position == selectedItem);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ExampleViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;
        TextView tunnelText;

        public ExampleViewHolder(@NonNull View itemView) {
            super(itemView);
            tunnelText = itemView.findViewById(R.id.textView);
            radioButton = itemView.findViewById(R.id.radio_button);

            radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedItem = getAdapterPosition();
//                    notifyDataSetChanged(); // Refresh the UI
                }
            });
        }
    }
}
