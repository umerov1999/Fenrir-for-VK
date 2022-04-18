package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class VKApiUploadServer implements Parcelable, UploadServer {

    public static final Creator<VKApiUploadServer> CREATOR = new Creator<VKApiUploadServer>() {
        @Override
        public VKApiUploadServer createFromParcel(Parcel in) {
            return new VKApiUploadServer(in);
        }

        @Override
        public VKApiUploadServer[] newArray(int size) {
            return new VKApiUploadServer[size];
        }
    };
    /**
     * адрес для загрузки фотографий
     */
    @Nullable
    public String upload_url;
    /**
     * идентификатор альбома, в который будет загружена фотография
     */
    public int album_id;
    /**
     * идентификатор пользователя, от чьего имени будет загружено фото
     */
    public int user_id;

    protected VKApiUploadServer(Parcel in) {
        upload_url = in.readString();
        album_id = in.readInt();
        user_id = in.readInt();
    }

    public VKApiUploadServer() {

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
        return "VKApiUploadServer{" +
                "upload_url='" + upload_url + '\'' +
                ", album_id=" + album_id +
                ", user_id=" + user_id +
                '}';
    }

    @Nullable
    @Override
    public String getUrl() {
        return upload_url;
    }
}
