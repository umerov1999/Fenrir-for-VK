package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName

class RepostReponse {
    @SerializedName("post_id")
    var postId: Int? = null

    @SerializedName("reposts_count")
    var repostsCount: Int? = null

    @SerializedName("likes_count")
    var likesCount: Int? = null
}