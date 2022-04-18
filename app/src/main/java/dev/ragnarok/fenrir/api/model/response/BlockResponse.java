package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class BlockResponse<T> {
    @Nullable
    @SerializedName("block")
    public T block;
}
