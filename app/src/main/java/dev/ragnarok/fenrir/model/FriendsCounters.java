package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;


public final class FriendsCounters implements Parcelable {

    public static final Creator<FriendsCounters> CREATOR = new Creator<FriendsCounters>() {
        @Override
        public FriendsCounters createFromParcel(Parcel in) {
            return new FriendsCounters(in);
        }

        @Override
        public FriendsCounters[] newArray(int size) {
            return new FriendsCounters[size];
        }
    };
    private final int all;
    private final int online;
    private final int followers;
    private final int mutual;

    public FriendsCounters(int all, int online, int followers, int mutual) {
        this.all = all;
        this.online = online;
        this.followers = followers;
        this.mutual = mutual;
    }

    private FriendsCounters(Parcel in) {
        all = in.readInt();
        online = in.readInt();
        followers = in.readInt();
        mutual = in.readInt();
    }

    public int getAll() {
        return all;
    }

    public int getFollowers() {
        return followers;
    }

    public int getMutual() {
        return mutual;
    }

    public int getOnline() {
        return online;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(all);
        parcel.writeInt(online);
        parcel.writeInt(followers);
        parcel.writeInt(mutual);
    }
}