package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class RefreshToken {
    @SerializedName("token")
    var token: String? = null
}