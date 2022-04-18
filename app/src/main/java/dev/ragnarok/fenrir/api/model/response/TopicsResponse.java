package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiTopic;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class TopicsResponse {

    @SerializedName("count")
    public int count;

    @Nullable
    @SerializedName("items")
    public List<VKApiTopic> items;

    @SerializedName("default_order")
    public int defaultOrder;

    @SerializedName("can_add_topics")
    public int canAddTopics;

    @Nullable
    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @Nullable
    @SerializedName("groups")
    public List<VKApiCommunity> groups;
}