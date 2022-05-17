package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiValidationResponse {
    @SerializedName("sid")
    var sid: String? = null

    @SerializedName("delay")
    var delay = 0

    @SerializedName("validation_type")
    var validation_type: String? = null

    @SerializedName("validation_resend")
    var validation_resend: String? = null
}