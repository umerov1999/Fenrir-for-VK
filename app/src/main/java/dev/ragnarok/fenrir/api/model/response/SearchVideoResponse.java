package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VKApiVideo;

public class SearchVideoResponse {

    @SerializedName("count")
    public int count;

    @Nullable
    @SerializedName("items")
    public List<VKApiVideo> items;

    @Nullable
    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @Nullable
    @SerializedName("groups")
    public List<VKApiCommunity> groups;
}
