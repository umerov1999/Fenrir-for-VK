package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VKApiTopic;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VKApiVideo;

public class NewsfeedCommentsResponse {

    @SerializedName("items")
    public List<Dto> items;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @SerializedName("next_from")
    public String nextFrom;

    public abstract static class Dto {

    }

    public static class PostDto extends Dto {

        public final VKApiPost post;

        public PostDto(VKApiPost post) {
            this.post = post;
        }
    }

    public static class PhotoDto extends Dto {

        public final VKApiPhoto photo;

        public PhotoDto(VKApiPhoto photo) {
            this.photo = photo;
        }
    }

    public static class VideoDto extends Dto {

        public final VKApiVideo video;

        public VideoDto(VKApiVideo video) {
            this.video = video;
        }
    }

    public static class TopicDto extends Dto {

        public final VKApiTopic topic;

        public TopicDto(VKApiTopic topic) {
            this.topic = topic;
        }
    }
}