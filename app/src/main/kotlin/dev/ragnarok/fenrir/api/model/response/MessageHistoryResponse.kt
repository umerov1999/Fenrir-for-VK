package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiConversation
import dev.ragnarok.fenrir.api.model.VKApiMessage
import dev.ragnarok.fenrir.api.model.VKApiUser

class MessageHistoryResponse {
    @SerializedName("items")
    var messages: ArrayList<VKApiMessage>? = null

    @SerializedName("count")
    var count = 0

    @SerializedName("unread")
    var unread = 0

    @SerializedName("conversations")
    var conversations: List<VKApiConversation>? = null

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null

    @SerializedName("groups")
    var groups: List<VKApiCommunity>? = null
}