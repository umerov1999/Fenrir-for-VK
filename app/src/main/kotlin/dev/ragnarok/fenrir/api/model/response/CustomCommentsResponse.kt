package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiComment
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiPoll
import dev.ragnarok.fenrir.api.model.VKApiUser

class CustomCommentsResponse {
    // Parse manually in CustomCommentsResponseAdapter
    var main: Main? = null
    var firstId: Int? = null
    var lastId: Int? = null
    var admin_level: Int? = null

    class Main {
        @SerializedName("count")
        var count = 0

        @SerializedName("items")
        var comments: List<VKApiComment>? = null

        @SerializedName("profiles")
        var profiles: List<VKApiUser>? = null

        @SerializedName("groups")
        var groups: List<VKApiCommunity>? = null

        @SerializedName("poll")
        var poll: VKApiPoll? = null
    }
}