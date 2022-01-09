package dev.ragnarok.fenrir.db.model.entity.feedback;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.db.model.entity.CommentEntity;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.EntityWrapper;
import dev.ragnarok.fenrir.model.feedback.FeedbackType;

public class NewCommentEntity extends FeedbackEntity {

    @SerializedName("commented")
    private EntityWrapper commented = new EntityWrapper();
    @SerializedName("comment")
    private CommentEntity comment;

    @SuppressWarnings("unused")
    public NewCommentEntity() {
    }

    public NewCommentEntity(@FeedbackType int type) {
        setType(type);
    }

    public CommentEntity getComment() {
        return comment;
    }

    public NewCommentEntity setComment(CommentEntity comment) {
        this.comment = comment;
        return this;
    }

    public Entity getCommented() {
        return commented.get();
    }

    public NewCommentEntity setCommented(Entity commented) {
        this.commented = new EntityWrapper().wrap(commented);
        return this;
    }
}