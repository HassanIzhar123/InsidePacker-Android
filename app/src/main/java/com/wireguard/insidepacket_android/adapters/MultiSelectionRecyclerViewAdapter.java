package com.wireguard.insidepacket_android.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.models.settings.TrustedWifi;

import java.util.List;

public class MultiSelectionRecyclerViewAdapter extends RecyclerView.Adapter<MultiSelectionRecyclerViewAdapter.ViewHolder> {

    private final List<TrustedWifi> items;
    private OnCheckedChangeListener checkedChangeListener;

    public MultiSelectionRecyclerViewAdapter(List<TrustedWifi> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkbox, parent, false);
        return new ViewHolder(view, checkedChangeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = items.get(position).getName();
        holder.textView.setText(item);

        // Check if the current item is in the list of selected items
        holder.checkBox.setChecked(items.get(position).getSelected());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textView;

        public ViewHolder(@NonNull View itemView, OnCheckedChangeListener checkedChangeListener) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            textView = itemView.findViewById(R.id.textView);
            checkBox.setOnClickListener(v -> {
                if (checkedChangeListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        boolean isChecked = checkBox.isChecked();
                        checkedChangeListener.onCheckedChanged(position, isChecked);
                    }
                }
            });
        }
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(int position, boolean isChecked);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.checkedChangeListener = listener;
    }
}
