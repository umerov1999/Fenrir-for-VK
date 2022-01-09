package dev.ragnarok.fenrir.model.database;

import android.os.Parcel;
import android.os.Parcelable;


public final class Chair implements Parcelable {

    public static final Creator<Chair> CREATOR = new Creator<Chair>() {
        @Override
        public Chair createFromParcel(Parcel in) {
            return new Chair(in);
        }

        @Override
        public Chair[] newArray(int size) {
            return new Chair[size];
        }
    };
    private final int id;
    private final String title;

    public Chair(int id, String title) {
        this.id = id;
        this.title = title;
    }

    private Chair(Parcel in) {
        id = in.readInt();
        title = in.readString();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
    }
}