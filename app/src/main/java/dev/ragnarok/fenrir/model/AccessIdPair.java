package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AccessIdPair implements Parcelable {

    public static final Creator<AccessIdPair> CREATOR = new Creator<AccessIdPair>() {
        @Override
        public AccessIdPair createFromParcel(Parcel in) {
            return new AccessIdPair(in);
        }

        @Override
        public AccessIdPair[] newArray(int size) {
            return new AccessIdPair[size];
        }
    };
    private final int id;
    private final int ownerId;
    private final String accessKey;

    public AccessIdPair(int id, int ownerId, String accessKey) {
        this.id = id;
        this.ownerId = ownerId;
        this.accessKey = accessKey;
    }

    protected AccessIdPair(Parcel in) {
        id = in.readInt();
        ownerId = in.readInt();
        accessKey = in.readString();
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(ownerId);
        parcel.writeString(accessKey);
    }
}
