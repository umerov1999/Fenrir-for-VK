package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiTopic
import dev.ragnarok.fenrir.api.model.VKApiUser

class TopicsResponse {
    @SerializedName("count")
    var count = 0

    @SerializedName("items")
    var items: List<VKApiTopic>? = null

    @SerializedName("default_order")
    var defaultOrder = 0

    @SerializedName("can_add_topics")
    var canAddTopics = 0

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null

    @SerializedName("groups")
    var groups: List<VKApiCommunity>? = null
}