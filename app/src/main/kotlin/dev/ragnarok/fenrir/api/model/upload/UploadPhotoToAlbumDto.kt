package dev.ragnarok.fenrir.api.model.upload

import com.google.gson.annotations.SerializedName

class UploadPhotoToAlbumDto {
    @SerializedName("server")
    var server = 0

    @SerializedName("photos_list")
    var photos_list: String? = null

    @SerializedName("aid")
    var aid = 0

    @SerializedName("hash")
    var hash: String? = null
    override fun toString(): String {
        return "UploadPhotoToAlbumDto{" +
                "server=" + server +
                ", photos_list='" + photos_list + '\'' +
                ", aid=" + aid +
                ", hash='" + hash + '\'' +
                '}'
    }
}