package dev.ragnarok.fenrir.dialog.directauth

import android.util.Log
import dev.ragnarok.fenrir.BuildConfig
import dev.ragnarok.fenrir.api.auth.AccountCaptchaChallenge
import dev.ragnarok.fenrir.api.auth.AuthFailurePolicy
import dev.ragnarok.fenrir.api.auth.VkIdSession
import dev.ragnarok.fenrir.api.exceptions.ApiException
import dev.ragnarok.fenrir.api.exceptions.AuthException
import dev.ragnarok.fenrir.api.exceptions.NeedValidationException
import dev.ragnarok.fenrir.api.exceptions.VKIdCaptchaNeedException
import dev.ragnarok.fenrir.api.rest.HttpException
import kotlinx.serialization.SerializationException
import java.io.IOException

internal object VkIdDiagnostics {
    fun cause(error: Throwable): Throwable {
        return AuthFailurePolicy.cause(error) { result ->
            result is ApiException || result is AuthException ||
                    result is NeedValidationException ||
                    result is VKIdCaptchaNeedException ||
                    result is HttpException || result is IOException || result is SerializationException ||
                    result is VkIdSession.MissingSession
        }
    }

    fun kind(error: Throwable): AuthFailurePolicy.Kind {
        val value = cause(error)
        return AuthFailurePolicy.classify(
            apiCode(value),
            (value as? AuthException)?.code,
            // Only server authentication/API descriptions can identify a server SID error.
            value.message.takeIf { value is ApiException || value is AuthException },
            (value as? HttpException)?.code,
            networkFailure = value is IOException, apiFailure = value is ApiException,
            authenticationFailure = value is AuthException,
            missingSession = value is VkIdSession.MissingSession,
            captchaRequired = value is VKIdCaptchaNeedException,
            invalidResponse = value is SerializationException
        )
    }

    private fun apiCode(error: Throwable?) = (error as? ApiException)?.error?.errorCode
        ?: (error as? AuthException)?.code?.toIntOrNull()

    fun record(
        request: VkIdSession.Request,
        event: Event,
        session: VkIdSession,
        error: Throwable? = null,
        verificationMethod: String? = null
    ): String {
        val value = error?.let(::cause)
        val oauth = when (value) {
            is NeedValidationException -> "need_validation"
            is VKIdCaptchaNeedException -> "need_captcha"
            else -> (value as? AuthException)?.code
        }
        val codes =
            AuthFailurePolicy.safeCodes(apiCode(value), oauth, (value as? HttpException)?.code)
        val challenge = when (value) {
            is ApiException if value.error.errorCode == 14 -> AccountCaptchaChallenge(
                request,
                value.error.redirectUri, value.error.captchaSid, value.error.captchaImg
            ).kind.name

            is VKIdCaptchaNeedException -> "WEB"
            else -> "none"
        }
        val method = verificationMethod?.takeIf {
            it in setOf(
                "password",
                "push",
                "email",
                "sms",
                "callreset",
                "reserve_code",
                "codegen"
            )
        } ?: "other"
        val endpoint = if (request.operation == VkIdSession.Operation.SEND_OTP && method in setOf(
                "sms",
                "push",
                "email",
                "callreset"
            )
        )
            request.operation.method + VkIdSession.otpSuffix(method) else request.operation.method
        val result = "build=${BuildConfig.VERSION_NAME} variant=${BuildConfig.BUILD_TYPE} " +
                "method=$endpoint verification=$method event=${event.name} $codes request_sid_present=${!request.sid.isNullOrBlank()} " +
                "kind=${value?.let(::kind)?.name ?: "none"} captcha=$challenge ${session.diagnosticState()}"
        // Never pass Throwable, URLs, request/response bodies, or opaque identifiers to Log.
        Log.i("FenrirAuth", result)
        return result
    }

    enum class Event { START, SUCCESS, ERROR, STALE }
}