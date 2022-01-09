package dev.ragnarok.fenrir.model;

import android.os.Parcel;

public class FaveLink extends AbsModel {

    public static final Creator<FaveLink> CREATOR = new Creator<FaveLink>() {
        @Override
        public FaveLink createFromParcel(Parcel in) {
            return new FaveLink(in);
        }

        @Override
        public FaveLink[] newArray(int size) {
            return new FaveLink[size];
        }
    };
    private final String id;
    private String url;
    private String title;
    private String description;
    private Photo photo;

    public FaveLink(String id) {
        this.id = id;
    }

    protected FaveLink(Parcel in) {
        super(in);
        id = in.readString();
        url = in.readString();
        title = in.readString();
        description = in.readString();
        photo = in.readParcelable(Photo.class.getClassLoader());
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public FaveLink setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public FaveLink setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public FaveLink setDescription(String description) {
        this.description = description;
        return this;
    }

    public Photo getPhoto() {
        return photo;
    }

    public FaveLink setPhoto(Photo photo) {
        this.photo = photo;
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeParcelable(photo, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}