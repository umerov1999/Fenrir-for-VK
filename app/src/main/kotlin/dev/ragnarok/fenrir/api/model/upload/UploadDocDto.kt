package dev.ragnarok.fenrir.api.model.upload

import com.google.gson.annotations.SerializedName

class UploadDocDto {
    @SerializedName("file")
    var file: String? = null
    override fun toString(): String {
        return "UploadDocDto{" +
                "file='" + file + '\'' +
                '}'
    }
}