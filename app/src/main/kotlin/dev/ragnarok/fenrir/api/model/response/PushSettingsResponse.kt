package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.response.PushSettingsResponse.ConversationsPush.ConversationPushItem

class PushSettingsResponse {
    @SerializedName("conversations")
    var conversations: ConversationsPush? = null
    val pushSettings: List<ConversationPushItem>
        get() = conversations?.items ?: emptyList()

    class ConversationsPush {
        @SerializedName("items")
        var items: List<ConversationPushItem>? = null

        class ConversationPushItem {
            @SerializedName("disabled_until")
            var disabled_until = 0

            @SerializedName("peer_id")
            var peer_id = 0
        }
    }
}