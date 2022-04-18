package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiComment;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class DefaultCommentsResponse {

    @SerializedName("count")
    public int count;

    @Nullable
    @SerializedName("items")
    public List<VKApiComment> items;

    @Nullable
    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @Nullable
    @SerializedName("profiles")
    public List<VKApiUser> profiles;

}
