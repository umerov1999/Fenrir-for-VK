package dev.ragnarok.fenrir.api.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;


public class RefreshToken {
    @Nullable
    @SerializedName("token")
    public String token;

}
