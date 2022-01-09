package dev.ragnarok.fenrir.db.model.entity.feedback;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.db.model.entity.PostEntity;
import dev.ragnarok.fenrir.model.feedback.FeedbackType;

public class PostFeedbackEntity extends FeedbackEntity {

    @SerializedName("post")
    private PostEntity post;

    @SuppressWarnings("unused")
    public PostFeedbackEntity() {
    }

    public PostFeedbackEntity(@FeedbackType int type) {
        setType(type);
    }

    public PostEntity getPost() {
        return post;
    }

    public PostFeedbackEntity setPost(PostEntity post) {
        this.post = post;
        return this;
    }
}