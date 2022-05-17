package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiPost
import dev.ragnarok.fenrir.api.model.VKApiUser

class WallSearchResponse {
    @SerializedName("count")
    var count = 0

    @SerializedName("items")
    var items: List<VKApiPost>? = null

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null

    @SerializedName("groups")
    var groups: List<VKApiCommunity>? = null
}