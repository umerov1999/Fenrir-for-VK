package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiUser
import dev.ragnarok.fenrir.api.model.feedback.VKApiBaseFeedback

class NotificationsResponse {
    @SerializedName("count")
    var count = 0

    @SerializedName("items")
    var notifications: List<VKApiBaseFeedback>? = null

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null

    @SerializedName("groups")
    var groups: List<VKApiCommunity>? = null

    @SerializedName("next_from")
    var nextFrom: String? = null

    @SerializedName("last_viewed")
    var lastViewed: Long = 0
}