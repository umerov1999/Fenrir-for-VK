package dev.ragnarok.fenrir.api.model.ecosystem

import android.content.Context
import dev.ragnarok.fenrir.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class EcosystemGetVerificationMethods {
    @SerialName("methods")
    var methods: ArrayList<Method>? = null

    @Serializable
    class Method {
        @SerialName("name")
        var name: String? = null

        @SerialName("priority")
        var priority: Int = 0

        @SerialName("timeout")
        var timeout: Int = 0

        @SerialName("info")
        var info: String? = null

        @SerialName("can_fallback")
        var canFallback: Boolean = false

        fun setName(name: String?): Method {
            this.name = name
            return this
        }

        fun getDisplayedName(context: Context): String {
            return when (name) {
                "push" -> {
                    context.getString(R.string.auth_validate_method_push, info ?: "")
                }

                "email" -> {
                    context.getString(R.string.auth_validate_method_email, info ?: "")
                }

                "sms" -> {
                    context.getString(R.string.auth_validate_method_sms, info ?: "")
                }

                "callreset" -> {
                    context.getString(R.string.auth_validate_method_callreset, info ?: "")
                }

                "password" -> {
                    context.getString(R.string.auth_validate_method_password)
                }

                "reserve_code" -> {
                    context.getString(R.string.auth_validate_method_reserve_code)
                }

                "codegen" -> {
                    context.getString(R.string.auth_validate_method_codegen)
                }

                else -> {
                    name ?: "[null]"
                }
            }
        }

    }
}