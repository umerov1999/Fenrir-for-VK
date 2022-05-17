package dev.ragnarok.fenrir.api.model.upload

import com.google.gson.annotations.SerializedName

class UploadAudioDto {
    @SerializedName("server")
    var server: String? = null

    @SerializedName("audio")
    var audio: String? = null

    @SerializedName("hash")
    var hash: String? = null
    override fun toString(): String {
        return "UploadAudioDto{" +
                "server='" + server + '\'' +
                ", audio='" + audio + '\'' +
                ", hash='" + hash + '\'' +
                '}'
    }
}