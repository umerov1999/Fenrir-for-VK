package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiMarketAlbum : VKApiAttachment {
    @SerializedName("id")
    var id = 0

    @SerializedName("owner_id")
    var owner_id = 0

    @SerializedName("access_key")
    var access_key: String? = null

    @SerializedName("title")
    var title: String? = null

    @SerializedName("photo")
    var photo: VKApiPhoto? = null

    @SerializedName("count")
    var count = 0

    @SerializedName("updated_time")
    var updated_time: Long = 0
    override fun getType(): String {
        return VKApiAttachment.TYPE_MARKET_ALBUM
    }
}