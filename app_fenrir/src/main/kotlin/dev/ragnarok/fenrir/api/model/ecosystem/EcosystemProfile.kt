package dev.ragnarok.fenrir.api.model.ecosystem

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class EcosystemProfile {
    @SerialName("can_unbind_phone")
    var canUnbindPhone: Boolean = false

    @SerialName("first_name")
    var firstName: String? = null

    @SerialName("has_2fa")
    var has2fa: Boolean = false

    @SerialName("last_name")
    var lastName: String? = null

    @SerialName("phone")
    var phone: String? = null

    @SerialName("photo_200")
    var photo200: String? = null
}