package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName

class GroupLongpollServer {
    @SerializedName("key")
    var key: String? = null

    @SerializedName("server")
    var server: String? = null

    @SerializedName("ts")
    var ts: String? = null
}