package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiGraffiti : VKApiAttachment {
    @SerializedName("id")
    var id = 0

    @SerializedName("owner_id")
    var owner_id = 0

    @SerializedName("url")
    var url: String? = null

    @SerializedName("width")
    var width = 0

    @SerializedName("height")
    var height = 0

    @SerializedName("access_key")
    var access_key: String? = null
    override fun getType(): String {
        return VKApiAttachment.TYPE_GRAFFITI
    }
}