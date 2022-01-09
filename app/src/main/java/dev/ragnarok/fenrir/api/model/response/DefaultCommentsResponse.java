package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiComment;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class DefaultCommentsResponse {

    @SerializedName("count")
    public int count;

    @SerializedName("items")
    public List<VKApiComment> items;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

}
