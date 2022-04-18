package dev.ragnarok.fenrir.api.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Items<I> {

    @SerializedName("count")
    public int count;

    @Nullable
    @SerializedName("items")
    public List<I> items;

    public int getCount() {
        return count;
    }
}
