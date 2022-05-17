package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiConversationMembers
import dev.ragnarok.fenrir.api.model.VKApiUser

class ConversationMembersResponse {
    @SerializedName("items")
    var conversationMembers: List<VKApiConversationMembers>? = null

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null

    @SerializedName("groups")
    var groups: List<VKApiCommunity>? = null
}