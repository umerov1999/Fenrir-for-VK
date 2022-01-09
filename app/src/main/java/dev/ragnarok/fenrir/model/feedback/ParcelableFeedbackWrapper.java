package dev.ragnarok.fenrir.model.feedback;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;


public final class ParcelableFeedbackWrapper implements Parcelable {

    public static final Creator<ParcelableFeedbackWrapper> CREATOR = new Creator<ParcelableFeedbackWrapper>() {
        @Override
        public ParcelableFeedbackWrapper createFromParcel(Parcel in) {
            return new ParcelableFeedbackWrapper(in);
        }

        @Override
        public ParcelableFeedbackWrapper[] newArray(int size) {
            return new ParcelableFeedbackWrapper[size];
        }
    };
    private static final List<Class<?>> TYPES = new ArrayList<>();

    static {
        TYPES.add(CommentFeedback.class);
        TYPES.add(CopyFeedback.class);
        TYPES.add(LikeCommentFeedback.class);
        TYPES.add(LikeFeedback.class);
        TYPES.add(MentionCommentFeedback.class);
        TYPES.add(MentionFeedback.class);
        TYPES.add(PostPublishFeedback.class);
        TYPES.add(ReplyCommentFeedback.class);
        TYPES.add(UsersFeedback.class);
    }

    private final Feedback feedback;

    public ParcelableFeedbackWrapper(Feedback feedback) {
        this.feedback = feedback;
    }

    private ParcelableFeedbackWrapper(Parcel in) {
        int index = in.readInt();
        ClassLoader classLoader = TYPES.get(index).getClassLoader();
        feedback = in.readParcelable(classLoader);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int index = TYPES.indexOf(feedback.getClass());
        if (index == -1) {
            throw new UnsupportedOperationException("Unsupported class: " + feedback.getClass());
        }

        dest.writeInt(index);
        dest.writeParcelable(feedback, flags);
    }

    public Feedback get() {
        return feedback;
    }
}