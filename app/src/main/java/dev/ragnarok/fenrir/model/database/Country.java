package dev.ragnarok.fenrir.model.database;

import android.os.Parcel;
import android.os.Parcelable;


public final class Country implements Parcelable {

    public static final Creator<Country> CREATOR = new Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel in) {
            return new Country(in);
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };
    private final int id;
    private final String title;

    public Country(int id, String title) {
        this.id = id;
        this.title = title;
    }

    private Country(Parcel in) {
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