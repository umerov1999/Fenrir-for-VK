package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiUser

class FavePageResponse {
    @SerializedName("description")
    var description: String? = null

    @SerializedName("type")
    var type: String? = null

    @SerializedName("updated_date")
    var updated_date: Long = 0

    @SerializedName("user")
    var user: VKApiUser? = null

    @SerializedName("group")
    var group: VKApiCommunity? = null
}