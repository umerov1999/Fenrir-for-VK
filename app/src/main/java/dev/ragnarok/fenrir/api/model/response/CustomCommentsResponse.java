package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiComment;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiPoll;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class CustomCommentsResponse {

    // Parse manually in CustomCommentsResponseAdapter

    public Main main;

    public Integer firstId;

    public Integer lastId;

    public Integer admin_level;

    public static class Main {

        @SerializedName("count")
        public int count;

        @SerializedName("items")
        public List<VKApiComment> comments;

        @SerializedName("profiles")
        public List<VKApiUser> profiles;

        @SerializedName("groups")
        public List<VKApiCommunity> groups;

        @SerializedName("poll")
        public VKApiPoll poll;
    }

}
