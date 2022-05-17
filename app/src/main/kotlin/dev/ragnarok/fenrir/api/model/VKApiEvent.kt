package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiEvent : VKApiAttachment {
    @SerializedName("id")
    var id = 0

    @SerializedName("button_text")
    var button_text: String? = null

    @SerializedName("text")
    var text: String? = null
    override fun getType(): String {
        return VKApiAttachment.TYPE_EVENT
    }
}