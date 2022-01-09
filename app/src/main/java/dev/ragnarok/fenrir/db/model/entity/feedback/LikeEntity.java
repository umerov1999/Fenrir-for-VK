package dev.ragnarok.fenrir.db.model.entity.feedback;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.EntityWrapper;
import dev.ragnarok.fenrir.model.feedback.FeedbackType;

public class LikeEntity extends FeedbackEntity {

    @SerializedName("likesOwnerIds")
    private int[] likesOwnerIds;

    @SerializedName("liked")
    private EntityWrapper liked = new EntityWrapper();

    @SuppressWarnings("unused")
    public LikeEntity() {
    }

    public LikeEntity(@FeedbackType int type) {
        setType(type);
    }

    public int[] getLikesOwnerIds() {
        return likesOwnerIds;
    }

    public LikeEntity setLikesOwnerIds(int[] likesOwnerIds) {
        this.likesOwnerIds = likesOwnerIds;
        return this;
    }

    public Entity getLiked() {
        return liked.get();
    }

    public LikeEntity setLiked(Entity liked) {
        this.liked = new EntityWrapper().wrap(liked);
        return this;
    }
}