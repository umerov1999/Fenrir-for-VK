package dev.ragnarok.fenrir.model.feedback;

import android.os.Parcel;

import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.ParcelableModelWrapper;

public final class MentionCommentFeedback extends Feedback {

    public static final Creator<MentionCommentFeedback> CREATOR = new Creator<MentionCommentFeedback>() {
        @Override
        public MentionCommentFeedback createFromParcel(Parcel in) {
            return new MentionCommentFeedback(in);
        }

        @Override
        public MentionCommentFeedback[] newArray(int size) {
            return new MentionCommentFeedback[size];
        }
    };
    private Comment where;
    private AbsModel commentOf;

    // one of FeedbackType.MENTION_COMMENT_POST, FeedbackType.MENTION_COMMENT_PHOTO, FeedbackType.MENTION_COMMENT_VIDEO
    public MentionCommentFeedback(@FeedbackType int type) {
        super(type);
    }

    private MentionCommentFeedback(Parcel in) {
        super(in);
        where = in.readParcelable(Comment.class.getClassLoader());
        commentOf = ParcelableModelWrapper.readModel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(where, flags);
        ParcelableModelWrapper.writeModel(dest, flags, commentOf);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public AbsModel getCommentOf() {
        return commentOf;
    }

    public MentionCommentFeedback setCommentOf(AbsModel commentOf) {
        this.commentOf = commentOf;
        return this;
    }

    public Comment getWhere() {
        return where;
    }

    public MentionCommentFeedback setWhere(Comment where) {
        this.where = where;
        return this;
    }
}