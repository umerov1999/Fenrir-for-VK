package dev.ragnarok.fenrir.upload;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class UploadDestination implements Parcelable {

    public static final int WITHOUT_OWNER = 0;
    public static final int NO_ID = 0;
    public static final Creator<UploadDestination> CREATOR = new Creator<UploadDestination>() {
        @Override
        public UploadDestination createFromParcel(Parcel in) {
            return new UploadDestination(in);
        }

        @Override
        public UploadDestination[] newArray(int size) {
            return new UploadDestination[size];
        }
    };
    private final int id;
    private final int ownerId;
    @Method
    private final int method;

    @MessageMethod
    private int message_method;

    public UploadDestination(int id, int ownerId, int method, int message_method) {
        this.id = id;
        this.ownerId = ownerId;
        this.method = method;
        this.message_method = message_method;
    }

    protected UploadDestination(Parcel in) {
        id = in.readInt();
        ownerId = in.readInt();
        method = in.readInt();
        message_method = in.readInt();
    }

    public static UploadDestination forProfilePhoto(int ownerId) {
        return new UploadDestination(NO_ID, ownerId, Method.PHOTO_TO_PROFILE, MessageMethod.NULL);
    }

    public static UploadDestination forChatPhoto(int chat_id) {
        return new UploadDestination(NO_ID, chat_id, Method.PHOTO_TO_CHAT, MessageMethod.NULL);
    }

    public static UploadDestination forDocuments(int ownerId) {
        return new UploadDestination(NO_ID, ownerId, Method.DOCUMENT, MessageMethod.NULL);
    }

    public static UploadDestination forAudio(int ownerId) {
        return new UploadDestination(NO_ID, ownerId, Method.AUDIO, MessageMethod.NULL);
    }

    public static UploadDestination forRemotePlay() {
        return new UploadDestination(NO_ID, NO_ID, Method.REMOTE_PLAY_AUDIO, MessageMethod.NULL);
    }

    public static UploadDestination forStory(@MessageMethod int msg_method) {
        return new UploadDestination(NO_ID, NO_ID, Method.STORY, msg_method);
    }

    public static UploadDestination forVideo(int is_public, int ownerId) {
        return new UploadDestination(is_public, ownerId, Method.VIDEO, MessageMethod.NULL);
    }

    public static UploadDestination forMessage(int mdbid) {
        return new UploadDestination(mdbid, WITHOUT_OWNER,
                Method.TO_MESSAGE, MessageMethod.PHOTO);
    }

    public static UploadDestination forMessage(int mdbid, @MessageMethod int msg_method) {
        return new UploadDestination(mdbid, WITHOUT_OWNER,
                Method.TO_MESSAGE, msg_method);
    }

    public static UploadDestination forPhotoAlbum(int albumId, int ownerId) {
        return new UploadDestination(albumId, ownerId, Method.PHOTO_TO_ALBUM, MessageMethod.NULL);
    }

    public static UploadDestination forPost(int dbid, int ownerId, @MessageMethod int msg_method) {
        return new UploadDestination(dbid, ownerId, Method.TO_WALL, msg_method);
    }

    public static UploadDestination forPost(int dbid, int ownerId) {
        return new UploadDestination(dbid, ownerId, Method.TO_WALL, MessageMethod.PHOTO);
    }

    public static UploadDestination forComment(int dbid, int sourceOwnerId) {
        return new UploadDestination(dbid, sourceOwnerId, Method.TO_COMMENT, MessageMethod.PHOTO);
    }

    public static UploadDestination forComment(int dbid, int sourceOwnerId, @MessageMethod int msg_method) {
        return new UploadDestination(dbid, sourceOwnerId, Method.TO_COMMENT, msg_method);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(ownerId);
        dest.writeInt(method);
        dest.writeInt(message_method);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadDestination that = (UploadDestination) o;
        return id == that.id && ownerId == that.ownerId && method == that.method;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + ownerId;
        result = 31 * result + method;
        return result;
    }

    public boolean compareTo(UploadDestination destination) {
        return compareTo(destination.id, destination.ownerId, destination.method);
    }

    public boolean compareTo(int id, int ownerId, int type) {
        return this.id == id && this.ownerId == ownerId && method == type;
    }

    @NonNull
    @Override
    public String toString() {
        return "UploadDestination{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", method=" + method +
                ", message_method=" + message_method +
                '}';
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getMethod() {
        return method;
    }

    public int getMessageMethod() {
        return message_method;
    }

    public void setMessageMethod(int message_method) {
        this.message_method = message_method;
    }
}
