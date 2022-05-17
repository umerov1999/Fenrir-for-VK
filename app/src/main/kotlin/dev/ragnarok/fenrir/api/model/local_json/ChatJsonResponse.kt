package dev.ragnarok.fenrir.api.model.local_json

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiMessage

class ChatJsonResponse {
    var type: String? = null
    var messages: List<VKApiMessage>? = null
    var version: Version? = null
    var page_id = 0
    var page_title: String? = null
    var page_avatar: String? = null
    var page_phone_number: String? = null
    var page_instagram: String? = null
    var page_site: String? = null

    class Version {
        @SerializedName("float")
        var floatValue = 0f

        @SerializedName("string")
        var stringValue = 0f
    }
}