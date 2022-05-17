package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiNarratives {
    @SerializedName("id")
    var id = 0

    @SerializedName("owner_id")
    var owner_id = 0

    @SerializedName("title")
    var title: String? = null

    @SerializedName("story_ids")
    var story_ids: IntArray? = null

    @SerializedName("cover")
    var cover: String? = null
}