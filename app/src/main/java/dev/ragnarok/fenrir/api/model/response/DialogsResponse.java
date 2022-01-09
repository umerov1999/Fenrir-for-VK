package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VkApiDialog;

public class DialogsResponse {

    @SerializedName("items")
    public List<VkApiDialog> dialogs;

    @SerializedName("count")
    public int count;

    @SerializedName("unread_count")
    public int unreadCount;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;
}