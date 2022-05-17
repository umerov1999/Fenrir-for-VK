package dev.ragnarok.fenrir.api.model.longpoll

import com.google.gson.annotations.SerializedName

class VkApiGroupLongpollUpdates {
    @SerializedName("failed")
    var failed = 0

    @SerializedName("ts")
    var ts: String? = null
    val count: Int
        get() = 0
}