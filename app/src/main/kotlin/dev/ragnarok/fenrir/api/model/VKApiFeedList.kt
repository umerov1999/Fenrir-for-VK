package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiFeedList {
    @SerializedName("id")
    var id = 0

    @SerializedName("title")
    var title: String? = null

    @SerializedName("no_reposts")
    var no_reposts = false

    @SerializedName("source_ids")
    var source_ids: IntArray? = null
}