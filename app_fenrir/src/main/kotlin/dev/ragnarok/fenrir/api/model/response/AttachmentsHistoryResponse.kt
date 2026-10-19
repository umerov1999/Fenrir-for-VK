package dev.ragnarok.fenrir.api.model.response

import dev.ragnarok.fenrir.api.model.VKApiAttachments
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class AttachmentsHistoryResponse {
    @SerialName("items")
    var items: List<One>? = null

    @SerialName("next_from")
    var next_from: String? = null

    @Serializable
    class One {
        @SerialName("cmid")
        var cmId = 0

        @SerialName("attachment")
        var entry: VKApiAttachments.Entry? = null
    }
}