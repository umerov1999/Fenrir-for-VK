package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class CommentsDto {
    @SerializedName("count")
    var count = 0

    @SerializedName("can_post")
    var canPost = false

    @SerializedName("list")
    var list: List<VKApiComment>? = null
}