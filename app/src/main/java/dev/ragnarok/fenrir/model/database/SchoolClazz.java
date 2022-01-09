package dev.ragnarok.fenrir.model.database;

import android.os.Parcel;
import android.os.Parcelable;

public final class SchoolClazz implements Parcelable {

    public static final Creator<SchoolClazz> CREATOR = new Creator<SchoolClazz>() {
        @Override
        public SchoolClazz createFromParcel(Parcel in) {
            return new SchoolClazz(in);
        }

        @Override
        public SchoolClazz[] newArray(int size) {
            return new SchoolClazz[size];
        }
    };
    private final int id;
    private final String title;

    public SchoolClazz(int id, String title) {
        this.id = id;
        this.title = title;
    }

    private SchoolClazz(Parcel in) {
        id = in.readInt();
        title = in.readString();
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
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