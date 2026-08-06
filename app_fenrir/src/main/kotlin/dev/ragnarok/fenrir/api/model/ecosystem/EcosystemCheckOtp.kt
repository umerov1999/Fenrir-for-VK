package dev.ragnarok.fenrir.api.model.ecosystem

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class EcosystemCheckOtp {
    @SerialName("sid")
    var sid: String? = null

    @SerialName("auth_hash")
    var authHash: String? = null

    @SerialName("can_skip_password")
    var canSkipPassword: Boolean = false

    @SerialName("profile_exist")
    var profileExist: Boolean = false

    @SerialName("profile")
    var profile: EcosystemProfile? = null
}