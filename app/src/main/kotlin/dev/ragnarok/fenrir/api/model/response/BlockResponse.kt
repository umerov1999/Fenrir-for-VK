package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName

class BlockResponse<T> {
    @SerializedName("block")
    var block: T? = null
}