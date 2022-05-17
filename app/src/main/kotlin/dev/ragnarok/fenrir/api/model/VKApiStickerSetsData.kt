package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiStickerSetsData {
    @SerializedName("recent")
    var recent: Items<VKApiSticker>? = null

    @SerializedName("sticker_pack")
    var sticker_pack: Items<VKApiStickerSet.Product>? = null
}