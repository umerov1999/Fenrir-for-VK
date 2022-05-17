package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiMessage

class LongpollHistoryResponse {
    @SerializedName("messages")
    var messages: Messages? = null

    class Messages {
        @SerializedName("items")
        var items: List<VKApiMessage>? = null
    }
}