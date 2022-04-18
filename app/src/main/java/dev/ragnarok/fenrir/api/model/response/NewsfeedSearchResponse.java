package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class NewsfeedSearchResponse {
    @Nullable
    @SerializedName("items")
    public List<VKApiPost> items;

    @Nullable
    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @Nullable
    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @Nullable
    @SerializedName("next_from")
    public String nextFrom;

    //@SerializedName("count")
    //public Integer count;

    //@SerializedName("total_count")
    //public Integer totalCount;
}
