package com.wireguard.insidepacker_android.adapters;

import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.wireguard.insidepacker_android.R;

import java.util.List;

public class MultiSelectionRecyclerViewAdapter extends RecyclerView.Adapter<MultiSelectionRecyclerViewAdapter.ViewHolder> {

    private final List<String> items;
    private final SparseBooleanArray selectedItems;

    public MultiSelectionRecyclerViewAdapter(List<String> items) {
        this.items = items;
        selectedItems = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = items.get(position);
        holder.textView.setText(item);
        holder.checkBox.setChecked(selectedItems.get(position, false));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                selectedItems.put(position, true);
            else
                selectedItems.delete(position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            textView = itemView.findViewById(R.id.textView);
        }
    }

    public SparseBooleanArray getSelectedItems() {
        return selectedItems;
    }
}
