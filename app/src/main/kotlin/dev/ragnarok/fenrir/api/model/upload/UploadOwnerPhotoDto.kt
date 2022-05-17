package dev.ragnarok.fenrir.api.model.upload

import com.google.gson.annotations.SerializedName

class UploadOwnerPhotoDto {
    @SerializedName("server")
    var server: String? = null

    @SerializedName("photo")
    var photo: String? = null

    @SerializedName("hash")
    var hash: String? = null
    override fun toString(): String {
        return "UploadOwnerPhotoDto{" +
                "server=" + server +
                ", photo='" + photo + '\'' +
                ", hash='" + hash + '\'' +
                '}'
    }
}