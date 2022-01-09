package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LocalImageAlbum implements Parcelable {

    public static final Creator<LocalImageAlbum> CREATOR = new Creator<LocalImageAlbum>() {
        @Override
        public LocalImageAlbum createFromParcel(Parcel in) {
            return new LocalImageAlbum(in);
        }

        @Override
        public LocalImageAlbum[] newArray(int size) {
            return new LocalImageAlbum[size];
        }
    };
    private int id;
    private String name;
    private long coverImageId;
    private int photoCount;

    public LocalImageAlbum() {

    }

    protected LocalImageAlbum(Parcel in) {
        id = in.readInt();
        name = in.readString();
        coverImageId = in.readLong();
        photoCount = in.readInt();
    }

    public int getId() {
        return id;
    }

    public LocalImageAlbum setId(int id) {
        this.id = id;
        return this;
    }

    public long getCoverImageId() {
        return coverImageId;
    }

    public LocalImageAlbum setCoverId(long coverImageId) {
        this.coverImageId = coverImageId;
        return this;
    }

    public String getName() {
        return name;
    }

    public LocalImageAlbum setName(String name) {
        this.name = name;
        return this;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public LocalImageAlbum setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeLong(coverImageId);
        dest.writeInt(photoCount);
    }
}