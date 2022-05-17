package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class LocalServerSettings {
    @SerializedName("url")
    var url: String? = null

    @SerializedName("password")
    var password: String? = null

    @SerializedName("enabled")
    var enabled = false
}