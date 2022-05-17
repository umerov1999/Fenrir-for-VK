package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiNews
import dev.ragnarok.fenrir.api.model.VKApiUser

class NewsfeedResponse {
    @SerializedName("items")
    var items: List<VKApiNews>? = null

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null

    @SerializedName("groups")
    var groups: List<VKApiCommunity>? = null

    @SerializedName("next_from")
    var nextFrom: String? = null
}