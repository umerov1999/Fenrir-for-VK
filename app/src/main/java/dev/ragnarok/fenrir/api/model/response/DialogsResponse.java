package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiDialog;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class DialogsResponse {

    @Nullable
    @SerializedName("items")
    public List<VKApiDialog> dialogs;

    @SerializedName("count")
    public int count;

    @SerializedName("unread_count")
    public int unreadCount;

    @Nullable
    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @Nullable
    @SerializedName("groups")
    public List<VKApiCommunity> groups;
}