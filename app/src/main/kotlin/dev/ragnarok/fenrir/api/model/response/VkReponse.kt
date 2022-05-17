package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.Error

open class VkReponse {
    @SerializedName("error")
    var error: Error? = null

    @SerializedName("execute_errors")
    var executeErrors: List<Error>? = null
}