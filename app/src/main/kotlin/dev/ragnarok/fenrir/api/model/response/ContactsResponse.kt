package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiConversation
import dev.ragnarok.fenrir.api.model.VKApiUser

class ContactsResponse {
    @SerializedName("items")
    var items: List<Int>? = null

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null

    @SerializedName("contacts")
    var contacts: List<VKApiConversation.ContactElement>? = null
}