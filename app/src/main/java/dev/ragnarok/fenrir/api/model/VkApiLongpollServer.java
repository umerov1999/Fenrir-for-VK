package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VkApiLongpollServer {

    @SerializedName("key")
    public String key;

    @SerializedName("server")
    public String server;

    @SerializedName("ts")
    public long ts;

    @SerializedName("pts")
    public long pts;
}