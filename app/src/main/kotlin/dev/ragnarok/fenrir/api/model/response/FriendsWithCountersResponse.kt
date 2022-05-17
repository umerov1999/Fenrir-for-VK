package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.Items
import dev.ragnarok.fenrir.api.model.VKApiUser

class FriendsWithCountersResponse {
    @SerializedName("friends")
    var friends: Items<VKApiUser>? = null

    @SerializedName("counters")
    var counters: VKApiUser.Counters? = null
}