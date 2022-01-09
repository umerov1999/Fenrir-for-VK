package dev.ragnarok.fenrir.model;

import android.os.Parcel;


public class VideoAlbum extends AbsModel {

    public static final Creator<VideoAlbum> CREATOR = new Creator<VideoAlbum>() {
        @Override
        public VideoAlbum createFromParcel(Parcel in) {
            return new VideoAlbum(in);
        }

        @Override
        public VideoAlbum[] newArray(int size) {
            return new VideoAlbum[size];
        }
    };
    private final int id;
    private final int ownerId;
    private String title;
    private int count;
    private long updatedTime;
    private String image;
    private SimplePrivacy privacy;

    public VideoAlbum(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    protected VideoAlbum(Parcel in) {
        super(in);
        id = in.readInt();
        ownerId = in.readInt();
        title = in.readString();
        count = in.readInt();
        updatedTime = in.readLong();
        image = in.readString();
        privacy = in.readParcelable(SimplePrivacy.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(ownerId);
        dest.writeString(title);
        dest.writeInt(count);
        dest.writeLong(updatedTime);
        dest.writeString(image);
        dest.writeParcelable(privacy, flags);
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public VideoAlbum setTitle(String title) {
        this.title = title;
        return this;
    }

    public SimplePrivacy getPrivacy() {
        return privacy;
    }

    public VideoAlbum setPrivacy(SimplePrivacy privacy) {
        this.privacy = privacy;
        return this;
    }

    public int getCount() {
        return count;
    }

    public VideoAlbum setCount(int count) {
        this.count = count;
        return this;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public VideoAlbum setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
        return this;
    }

    public String getImage() {
        return image;
    }

    public VideoAlbum setImage(String image) {
        this.image = image;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
