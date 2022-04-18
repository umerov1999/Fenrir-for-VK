package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class VKApiWallUploadServer implements Parcelable, UploadServer {

    public static final Creator<VKApiWallUploadServer> CREATOR = new Creator<VKApiWallUploadServer>() {
        @Override
        public VKApiWallUploadServer createFromParcel(Parcel in) {
            return new VKApiWallUploadServer(in);
        }

        @Override
        public VKApiWallUploadServer[] newArray(int size) {
            return new VKApiWallUploadServer[size];
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

    public VKApiWallUploadServer() {

    }

    protected VKApiWallUploadServer(Parcel in) {
        upload_url = in.readString();
        album_id = in.readInt();
        user_id = in.readInt();
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

    @Nullable
    @Override
    public String getUrl() {
        return upload_url;
    }
}
