package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName

class isLikeResponse {
    @SerializedName("liked")
    var liked = 0

    @SerializedName("copied")
    var copied = 0
}