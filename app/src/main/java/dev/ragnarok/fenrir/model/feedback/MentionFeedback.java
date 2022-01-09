package dev.ragnarok.fenrir.model.feedback;

import android.os.Parcel;

import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.ParcelableModelWrapper;

/**
 * Base class for types [mention, mention_comment_photo, mention_comment_video]
 * where - в каком обьекте было упоминание
 */
public final class MentionFeedback extends Feedback {

    public static final Creator<MentionFeedback> CREATOR = new Creator<MentionFeedback>() {
        @Override
        public MentionFeedback createFromParcel(Parcel in) {
            return new MentionFeedback(in);
        }

        @Override
        public MentionFeedback[] newArray(int size) {
            return new MentionFeedback[size];
        }
    };
    private AbsModel where;

    // one of FeedbackType.MENTION
    public MentionFeedback(@FeedbackType int type) {
        super(type);
    }

    private MentionFeedback(Parcel in) {
        super(in);
        where = ParcelableModelWrapper.readModel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelableModelWrapper.writeModel(dest, flags, where);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public AbsModel getWhere() {
        return where;
    }

    public MentionFeedback setWhere(AbsModel where) {
        this.where = where;
        return this;
    }
}