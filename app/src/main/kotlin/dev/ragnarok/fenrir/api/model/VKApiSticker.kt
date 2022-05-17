package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

/**
 * Describes a photo object from VK.
 */
class VKApiSticker : VKApiAttachment {
    /**
     * Sticker ID, positive number
     */
    var sticker_id = 0

    @SerializedName("images")
    var images: List<Image>? = null

    @SerializedName("images_with_background")
    var images_with_background: List<Image>? = null

    @SerializedName("animation_url")
    var animation_url: String? = null

    @SerializedName("animations")
    var animations: List<VKApiAnimation>? = null
    override fun getType(): String {
        return VKApiAttachment.TYPE_STICKER
    }

    class Image {
        @SerializedName("url")
        var url: String? = null

        @SerializedName("width")
        var width = 0

        @SerializedName("height")
        var height = 0
    }

    class VKApiAnimation {
        @SerializedName("type")
        var type: String? = null

        @SerializedName("url")
        var url: String? = null
    }
}