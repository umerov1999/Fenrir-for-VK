package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiUser;

public class AccountsBannedResponce {
    @Nullable
    @SerializedName("profiles")
    public List<VKApiUser> profiles;
}
