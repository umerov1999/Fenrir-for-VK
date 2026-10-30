package dev.ragnarok.fenrir.api.auth

/** CAPTCHA identifiers are separate from account/OTP/OAuth authentication sessions. */
class AccountCaptchaChallenge(
    val request: VkIdSession.Request, val redirectUri: String?,
    private val captchaSid: String?, val image: String?
) {
    enum class Kind { WEB, IMAGE, UNAVAILABLE }

    val kind = when {
        !redirectUri.isNullOrBlank() -> Kind.WEB
        !captchaSid.isNullOrBlank() && !image.isNullOrBlank() -> Kind.IMAGE
        else -> Kind.UNAVAILABLE
    }

    fun webAnswer(request: VkIdSession.Request, token: String?): AuthCaptchaAnswer? =
        if (this.request === request && kind == Kind.WEB && !token.isNullOrBlank())
            AuthCaptchaAnswer(successToken = token) else null

    fun imageAnswer(request: VkIdSession.Request, key: String?): AuthCaptchaAnswer? =
        if (this.request === request && kind == Kind.IMAGE && !key.isNullOrBlank())
            AuthCaptchaAnswer(sid = captchaSid, key = key.trim()) else null

    override fun toString() = "AccountCaptchaChallenge(kind=${kind.name})"
}

class AuthCaptchaAnswer internal constructor(
    val successToken: String? = null, val sid: String? = null, val key: String? = null
) {
    override fun toString() = "AuthCaptchaAnswer(redacted)"
}
