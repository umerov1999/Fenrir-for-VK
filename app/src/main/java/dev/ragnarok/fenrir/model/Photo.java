package dev.ragnarok.fenrir.model;

import android.os.Parcel;

import androidx.annotation.Keep;

import dev.ragnarok.fenrir.module.parcel.ParcelNative;
import dev.ragnarok.fenrir.util.Objects;


@Keep
public class Photo extends AbsModel implements ISomeones, ParcelNative.ParcelableNative {

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public static final ParcelNative.Creator<Photo> NativeCreator = Photo::new;
    private int id;
    private int ownerId;
    private int albumId;
    private int width;
    private int height;
    private PhotoSizes sizes;
    private String text;
    private long date;
    private boolean userLikes;
    private boolean canComment;
    private int likesCount;
    private int repostsCount;
    private int commentsCount;
    private int tagsCount;
    private String accessKey;
    private boolean deleted;
    private int postId;
    private int msgId;
    private int msgPeerId;

    public Photo() {

    }

    protected Photo(ParcelNative in) {
        id = in.readInt();
        ownerId = in.readInt();
        albumId = in.readInt();
        width = in.readInt();
        height = in.readInt();
        sizes = in.readParcelable(PhotoSizes.NativeCreator);
        text = in.readString();
        date = in.readLong();
        userLikes = in.readBoolean();
        canComment = in.readBoolean();
        likesCount = in.readInt();
        commentsCount = in.readInt();
        tagsCount = in.readInt();
        accessKey = in.readString();
        deleted = in.readBoolean();
        postId = in.readInt();
        repostsCount = in.readInt();
        msgId = in.readInt();
        msgPeerId = in.readInt();
    }

    protected Photo(Parcel in) {
        super(in);
        id = in.readInt();
        ownerId = in.readInt();
        albumId = in.readInt();
        width = in.readInt();
        height = in.readInt();
        sizes = in.readParcelable(PhotoSizes.class.getClassLoader());
        text = in.readString();
        date = in.readLong();
        userLikes = in.readByte() != 0;
        canComment = in.readByte() != 0;
        likesCount = in.readInt();
        commentsCount = in.readInt();
        tagsCount = in.readInt();
        accessKey = in.readString();
        deleted = in.readByte() != 0;
        postId = in.readInt();
        repostsCount = in.readInt();
        msgId = in.readInt();
        msgPeerId = in.readInt();
    }

    public int getId() {
        return id;
    }

    public Photo setId(int id) {
        this.id = id;
        return this;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public Photo setOwnerId(int ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public int getAlbumId() {
        return albumId;
    }

    public Photo setAlbumId(int albumId) {
        this.albumId = albumId;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public Photo setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public Photo setHeight(int height) {
        this.height = height;
        return this;
    }

    public PhotoSizes getSizes() {
        return sizes;
    }

    public Photo setSizes(PhotoSizes sizes) {
        this.sizes = sizes;
        return this;
    }

    public String getText() {
        return text;
    }

    public Photo setText(String text) {
        this.text = text;
        return this;
    }

    public long getDate() {
        return date;
    }

    public Photo setDate(long date) {
        this.date = date;
        return this;
    }

    public boolean isUserLikes() {
        return userLikes;
    }

    public Photo setUserLikes(boolean userLikes) {
        this.userLikes = userLikes;
        return this;
    }

    public boolean isCanComment() {
        return canComment;
    }

    public Photo setCanComment(boolean canComment) {
        this.canComment = canComment;
        return this;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public Photo setLikesCount(int likesCount) {
        this.likesCount = likesCount;
        return this;
    }

    public int getRepostsCount() {
        return repostsCount;
    }

    public Photo setRepostsCount(int repostsCount) {
        this.repostsCount = repostsCount;
        return this;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public Photo setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
        return this;
    }

    public int getTagsCount() {
        return tagsCount;
    }

    public Photo setTagsCount(int tagsCount) {
        this.tagsCount = tagsCount;
        return this;
    }

    public String getUrlForSize(@PhotoSize int size, boolean excludeNonAspectRatio) {
        return Objects.isNull(sizes) ? null : sizes.getUrlForSize(size, excludeNonAspectRatio);
    }

    public String getAccessKey() {
        return accessKey;
    }

    public Photo setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Photo setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public int getPostId() {
        return postId;
    }

    public Photo setPostId(int postId) {
        this.postId = postId;
        return this;
    }

    public int getMsgId() {
        return msgId;
    }

    public Photo setMsgId(int msgId) {
        this.msgId = msgId;
        return this;
    }

    public int getMsgPeerId() {
        return msgPeerId;
    }

    public Photo setMsgPeerId(int msgPeerId) {
        this.msgPeerId = msgPeerId;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(id);
        parcel.writeInt(ownerId);
        parcel.writeInt(albumId);
        parcel.writeInt(width);
        parcel.writeInt(height);
        parcel.writeParcelable(sizes, i);
        parcel.writeString(text);
        parcel.writeLong(date);
        parcel.writeByte((byte) (userLikes ? 1 : 0));
        parcel.writeByte((byte) (canComment ? 1 : 0));
        parcel.writeInt(likesCount);
        parcel.writeInt(commentsCount);
        parcel.writeInt(tagsCount);
        parcel.writeString(accessKey);
        parcel.writeByte((byte) (deleted ? 1 : 0));
        parcel.writeInt(postId);
        parcel.writeInt(repostsCount);
        parcel.writeInt(msgId);
        parcel.writeInt(msgPeerId);
    }

    public String generateWebLink() {
        return String.format("vk.com/photo%s_%s", ownerId, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Photo photo = (Photo) o;
        return id == photo.id && ownerId == photo.ownerId;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + ownerId;
        return result;
    }

    @Override
    public void writeToParcelNative(ParcelNative parcel) {
        parcel.writeInt(id);
        parcel.writeInt(ownerId);
        parcel.writeInt(albumId);
        parcel.writeInt(width);
        parcel.writeInt(height);
        parcel.writeParcelable(sizes);
        parcel.writeString(text);
        parcel.writeLong(date);
        parcel.writeBoolean(userLikes);
        parcel.writeBoolean(canComment);
        parcel.writeInt(likesCount);
        parcel.writeInt(commentsCount);
        parcel.writeInt(tagsCount);
        parcel.writeString(accessKey);
        parcel.writeBoolean(deleted);
        parcel.writeInt(postId);
        parcel.writeInt(repostsCount);
        parcel.writeInt(msgId);
        parcel.writeInt(msgPeerId);
    }
}
