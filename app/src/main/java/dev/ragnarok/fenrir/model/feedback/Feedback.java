package dev.ragnarok.fenrir.model.feedback;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.CallSuper;

import dev.ragnarok.fenrir.model.Comment;

public abstract class Feedback implements Parcelable {

    @FeedbackType
    private final int type;

    private long date;

    private Comment reply;

    public Feedback(@FeedbackType int type) {
        this.type = type;
    }

    protected Feedback(Parcel in) {
        type = in.readInt();
        date = in.readLong();
        reply = in.readParcelable(Comment.class.getClassLoader());
    }

    public final int getType() {
        return type;
    }

    public final long getDate() {
        return date;
    }

    public final Feedback setDate(long date) {
        this.date = date;
        return this;
    }

    public final Comment getReply() {
        return reply;
    }

    public final Feedback setReply(Comment reply) {
        this.reply = reply;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @CallSuper
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeLong(date);
        dest.writeParcelable(reply, flags);
    }
}