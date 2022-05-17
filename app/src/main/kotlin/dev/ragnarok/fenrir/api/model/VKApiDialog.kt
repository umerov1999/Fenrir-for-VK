package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiDialog {
    @SerializedName("conversation")
    var conversation: VKApiConversation? = null

    @SerializedName("last_message")
    var lastMessage: VKApiMessage? = null
}