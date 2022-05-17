package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiAttachments
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiUser

class AttachmentsHistoryResponse {
    @SerializedName("items")
    var items: List<One>? = null

    @SerializedName("next_from")
    var next_from: String? = null

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null

    @SerializedName("groups")
    var groups: List<VKApiCommunity>? = null

    class One {
        @SerializedName("message_id")
        var messageId = 0

        @SerializedName("attachment")
        var entry: VKApiAttachments.Entry? = null
    }
}