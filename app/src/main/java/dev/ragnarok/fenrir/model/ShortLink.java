package dev.ragnarok.fenrir.model;

import android.os.Parcel;

public class ShortLink extends AbsModel {

    public static final Creator<ShortLink> CREATOR = new Creator<ShortLink>() {
        @Override
        public ShortLink createFromParcel(Parcel in) {
            return new ShortLink(in);
        }

        @Override
        public ShortLink[] newArray(int size) {
            return new ShortLink[size];
        }
    };

    private String short_url;
    private String url;
    private long timestamp;
    private String key;
    private String access_key;
    private int views;

    public ShortLink() {

    }

    protected ShortLink(Parcel in) {
        super(in);
        short_url = in.readString();
        url = in.readString();
        timestamp = in.readLong();
        key = in.readString();
        views = in.readInt();
        access_key = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(short_url);
        dest.writeString(url);
        dest.writeLong(timestamp);
        dest.writeString(key);
        dest.writeInt(views);
        dest.writeString(access_key);
    }

    public String getShort_url() {
        return short_url;
    }

    public ShortLink setShort_url(String short_url) {
        this.short_url = short_url;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ShortLink setUrl(String url) {
        this.url = url;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ShortLink setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getKey() {
        return key;
    }

    public ShortLink setKey(String key) {
        this.key = key;
        return this;
    }

    public String getAccess_key() {
        return access_key;
    }

    public ShortLink setAccess_key(String access_key) {
        this.access_key = access_key;
        return this;
    }

    public int getViews() {
        return views;
    }

    public ShortLink setViews(int views) {
        this.views = views;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
