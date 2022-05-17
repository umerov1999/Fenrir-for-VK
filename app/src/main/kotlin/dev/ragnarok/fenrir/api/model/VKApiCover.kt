package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiCover {
    @SerializedName("enabled")
    var enabled = false

    @SerializedName("images")
    var images: List<Image>? = null

    class Image {
        @SerializedName("url")
        var url: String? = null

        @SerializedName("width")
        var width = 0

        @SerializedName("height")
        var height = 0
    }
}