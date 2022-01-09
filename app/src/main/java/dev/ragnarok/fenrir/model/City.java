package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;


public final class City implements Parcelable {

    public static final Creator<City> CREATOR = new Creator<City>() {
        @Override
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };
    private final int id;
    private final String title;
    private boolean important;
    private String area;
    private String region;

    public City(int id, String title) {
        this.id = id;
        this.title = title;
    }

    private City(Parcel in) {
        id = in.readInt();
        title = in.readString();
        important = in.readByte() != 0;
        area = in.readString();
        region = in.readString();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isImportant() {
        return important;
    }

    public City setImportant(boolean important) {
        this.important = important;
        return this;
    }

    public String getArea() {
        return area;
    }

    public City setArea(String area) {
        this.area = area;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public City setRegion(String region) {
        this.region = region;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeByte((byte) (important ? 1 : 0));
        parcel.writeString(area);
        parcel.writeString(region);
    }
}