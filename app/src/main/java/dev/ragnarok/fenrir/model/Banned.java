package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class Banned implements Parcelable {

    public static final Creator<Banned> CREATOR = new Creator<Banned>() {
        @Override
        public Banned createFromParcel(Parcel in) {
            return new Banned(in);
        }

        @Override
        public Banned[] newArray(int size) {
            return new Banned[size];
        }
    };
    private final Owner banned;
    private final User admin;
    private final Info info;

    public Banned(@NonNull Owner banned, @NonNull User admin, @NonNull Info info) {
        this.banned = banned;
        this.admin = admin;
        this.info = info;
    }

    private Banned(Parcel in) {
        ParcelableOwnerWrapper wrapper = in.readParcelable(ParcelableOwnerWrapper.class.getClassLoader());
        banned = wrapper.get();
        admin = in.readParcelable(User.class.getClassLoader());
        info = in.readParcelable(Info.class.getClassLoader());
    }

    public Owner getBanned() {
        return banned;
    }

    public User getAdmin() {
        return admin;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(new ParcelableOwnerWrapper(banned), flags);
        dest.writeParcelable(admin, flags);
        dest.writeParcelable(info, flags);
    }

    public Info getInfo() {
        return info;
    }

    public static final class Info implements Parcelable {

        public static final Creator<Info> CREATOR = new Creator<Info>() {
            @Override
            public Info createFromParcel(Parcel in) {
                return new Info(in);
            }

            @Override
            public Info[] newArray(int size) {
                return new Info[size];
            }
        };
        private long date;
        private int reason;
        private String comment;
        private long endDate;
        private boolean commentVisible;

        private Info(Parcel in) {
            date = in.readLong();
            reason = in.readInt();
            comment = in.readString();
            endDate = in.readLong();
            commentVisible = in.readByte() != 0;
        }

        public Info() {

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeLong(date);
            parcel.writeInt(reason);
            parcel.writeString(comment);
            parcel.writeLong(endDate);
            parcel.writeByte((byte) (commentVisible ? 1 : 0));
        }

        public long getDate() {
            return date;
        }

        public Info setDate(long date) {
            this.date = date;
            return this;
        }

        public int getReason() {
            return reason;
        }

        public Info setReason(int reason) {
            this.reason = reason;
            return this;
        }

        public String getComment() {
            return comment;
        }

        public Info setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public long getEndDate() {
            return endDate;
        }

        public Info setEndDate(long endDate) {
            this.endDate = endDate;
            return this;
        }

        public boolean isCommentVisible() {
            return commentVisible;
        }

        public Info setCommentVisible(boolean commentVisible) {
            this.commentVisible = commentVisible;
            return this;
        }
    }
}