package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.feedback.VKApiBaseFeedback;

public class NotificationsResponse {
    @SerializedName("count")
    public int count;

    @Nullable
    @SerializedName("items")
    public List<VKApiBaseFeedback> notifications;

    @Nullable
    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @Nullable
    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @Nullable
    @SerializedName("next_from")
    public String nextFrom;

    @SerializedName("last_viewed")
    public long lastViewed;
}
