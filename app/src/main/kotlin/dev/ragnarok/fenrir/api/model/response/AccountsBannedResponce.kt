package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiUser

class AccountsBannedResponce {
    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null
}