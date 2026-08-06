package dev.ragnarok.fenrir.api.model.ecosystem

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class EcosystemSendOtp {
    @SerialName("code_length")
    var codeLength: Int = 0

    @SerialName("info")
    var info: String? = null

    @SerialName("sid")
    var sid: String? = null

    @SerialName("status")
    var status = 0
}
