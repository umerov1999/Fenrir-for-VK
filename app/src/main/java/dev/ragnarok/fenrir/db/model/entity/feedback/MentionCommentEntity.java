package dev.ragnarok.fenrir.db.model.entity.feedback;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.db.model.entity.CommentEntity;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.EntityWrapper;
import dev.ragnarok.fenrir.model.feedback.FeedbackType;

/**
 * Base class for types [mention_comments, mention_comment_photo, mention_comment_video]
 */
public class MentionCommentEntity extends FeedbackEntity {

    @SerializedName("where")
    private CommentEntity where;
    @SerializedName("commented")
    private EntityWrapper commented = new EntityWrapper();

    @SuppressWarnings("unused")
    public MentionCommentEntity() {
    }

    public MentionCommentEntity(@FeedbackType int type) {
        setType(type);
    }

    public CommentEntity getWhere() {
        return where;
    }

    public MentionCommentEntity setWhere(CommentEntity where) {
        this.where = where;
        return this;
    }

    public Entity getCommented() {
        return commented.get();
    }

    public MentionCommentEntity setCommented(Entity commented) {
        this.commented = new EntityWrapper().wrap(commented);
        return this;
    }
}