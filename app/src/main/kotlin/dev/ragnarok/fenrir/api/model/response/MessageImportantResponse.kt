package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiConversation
import dev.ragnarok.fenrir.api.model.VKApiMessage
import dev.ragnarok.fenrir.api.model.VKApiUser

class MessageImportantResponse {
    @SerializedName("messages")
    var messages: Message? = null

    @SerializedName("conversations")
    var conversations: List<VKApiConversation>? = null

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null

    @SerializedName("groups")
    var groups: List<VKApiCommunity>? = null

    class Message {
        @SerializedName("items")
        var items: ArrayList<VKApiMessage>? = null

        @SerializedName("count")
        var count = 0
    }
}