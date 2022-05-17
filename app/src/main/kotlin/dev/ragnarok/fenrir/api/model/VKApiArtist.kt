package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiArtist {
    @SerializedName("domain")
    var domain: String? = null

    @SerializedName("id")
    var id: String? = null

    @SerializedName("name")
    var name: String? = null
}