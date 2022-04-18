package dev.ragnarok.fenrir.api.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class LocalServerSettings {
    @Nullable
    @SerializedName("url")
    public String url;

    @Nullable
    @SerializedName("password")
    public String password;

    @SerializedName("enabled")
    public boolean enabled;
}
