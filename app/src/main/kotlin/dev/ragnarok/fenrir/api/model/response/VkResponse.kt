package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.Error

open class VkResponse {
    @SerializedName("error")
    var error: Error? = null
}