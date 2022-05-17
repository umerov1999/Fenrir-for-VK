package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiCheckedLink {
    @SerializedName("status")
    var status: String? = null

    @SerializedName("link")
    var link: String? = null
}