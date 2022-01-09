package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VKApiShortLink {

    @SerializedName("short_url")
    public String short_url;

    @SerializedName("url")
    public String url;

    @SerializedName("timestamp")
    public long timestamp;

    @SerializedName("access_key")
    public String access_key;

    @SerializedName("key")
    public String key;

    @SerializedName("views")
    public int views;
}
