package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.Items
import dev.ragnarok.fenrir.api.model.VKApiUser

class PollUsersResponse {
    @SerializedName("users")
    var users: Items<VKApiUser>? = null
}