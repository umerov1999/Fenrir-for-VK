package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VKApiTopic;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VKApiVideo;

public class NewsfeedCommentsResponse {

    @Nullable
    @SerializedName("items")
    public List<Dto> items;

    @Nullable
    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @Nullable
    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @Nullable
    @SerializedName("next_from")
    public String nextFrom;

    public abstract static class Dto {

    }

    public static class PostDto extends Dto {
        @Nullable
        public final VKApiPost post;

        public PostDto(@Nullable VKApiPost post) {
            this.post = post;
        }
    }

    public static class PhotoDto extends Dto {
        @Nullable
        public final VKApiPhoto photo;

        public PhotoDto(@Nullable VKApiPhoto photo) {
            this.photo = photo;
        }
    }

    public static class VideoDto extends Dto {
        @Nullable
        public final VKApiVideo video;

        public VideoDto(@Nullable VKApiVideo video) {
            this.video = video;
        }
    }

    public static class TopicDto extends Dto {
        @Nullable
        public final VKApiTopic topic;

        public TopicDto(@Nullable VKApiTopic topic) {
            this.topic = topic;
        }
    }
}