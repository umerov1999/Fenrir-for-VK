package dev.ragnarok.fenrir.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class VKApiValidateAccount {
    @SerialName("flow_name")
    var flowName: String? = null

    @SerialName("flow_names")
    var flowNames: ArrayList<String>? = null

    @SerialName("is_phone")
    var isPhone = false

    @SerialName("is_email")
    var isEmail = false

    @SerialName("sid")
    var sid: String? = null

    @SerialName("next_step")
    var nextStep: NextVerificationStep? = null

    @Serializable
    class NextVerificationStep {
        @SerialName("available_libverify_verification_types")
        var availableLibVerifyVerificationTypes: ArrayList<String>? = null

        @SerialName("external_id")
        var externalId: String? = null

        @SerialName("has_another_verification_methods")
        var hasAnotherVerificationMethods = false

        @SerialName("service_code")
        var serviceCode = false

        @SerialName("verification_method")
        var verificationMethod: String? = null

    }

}