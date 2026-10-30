package dev.ragnarok.fenrir.api.auth

/** Classification inspects server text in memory, but never returns/records that text. */
object AuthFailurePolicy {
    enum class Kind { SID_INVALID, MISSING_SESSION, THROTTLED, NETWORK, HTTP, API, AUTHENTICATION, INVALID_RESPONSE, CAPTCHA, OTHER }
    enum class Recovery { BLOCK, RESTART_SESSION, RESTART_REQUEST, CAPTCHA, RETRY_MANUALLY }

    fun recovery(kind: Kind, phase: VkIdSession.Phase): Recovery = when {
        kind == Kind.THROTTLED -> Recovery.BLOCK
        kind == Kind.SID_INVALID || kind == Kind.MISSING_SESSION -> Recovery.RESTART_SESSION
        kind == Kind.CAPTCHA -> Recovery.CAPTCHA
        // EMPTY controls where a manual retry starts, never what caused the failure.
        phase == VkIdSession.Phase.EMPTY -> Recovery.RESTART_REQUEST
        else -> Recovery.RETRY_MANUALLY
    }

    private val sidError = Regex(
        "\\b(?:validation_)?sid\\b.{0,40}\\b(?:invalid|expired|consumed|used)\\b|" +
                "\\b(?:invalid|expired)\\s+(?:validation_)?sid\\b", RegexOption.IGNORE_CASE
    )
    private val safeOAuthCodes = setOf(
        "need_validation",
        "need_captcha",
        "invalid_grant",
        "invalid_client",
        "invalid_request",
        "invalid_sid",
        "sid_expired",
        "access_denied",
        "invalid_response",
        "invalid_code",
        "too_many_requests",
        "too_many_attempts",
        "temporarily_unavailable",
        "rate_limit_reached",
        "flood_control"
    )

    fun classify(
        apiCode: Int?, oauthCode: String?, description: String?, httpCode: Int? = null,
        networkFailure: Boolean = false, apiFailure: Boolean = apiCode != null,
        authenticationFailure: Boolean = oauthCode != null, missingSession: Boolean = false,
        captchaRequired: Boolean = false, invalidResponse: Boolean = false
    ): Kind {
        if (missingSession) return Kind.MISSING_SESSION
        if (networkFailure) return Kind.NETWORK
        val code = oauthCode?.lowercase()
        val text = description.orEmpty().lowercase()
        if (apiCode in setOf(6, 9, 29) || httpCode == 429 || code in setOf(
                "too_many_requests",
                "too_many_attempts",
                "temporarily_unavailable",
                "rate_limit_reached",
                "flood_control"
            ) || Regex("too many (?:auth |login |validation |code )?(?:attempts|requests)").containsMatchIn(
                text
            ) || listOf(
                "flood control", "temporarily blocked",
                "try again later"
            ).any { it in text }
        ) return Kind.THROTTLED
        return when {
            captchaRequired || apiCode == 14 || code == "need_captcha" -> Kind.CAPTCHA
            code == "invalid_sid" || code == "sid_expired" || sidError.containsMatchIn(text) -> Kind.SID_INVALID
            invalidResponse || code == "invalid_response" -> Kind.INVALID_RESPONSE
            httpCode != null -> Kind.HTTP
            authenticationFailure || apiCode in setOf(5, 27, 28) -> Kind.AUTHENTICATION
            apiFailure -> Kind.API
            else -> Kind.OTHER
        }
    }

    fun safeCodes(apiCode: Int?, oauthCode: String?, httpCode: Int?) =
        "api_code=${apiCode ?: "none"} oauth=${safeOAuthCode(oauthCode)} http=${httpCode ?: "none"}"

    fun safeOAuthCode(code: String?) = code?.lowercase()?.takeIf { it in safeOAuthCodes } ?: "other"

    fun cause(error: Throwable, terminal: (Throwable) -> Boolean): Throwable {
        var result = error
        repeat(8) {
            if (terminal(result)) return result
            val next = result.cause ?: return result
            if (next === result) return result
            result = next
        }
        return result
    }

    fun suppressBodyLogging(path: String): Boolean {
        val method = path.substringAfterLast('/').lowercase()
        return method.startsWith("auth.") || method.startsWith("ecosystem.") || method in setOf(
            "token", "get_anonym_token", "auth_by_exchange_token", "captcha.php"
        )
    }
}
