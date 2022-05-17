package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiAudioArtist : VKApiAttachment {
    @SerializedName("id")
    var id: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("photo")
    var photo: List<Image>? = null
    override fun getType(): String {
        return VKApiAttachment.TYPE_ARTIST
    }

    class Image {
        @SerializedName("url")
        var url: String? = null

        @SerializedName("width")
        var width = 0

        @SerializedName("height")
        var height = 0
    }
}