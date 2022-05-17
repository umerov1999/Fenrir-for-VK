package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName

class BaseResponse<T> : VkReponse() {
    @SerializedName("response")
    var response: T? = null
}