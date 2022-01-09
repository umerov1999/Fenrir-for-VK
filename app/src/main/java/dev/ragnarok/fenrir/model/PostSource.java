package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;


public final class PostSource implements Parcelable {

    public static final Creator<PostSource> CREATOR = new Creator<PostSource>() {
        @Override
        public PostSource createFromParcel(Parcel in) {
            return new PostSource(in);
        }

        @Override
        public PostSource[] newArray(int size) {
            return new PostSource[size];
        }
    };
    private final int type;
    private final String platform;
    private final int data;
    private final String url;

    public PostSource(int type, String platform, int data, String url) {
        this.type = type;
        this.platform = platform;
        this.data = data;
        this.url = url;
    }

    private PostSource(Parcel in) {
        type = in.readInt();
        platform = in.readString();
        data = in.readInt();
        url = in.readString();
    }

    public String getUrl() {
        return url;
    }

    public String getPlatform() {
        return platform;
    }

    public int getData() {
        return data;
    }

    public int getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(type);
        parcel.writeString(platform);
        parcel.writeInt(data);
        parcel.writeString(url);
    }
}