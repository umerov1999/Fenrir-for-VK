package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiShortLink {
    @SerializedName("short_url")
    var short_url: String? = null

    @SerializedName("url")
    var url: String? = null

    @SerializedName("timestamp")
    var timestamp: Long = 0

    @SerializedName("access_key")
    var access_key: String? = null

    @SerializedName("key")
    var key: String? = null

    @SerializedName("views")
    var views = 0
}