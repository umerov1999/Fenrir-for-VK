package dev.ragnarok.fenrir.api.model.server

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Атрибуты сервера для аплоада фотографий в личное сообщение
 */
class VKApiPhotoMessageServer : Parcelable, UploadServer {
    /**
     * Адрес сервера
     */
    @SerializedName("upload_url")
    override var url: String? = null

    /**
     * id альбома
     */
    @SerializedName("album_id")
    var album_id = 0

    /**
     * id текущего пользователя
     */
    @SerializedName("user_id")
    var user_id = 0

    @Suppress("UNUSED")
    constructor()
    private constructor(`in`: Parcel) {
        url = `in`.readString()
        album_id = `in`.readInt()
        user_id = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(url)
        dest.writeInt(album_id)
        dest.writeInt(user_id)
    }

    override fun toString(): String {
        return "VKApiPhotoMessageServer{" +
                "upload_url='" + url + '\'' +
                ", album_id=" + album_id +
                ", user_id=" + user_id +
                '}'
    }

    companion object CREATOR : Parcelable.Creator<VKApiPhotoMessageServer> {
        override fun createFromParcel(parcel: Parcel): VKApiPhotoMessageServer {
            return VKApiPhotoMessageServer(parcel)
        }

        override fun newArray(size: Int): Array<VKApiPhotoMessageServer?> {
            return arrayOfNulls(size)
        }
    }
}