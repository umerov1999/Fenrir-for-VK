package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiUser;

public class AccountsBannedResponce {
    @SerializedName("profiles")
    public List<VKApiUser> profiles;
}
