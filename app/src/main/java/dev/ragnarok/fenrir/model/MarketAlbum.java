package dev.ragnarok.fenrir.model;

import android.os.Parcel;

public class MarketAlbum extends AbsModel {

    public static final Creator<MarketAlbum> CREATOR = new Creator<MarketAlbum>() {
        @Override
        public MarketAlbum createFromParcel(Parcel in) {
            return new MarketAlbum(in);
        }

        @Override
        public MarketAlbum[] newArray(int size) {
            return new MarketAlbum[size];
        }
    };

    private final int id;
    private final int owner_id;
    private String access_key;
    private String title;
    private Photo photo;
    private int count;
    private int updated_time;

    public MarketAlbum(int id, int owner_id) {
        this.id = id;
        this.owner_id = owner_id;
    }

    protected MarketAlbum(Parcel in) {
        super(in);
        id = in.readInt();
        owner_id = in.readInt();
        access_key = in.readString();
        count = in.readInt();
        updated_time = in.readInt();
        title = in.readString();
        photo = in.readParcelable(Photo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(owner_id);
        dest.writeString(access_key);
        dest.writeInt(count);
        dest.writeInt(updated_time);
        dest.writeString(title);
        dest.writeParcelable(photo, flags);
    }

    public int getId() {
        return id;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public String getAccess_key() {
        return access_key;
    }

    public MarketAlbum setAccess_key(String access_key) {
        this.access_key = access_key;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MarketAlbum setTitle(String title) {
        this.title = title;
        return this;
    }

    public Photo getPhoto() {
        return photo;
    }

    public MarketAlbum setPhoto(Photo photo) {
        this.photo = photo;
        return this;
    }

    public int getCount() {
        return count;
    }

    public MarketAlbum setCount(int count) {
        this.count = count;
        return this;
    }

    public int getUpdated_time() {
        return updated_time;
    }

    public MarketAlbum setUpdated_time(int updated_time) {
        this.updated_time = updated_time;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
