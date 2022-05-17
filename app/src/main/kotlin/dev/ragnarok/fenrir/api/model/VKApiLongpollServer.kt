package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiLongpollServer {
    @SerializedName("key")
    var key: String? = null

    @SerializedName("server")
    var server: String? = null

    @SerializedName("ts")
    var ts: Long = 0

    @SerializedName("pts")
    var pts: Long = 0
}