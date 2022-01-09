package dev.ragnarok.fenrir.model;

import android.os.Parcel;

import dev.ragnarok.fenrir.module.parcel.ParcelNative;

public class Story extends AbsModel implements ParcelNative.ParcelableNative {
    public static final Creator<Story> CREATOR = new Creator<Story>() {
        @Override
        public Story createFromParcel(Parcel in) {
            return new Story(in);
        }

        @Override
        public Story[] newArray(int size) {
            return new Story[size];
        }
    };
    public static final ParcelNative.Creator<Story> NativeCreator = Story::new;
    private int id;
    private int owner_id;
    private long date;
    private long expires_at;
    private boolean is_expired;
    private String access_key;
    private String target_url;
    private Photo photo;
    private Video video;
    private Owner author;

    public Story() {

    }

    protected Story(Parcel in) {
        super(in);
        id = in.readInt();
        owner_id = in.readInt();
        date = in.readLong();
        expires_at = in.readLong();
        is_expired = in.readInt() != 0;
        access_key = in.readString();
        target_url = in.readString();
        video = in.readParcelable(Video.class.getClassLoader());
        photo = in.readParcelable(Photo.class.getClassLoader());
        author = ParcelableOwnerWrapper.readOwner(in);
    }

    protected Story(ParcelNative in) {
        id = in.readInt();
        owner_id = in.readInt();
        date = in.readLong();
        expires_at = in.readLong();
        is_expired = in.readInt() != 0;
        access_key = in.readString();
        target_url = in.readString();
        video = in.readParcelable(Video.NativeCreator);
        photo = in.readParcelable(Photo.NativeCreator);
        author = ParcelableOwnerWrapper.readOwner(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(owner_id);
        dest.writeLong(date);
        dest.writeLong(expires_at);
        dest.writeInt(is_expired ? 1 : 0);
        dest.writeString(access_key);
        dest.writeString(target_url);
        dest.writeParcelable(video, flags);
        dest.writeParcelable(photo, flags);
        ParcelableOwnerWrapper.writeOwner(dest, flags, author);
    }

    @Override
    public void writeToParcelNative(ParcelNative dest) {
        dest.writeInt(id);
        dest.writeInt(owner_id);
        dest.writeLong(date);
        dest.writeLong(expires_at);
        dest.writeInt(is_expired ? 1 : 0);
        dest.writeString(access_key);
        dest.writeString(target_url);
        dest.writeParcelable(video);
        dest.writeParcelable(photo);
        ParcelableOwnerWrapper.writeOwner(dest, author);
    }

    public Photo getPhoto() {
        return photo;
    }

    public Story setPhoto(Photo photo) {
        this.photo = photo;
        return this;
    }

    public int getId() {
        return id;
    }

    public Story setId(int id) {
        this.id = id;
        return this;
    }

    public Video getVideo() {
        return video;
    }

    public Story setVideo(Video video) {
        this.video = video;
        return this;
    }

    public int getOwnerId() {
        return owner_id;
    }

    public Story setOwnerId(int ownerId) {
        owner_id = ownerId;
        return this;
    }

    public long getDate() {
        return date;
    }

    public Story setDate(long date) {
        this.date = date;
        return this;
    }

    public long getExpires() {
        return expires_at;
    }

    public Story setExpires(long expires_at) {
        this.expires_at = expires_at;
        return this;
    }

    public boolean isIs_expired() {
        return is_expired;
    }

    public Story setIs_expired(boolean is_expired) {
        this.is_expired = is_expired;
        return this;
    }

    public String getAccessKey() {
        return access_key;
    }

    public Story setAccessKey(String access_key) {
        this.access_key = access_key;
        return this;
    }

    public Owner getOwner() {
        return author;
    }

    public Story setOwner(Owner author) {
        this.author = author;
        return this;
    }

    public String getTarget_url() {
        return target_url;
    }

    public Story setTarget_url(String target_url) {
        this.target_url = target_url;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Story))
            return false;

        Story story = (Story) o;
        return id == story.id && owner_id == story.owner_id;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + owner_id;
        return result;
    }
}
