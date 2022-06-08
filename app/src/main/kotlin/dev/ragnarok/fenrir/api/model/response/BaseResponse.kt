package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.Error

class BaseResponse<T> : VkResponse() {
    @SerializedName("response")
    var response: T? = null

    @SerializedName("execute_errors")
    var executeErrors: List<Error>? = null
}