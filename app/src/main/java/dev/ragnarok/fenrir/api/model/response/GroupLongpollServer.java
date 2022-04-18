package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class GroupLongpollServer {

    @Nullable
    @SerializedName("key")
    public String key;

    @Nullable
    @SerializedName("server")
    public String server;

    @Nullable
    @SerializedName("ts")
    public String ts;
}