package dev.ragnarok.fenrir.model.feedback;

import android.os.Parcel;

import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.ParcelableModelWrapper;

public final class ReplyCommentFeedback extends Feedback {

    public static final Creator<ReplyCommentFeedback> CREATOR = new Creator<ReplyCommentFeedback>() {
        @Override
        public ReplyCommentFeedback createFromParcel(Parcel in) {
            return new ReplyCommentFeedback(in);
        }

        @Override
        public ReplyCommentFeedback[] newArray(int size) {
            return new ReplyCommentFeedback[size];
        }
    };
    private AbsModel commentsOf;
    private Comment ownComment;
    private Comment feedbackComment;

    public ReplyCommentFeedback(@FeedbackType int type) {
        super(type);
    }

    private ReplyCommentFeedback(Parcel in) {
        super(in);
        commentsOf = ParcelableModelWrapper.readModel(in);
        ownComment = in.readParcelable(Comment.class.getClassLoader());
        feedbackComment = in.readParcelable(Comment.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelableModelWrapper.writeModel(dest, flags, commentsOf);
        dest.writeParcelable(ownComment, flags);
        dest.writeParcelable(feedbackComment, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public AbsModel getCommentsOf() {
        return commentsOf;
    }

    public ReplyCommentFeedback setCommentsOf(AbsModel commentsOf) {
        this.commentsOf = commentsOf;
        return this;
    }

    public Comment getOwnComment() {
        return ownComment;
    }

    public ReplyCommentFeedback setOwnComment(Comment ownComment) {
        this.ownComment = ownComment;
        return this;
    }

    public Comment getFeedbackComment() {
        return feedbackComment;
    }

    public ReplyCommentFeedback setFeedbackComment(Comment feedbackComment) {
        this.feedbackComment = feedbackComment;
        return this;
    }
}