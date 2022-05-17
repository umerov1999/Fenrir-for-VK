package dev.ragnarok.fenrir.api.model.upload

import com.google.gson.annotations.SerializedName

class UploadChatPhotoDto {
    @SerializedName("response")
    var response: String? = null
    override fun toString(): String {
        return "UploadChatPhotoDto{" +
                "response='" + response + '\'' +
                '}'
    }
}