package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiStickersKeywords {
    @SerializedName("words_stickers")
    var words_stickers: List<List<VKApiSticker>>? = null

    @SerializedName("keywords")
    var keywords: List<List<String>>? = null
}