package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Атрибуты сервера для аплоада фотографий в личное сообщение
 */
public class VKApiPhotoMessageServer implements Parcelable, UploadServer {

    public static final Creator<VKApiPhotoMessageServer> CREATOR = new Creator<VKApiPhotoMessageServer>() {
        @Override
        public VKApiPhotoMessageServer createFromParcel(Parcel in) {
            return new VKApiPhotoMessageServer(in);
        }

        @Override
        public VKApiPhotoMessageServer[] newArray(int size) {
            return new VKApiPhotoMessageServer[size];
        }
    };
    /**
     * Адрес сервера
     */
    @Nullable
    @SerializedName("upload_url")
    public String upload_url;
    /**
     * id альбома
     */
    @SerializedName("album_id")
    public int album_id;
    /**
     * id текущего пользователя
     */
    @SerializedName("user_id")
    public int user_id;

    public VKApiPhotoMessageServer() {
    }

    public VKApiPhotoMessageServer(Parcel in) {
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

    @NonNull
    @Override
    public String toString() {
        return "VKApiPhotoMessageServer{" +
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
