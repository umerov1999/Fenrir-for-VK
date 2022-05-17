package dev.ragnarok.fenrir.api.model.upload

import com.google.gson.annotations.SerializedName

class UploadStoryDto {
    @SerializedName("upload_result")
    var upload_result: String? = null
    override fun toString(): String {
        return "UploadStoryDto{" +
                "upload_result='" + upload_result + '\'' +
                '}'
    }
}