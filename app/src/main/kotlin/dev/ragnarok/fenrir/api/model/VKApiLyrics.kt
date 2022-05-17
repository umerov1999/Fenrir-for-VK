package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiLyrics {
    @SerializedName("lyrics_id")
    var lyrics_id = 0

    @SerializedName("text")
    var text: String? = null
}