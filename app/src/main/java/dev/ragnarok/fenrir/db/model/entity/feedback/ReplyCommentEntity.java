package dev.ragnarok.fenrir.db.model.entity.feedback;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.db.model.entity.CommentEntity;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.EntityWrapper;
import dev.ragnarok.fenrir.model.feedback.FeedbackType;

public class ReplyCommentEntity extends FeedbackEntity {

    @SerializedName("commented")
    private EntityWrapper commented = new EntityWrapper();
    @SerializedName("ownComment")
    private CommentEntity ownComment;
    @SerializedName("feedbackComment")
    private CommentEntity feedbackComment;

    @SuppressWarnings("unused")
    public ReplyCommentEntity() {
    }

    public ReplyCommentEntity(@FeedbackType int type) {
        setType(type);
    }

    public Entity getCommented() {
        return commented.get();
    }

    public ReplyCommentEntity setCommented(Entity commented) {
        this.commented = new EntityWrapper().wrap(commented);
        return this;
    }

    public CommentEntity getFeedbackComment() {
        return feedbackComment;
    }

    public ReplyCommentEntity setFeedbackComment(CommentEntity feedbackComment) {
        this.feedbackComment = feedbackComment;
        return this;
    }

    public CommentEntity getOwnComment() {
        return ownComment;
    }

    public ReplyCommentEntity setOwnComment(CommentEntity ownComment) {
        this.ownComment = ownComment;
        return this;
    }
}