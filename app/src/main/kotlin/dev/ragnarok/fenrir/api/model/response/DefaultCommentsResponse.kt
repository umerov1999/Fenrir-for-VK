package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiComment
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiUser

class DefaultCommentsResponse {
    @SerializedName("count")
    var count = 0

    @SerializedName("items")
    var items: List<VKApiComment>? = null

    @SerializedName("groups")
    var groups: List<VKApiCommunity>? = null

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null
}