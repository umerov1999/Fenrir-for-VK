package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class VkApiUploadServer implements Parcelable, UploadServer {

    public static final Creator<VkApiUploadServer> CREATOR = new Creator<VkApiUploadServer>() {
        @Override
        public VkApiUploadServer createFromParcel(Parcel in) {
            return new VkApiUploadServer(in);
        }

        @Override
        public VkApiUploadServer[] newArray(int size) {
            return new VkApiUploadServer[size];
        }
    };
    /**
     * адрес для загрузки фотографий
     */
    public String upload_url;
    /**
     * идентификатор альбома, в который будет загружена фотография
     */
    public int album_id;
    /**
     * идентификатор пользователя, от чьего имени будет загружено фото
     */
    public int user_id;

    protected VkApiUploadServer(Parcel in) {
        upload_url = in.readString();
        album_id = in.readInt();
        user_id = in.readInt();
    }

    public VkApiUploadServer() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(upload_url);
        dest.writeInt(album_id);
        dest.writeInt(user_id);
    }

    @NonNull
    @Override
    public String toString() {
        return "VkApiUploadServer{" +
                "upload_url='" + upload_url + '\'' +
                ", album_id=" + album_id +
                ", user_id=" + user_id +
                '}';
    }

    @Override
    public String getUrl() {
        return upload_url;
    }
}
