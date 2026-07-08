package dev.ragnarok.fenrir.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class VKApiValidationResponse {
    @SerialName("type")
    var type: String? = null

    @SerialName("sid")
    var sid: String? = null

    @SerialName("delay")
    var delay = 0

    @SerialName("validation_type")
    var validationType: String? = null

    @SerialName("validation_resend")
    var validationResend: String? = null

    @SerialName("external_id")
    var externalId: String? = null

    @SerialName("code_length")
    var codeLength: Int = 0
}