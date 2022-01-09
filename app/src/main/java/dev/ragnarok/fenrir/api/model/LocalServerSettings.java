package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class LocalServerSettings {
    @SerializedName("url")
    public String url;

    @SerializedName("password")
    public String password;

    @SerializedName("enabled")
    public boolean enabled;
}
