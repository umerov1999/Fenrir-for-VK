package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiProfileInfo {
    @SerializedName("first_name")
    var first_name: String? = null

    @SerializedName("last_name")
    var last_name: String? = null

    @SerializedName("maiden_name")
    var maiden_name: String? = null

    @SerializedName("screen_name")
    var screen_name: String? = null

    @SerializedName("home_town")
    var home_town: String? = null

    @SerializedName("bdate")
    var bdate: String? = null

    @SerializedName("sex")
    var sex = 0
}