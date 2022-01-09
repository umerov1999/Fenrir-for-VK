package dev.ragnarok.fenrir.db.model.entity.feedback;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.db.model.entity.CommentEntity;
import dev.ragnarok.fenrir.model.feedback.FeedbackType;

public abstract class FeedbackEntity {
    @SerializedName("type")
    private @FeedbackType
    int type;
    @SerializedName("date")
    private long date;
    @SerializedName("reply")
    private CommentEntity reply;

    public @FeedbackType
    int getType() {
        return type;
    }

    protected void setType(@FeedbackType int type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public FeedbackEntity setDate(long date) {
        this.date = date;
        return this;
    }

    public CommentEntity getReply() {
        return reply;
    }

    public FeedbackEntity setReply(CommentEntity reply) {
        this.reply = reply;
        return this;
    }
}