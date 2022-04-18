package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiComment;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiPoll;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class CustomCommentsResponse {

    // Parse manually in CustomCommentsResponseAdapter
    @Nullable
    public Main main;

    @Nullable
    public Integer firstId;

    @Nullable
    public Integer lastId;

    @Nullable
    public Integer admin_level;

    public static class Main {

        @SerializedName("count")
        public int count;

        @Nullable
        @SerializedName("items")
        public List<VKApiComment> comments;

        @Nullable
        @SerializedName("profiles")
        public List<VKApiUser> profiles;

        @Nullable
        @SerializedName("groups")
        public List<VKApiCommunity> groups;

        @Nullable
        @SerializedName("poll")
        public VKApiPoll poll;
    }

}
