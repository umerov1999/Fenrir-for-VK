package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Items<I> {

    @SerializedName("count")
    public int count;

    @SerializedName("items")
    public List<I> items;

    public List<I> getItems() {
        return items;
    }

    public int getCount() {
        return count;
    }
}
