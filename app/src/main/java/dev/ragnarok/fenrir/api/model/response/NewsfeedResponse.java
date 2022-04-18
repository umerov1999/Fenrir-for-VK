package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiNews;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class NewsfeedResponse {

    @Nullable
    @SerializedName("items")
    public List<VKApiNews> items;

    @Nullable
    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @Nullable
    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @Nullable
    @SerializedName("next_from")
    public String nextFrom;

}
