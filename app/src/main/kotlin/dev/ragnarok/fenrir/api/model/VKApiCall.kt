package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiCall : VKApiAttachment {
    @SerializedName("initiator_id")
    var initiator_id = 0

    @SerializedName("receiver_id")
    var receiver_id = 0

    @SerializedName("state")
    var state: String? = null

    @SerializedName("time")
    var time: Long = 0
    override fun getType(): String {
        return VKApiAttachment.TYPE_CALL
    }
}