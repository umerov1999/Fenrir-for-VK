package dev.ragnarok.fenrir.db.model.entity.feedback;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.db.model.entity.CommentEntity;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.EntityWrapper;
import dev.ragnarok.fenrir.model.feedback.FeedbackType;

public class LikeCommentEntity extends FeedbackEntity {

    @SerializedName("likesOwnerIds")
    private int[] likesOwnerIds;
    @SerializedName("commented")
    private EntityWrapper commented = new EntityWrapper();
    @SerializedName("liked")
    private CommentEntity liked;

    @SuppressWarnings("unused")
    public LikeCommentEntity() {
    }

    public LikeCommentEntity(@FeedbackType int type) {
        setType(type);
    }

    public CommentEntity getLiked() {
        return liked;
    }

    public LikeCommentEntity setLiked(CommentEntity liked) {
        this.liked = liked;
        return this;
    }

    public Entity getCommented() {
        return commented.get();
    }

    public LikeCommentEntity setCommented(Entity commented) {
        this.commented = new EntityWrapper().wrap(commented);
        return this;
    }

    public int[] getLikesOwnerIds() {
        return likesOwnerIds;
    }

    public LikeCommentEntity setLikesOwnerIds(int[] likesOwnerIds) {
        this.likesOwnerIds = likesOwnerIds;
        return this;
    }
}