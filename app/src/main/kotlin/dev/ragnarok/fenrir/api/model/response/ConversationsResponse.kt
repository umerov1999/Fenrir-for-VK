package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiConversation
import dev.ragnarok.fenrir.api.model.VKApiUser

class ConversationsResponse {
    @SerializedName("items")
    var conversations: List<VKApiConversation>? = null

    @SerializedName("count")
    var count = 0

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null

    @SerializedName("groups")
    var groups: List<VKApiCommunity>? = null

    @SerializedName("contacts")
    var contacts: List<VKApiConversation.ContactElement>? = null
}