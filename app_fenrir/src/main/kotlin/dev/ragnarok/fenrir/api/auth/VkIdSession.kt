package dev.ragnarok.fenrir.api.auth

/** In-memory request ownership; no SID is persisted, printed or assigned an invented TTL. */
class VkIdSession(
    private val now: () -> Long = { System.nanoTime() / 1_000_000 },
    private val epochSeconds: () -> Long = { System.currentTimeMillis() / 1000 }
) {
    companion object {
        fun otpSuffix(method: String) = when (method) {
            "sms" -> "Sms"
            "push" -> "Push"
            "email" -> "Email"
            "callreset" -> "CallReset"
            else -> throw IllegalArgumentException("Unsupported OTP delivery method")
        }
    }

    enum class Phase { EMPTY, ACCOUNT, OTP, CONFIRMED, OAUTH_CHALLENGE, FINISHED }
    enum class Operation(val method: String) {
        ACCOUNT("auth.validateAccount"), METHODS("ecosystem.getVerificationMethods"),
        SEND_OTP("ecosystem.sendOtp"), CHECK_OTP("ecosystem.checkOtp"),
        TOKEN("token"), VALIDATE_PHONE("auth.validatePhone")
    }

    // A normal class with a safe toString: a data class would expose SID in generated output.
    class Request internal constructor(
        val operation: Operation, val sid: String?, val grant: String? = null,
        val needsCode: Boolean = false, internal val epoch: Long,
        internal val ecosystemExchange: Boolean = false
    ) {
        override fun toString() = "AuthRequest(operation=${operation.name},epoch=$epoch)"
    }

    class MissingSession : IllegalStateException("Missing authentication stage session")

    var phase = Phase.EMPTY
        private set
    private var epoch = 0L
    private var revision = 0L
    private var changedAt = now()
    private var ecosystemSid: String? = null
    private var confirmedSid: String? = null
    private var oauthSid: String? = null
    private var oauthGrant: String? = null
    private var oauthExpiresAt: Long? = null
    private var active: Request? = null
    private var blockedUntil = 0L
    val busy get() = active != null
    val blocked get() = now() < blockedUntil
    val needsTokenCode get() = phase == Phase.OAUTH_CHALLENGE

    fun reset() {
        epoch++
        active = null
        ecosystemSid = null
        confirmedSid = null
        oauthSid = null
        oauthGrant = null
        oauthExpiresAt = null
        phase = Phase.EMPTY
        changed()
        // A server throttle is not cleared by editing credentials/restarting a local attempt.
    }

    fun block(retryAfterSeconds: Long? = null) {
        reset()
        blockedUntil = if (retryAfterSeconds != null && retryAfterSeconds > 0 &&
            retryAfterSeconds < (Long.MAX_VALUE - now()) / 1000
        ) now() + retryAfterSeconds * 1000 else Long.MAX_VALUE
    }

    fun beginAccount(): Request? {
        if (busy || blocked) return null
        reset()
        return request(Operation.ACCOUNT, null)
    }

    fun begin(operation: Operation): Request? {
        if (busy || blocked) return null
        rejectExpiredChallenge()
        val sid = when (operation) {
            Operation.METHODS -> if (phase == Phase.ACCOUNT) ecosystemSid else null
            Operation.SEND_OTP, Operation.CHECK_OTP ->
                if (phase == Phase.ACCOUNT || phase == Phase.OTP) ecosystemSid else null

            Operation.VALIDATE_PHONE -> if (phase == Phase.OAUTH_CHALLENGE) oauthSid else null
            else -> null
        } ?: throw MissingSession()
        return request(operation, sid)
    }

    fun beginToken(passwordMethod: Boolean, passwordRequired: Boolean): Request? {
        if (busy || blocked) return null
        rejectExpiredChallenge()
        return when {
            phase == Phase.OAUTH_CHALLENGE -> request(
                Operation.TOKEN, oauthSid ?: throw MissingSession(), oauthGrant, needsCode = true
            )

            (phase == Phase.ACCOUNT || phase == Phase.OTP) && passwordMethod ->
                // Preserve the modern /oauth/token contract of the existing client.
                // This is the fresh account-stage SID, never a leftover OAuth challenge.
                request(
                    Operation.TOKEN,
                    ecosystemSid,
                    "password",
                    ecosystemExchange = ecosystemSid != null
                )

            phase == Phase.CONFIRMED -> request(
                Operation.TOKEN, confirmedSid ?: throw MissingSession(),
                if (passwordRequired) "phone_confirmation_sid" else "without_password",
                ecosystemExchange = true
            )

            else -> throw MissingSession()
        }
    }

    private fun request(
        operation: Operation, sid: String?, grant: String? = null,
        needsCode: Boolean = false, ecosystemExchange: Boolean = false
    ) = Request(operation, sid, grant, needsCode, epoch, ecosystemExchange).also { active = it }

    fun isCurrent(request: Request) = active === request && request.epoch == epoch

    fun account(request: Request, sid: String?): Boolean {
        if (!finish(request)) return false
        ecosystemSid = sid?.takeIf { it.isNotBlank() }
        phase = Phase.ACCOUNT
        changed()
        return true
    }

    fun otpSent(request: Request, sid: String?): Boolean {
        if (!isCurrent(request)) return false
        // sendOtp may omit a replacement; keep the SID that was actually sent.
        ecosystemSid = sid?.takeIf { it.isNotBlank() } ?: request.sid
        finish(request)
        phase = Phase.OTP
        changed()
        return true
    }

    fun confirmed(request: Request, sid: String?): Boolean {
        if (!isCurrent(request)) return false
        // Never exchange an unconfirmed checkOtp request SID when VK omits the result SID.
        val next = sid?.takeIf { it.isNotBlank() } ?: throw MissingSession()
        finish(request)
        ecosystemSid = null
        confirmedSid = next
        phase = Phase.CONFIRMED
        changed()
        return true
    }

    fun challenge(request: Request, sid: String?, expiresAt: Long? = null): Boolean {
        if (!isCurrent(request)) return false
        val next = sid?.takeIf { it.isNotBlank() } ?: throw MissingSession()
        if (expiresAt != null && expiresAt > 0 && expiresAt <= epochSeconds()) throw MissingSession()
        finish(request)
        ecosystemSid = null
        confirmedSid = null
        oauthSid = next
        oauthGrant = request.grant
        oauthExpiresAt = expiresAt?.takeIf { it > 0 }
        phase = Phase.OAUTH_CHALLENGE
        changed()
        return true
    }

    fun phoneValidated(request: Request, sid: String?): Boolean {
        if (!isCurrent(request)) return false
        oauthSid = sid?.takeIf { it.isNotBlank() } ?: request.sid
        finish(request)
        changed()
        return true
    }

    fun finish(request: Request): Boolean {
        if (!isCurrent(request)) return false
        active = null
        return true
    }

    fun failed(
        request: Request,
        explicitCaptcha: Boolean = false,
        uncertain: Boolean = false
    ): Boolean {
        if (!finish(request)) return false
        if (!explicitCaptcha && (request.ecosystemExchange || uncertain)) reset()
        return true
    }

    fun succeeded(request: Request): Boolean {
        if (!finish(request)) return false
        reset()
        phase = Phase.FINISHED
        return true
    }

    private fun changed() {
        revision++; changedAt = now()
    }

    private fun rejectExpiredChallenge() {
        if (phase == Phase.OAUTH_CHALLENGE && oauthExpiresAt?.let { it <= epochSeconds() } == true) {
            reset()
            throw MissingSession()
        }
    }

    fun diagnosticState() = "phase=${phase.name} epoch=$epoch sid_revision=$revision " +
            "stage_age_s=${((now() - changedAt) / 1000).coerceAtLeast(0)} busy=$busy blocked=$blocked"
}
