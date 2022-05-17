package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.orZero

class VKApiStickerSet : Identificable {
    @SerializedName("background")
    var background: String? = null

    @SerializedName("description")
    var description: String? = null

    @SerializedName("author")
    var author: String? = null

    @SerializedName("free")
    var free = false

    @SerializedName("can_purchase")
    var can_purchase = false

    @SerializedName("payment_type")
    var payment_type: String? = null

    @SerializedName("product")
    var product: Product? = null
    override fun getObjectId(): Int {
        return product?.id.orZero()
    }

    class Image {
        @SerializedName("url")
        var url: String? = null

        @SerializedName("width")
        var width = 0

        @SerializedName("height")
        var height = 0
    }

    class Product {
        @SerializedName("id")
        var id = 0

        @SerializedName("purchased")
        var purchased = false

        @SerializedName("title")
        var title: String? = null

        @SerializedName("promoted")
        var promoted = false

        @SerializedName("active")
        var active = false

        @SerializedName("type")
        var type: String? = null

        @SerializedName("icon")
        var icon: List<Image>? = null

        @SerializedName("stickers")
        var stickers: List<VKApiSticker>? = null
    }
}