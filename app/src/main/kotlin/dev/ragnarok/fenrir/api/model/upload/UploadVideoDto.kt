package dev.ragnarok.fenrir.api.model.upload

import com.google.gson.annotations.SerializedName

class UploadVideoDto {
    @SerializedName("owner_id")
    var owner_id = 0

    @SerializedName("video_id")
    var video_id = 0

    @SerializedName("video_hash")
    var video_hash: String? = null
    override fun toString(): String {
        return "UploadVideoDto{" +
                "owner_id=" + owner_id +
                ", video_id='" + video_id + '\'' +
                ", video_hash='" + video_hash + '\'' +
                '}'
    }
}