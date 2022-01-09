package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class FriendsWithCountersResponse {

    @SerializedName("friends")
    public Items<VKApiUser> friends;

    @SerializedName("counters")
    public VKApiUser.Counters counters;
}
