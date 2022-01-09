package dev.ragnarok.fenrir.model;

import android.os.Parcel;

public class Graffiti extends AbsModel {

    public static final Creator<Graffiti> CREATOR = new Creator<Graffiti>() {
        @Override
        public Graffiti createFromParcel(Parcel in) {
            return new Graffiti(in);
        }

        @Override
        public Graffiti[] newArray(int size) {
            return new Graffiti[size];
        }
    };
    private int id;
    private int owner_id;
    private String url;
    private int width;
    private int height;
    private String access_key;

    public Graffiti() {

    }

    protected Graffiti(Parcel in) {
        super(in);
        id = in.readInt();
        owner_id = in.readInt();
        url = in.readString();
        width = in.readInt();
        height = in.readInt();
        access_key = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(owner_id);
        dest.writeString(url);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(access_key);
    }

    public int getId() {
        return id;
    }

    public Graffiti setId(int id) {
        this.id = id;
        return this;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public Graffiti setOwner_id(int owner_id) {
        this.owner_id = owner_id;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Graffiti setUrl(String url) {
        this.url = url;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public Graffiti setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public Graffiti setHeight(int height) {
        this.height = height;
        return this;
    }

    public String getAccess_key() {
        return access_key;
    }

    public Graffiti setAccess_key(String access_key) {
        this.access_key = access_key;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Graffiti))
            return false;

        Graffiti graffiti = (Graffiti) o;
        return id == graffiti.id && owner_id == graffiti.owner_id;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + owner_id;
        return result;
    }
}
