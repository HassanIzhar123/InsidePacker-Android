package com.wireguard.insidepacker_android.models.UserTenants;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class UserTenants implements Serializable {
    @SerializedName("total_count")
    @Expose
    private long totalCount;
    private long count;
    private List<Item> items;

    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long value) { this.totalCount = value; }

    public long getCount() { return count; }
    public void setCount(long value) { this.count = value; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> value) { this.items = value; }
}


