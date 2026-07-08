package dev.ragnarok.fenrir.dialog.directauth

import android.os.Bundle
import dev.ragnarok.fenrir.AccountType
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.Includes.networkInterfaces
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.Auth.scope
import dev.ragnarok.fenrir.api.exceptions.ApiException
import dev.ragnarok.fenrir.api.exceptions.CaptchaLegacyNeedException
import dev.ragnarok.fenrir.api.exceptions.NeedValidationException
import dev.ragnarok.fenrir.api.exceptions.VKIdCaptchaNeedException
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.response.LoginResponse
import dev.ragnarok.fenrir.fragment.base.RxSupportPresenter
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.service.ApiErrorCodes
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.trimmedNonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.delayedFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain

class DirectAuthPresenter(savedInstanceState: Bundle?) :
    RxSupportPresenter<IDirectAuthView>(savedInstanceState) {
    private val networker: INetworker = networkInterfaces
    private var requiredCaptchaLegacy: CaptchaLegacy? = null
    private var requiredVKIdCaptcha: VKIdCaptcha? = null
    private var requireSmsHelp = false
    private var requireSmsCode = false
    private var requireAppCode = false
    private var savePassword = false
    private var loginNow = false
    private var username: String? = null
    private var pass: String? = null
    private var smsCode: String? = null
    private var smsSid: String? = null
    private var captcha: String? = null
    private var appCode: String? = null
    private var redirectUrl: String? = null
    private var twoFaDescription: String? = null
    private var smsCodeHint: String? = null
    fun fireLoginClick() {
        doLogin(false)
    }

    fun fireVKIdCaptchaSuccess(token: String?) {
        requiredVKIdCaptcha?.success_token = token
    }

    private fun doLogin(forceSms: Boolean) {
        view?.hideKeyboard()
        val trimmedUsername = if (username.nonNullNoEmpty()) username?.trim() else ""
        val trimmedPass = if (pass.nonNullNoEmpty()) pass?.trim() else ""
        val captchaLegacySid =
            if (requiredCaptchaLegacy != null) requiredCaptchaLegacy?.captchaSid else null
        val captchaLegacyCode = if (captcha.nonNullNoEmpty()) captcha?.trim() else null
        val smsCode: String? = if (requireSmsCode) {
            if (smsCode.nonNullNoEmpty()) smsCode?.trim() else null
        } else if (requireAppCode) {
            if (appCode.nonNullNoEmpty()) appCode?.trim() else null
        } else {
            null
        }
        setLoginNow(true)
        appendJob(
            networker.vkDirectAuth()
                .directLogin(
                    "password",
                    Constants.API_ID,
                    Constants.SECRET,
                    trimmedUsername,
                    trimmedPass,
                    Constants.AUTH_API_VERSION,
                    Constants.DEFAULT_ACCOUNT_TYPE == AccountType.VK_ANDROID,
                    scope,
                    smsCode,
                    captchaLegacySid,
                    captchaLegacyCode,
                    requiredVKIdCaptcha?.success_token,
                    forceSms,
                    Constants.DEFAULT_ACCOUNT_TYPE == AccountType.VK_ANDROID,
                    smsSid,
                    if (Constants.DEFAULT_ACCOUNT_TYPE == AccountType.VK_ANDROID) Settings.get()
                        .accounts().anonymToken.token else null
                )
                .fromIOToMain({
                    onLoginResponse(it)
                }) {
                    onLoginError(getCauseIfRuntime(it))
                })
    }

    private fun onLoginError(t: Throwable) {
        setLoginNow(false)
        requireSmsHelp = smsCode.nonNullNoEmpty()
        requiredCaptchaLegacy = null
        requiredVKIdCaptcha = null
        requireAppCode = false
        requireSmsCode = false
        twoFaDescription = null
        smsCodeHint = null
        if (t is CaptchaLegacyNeedException) {
            val sid = t.captchaSid ?: return showError(t)
            val img = t.captchaImg ?: return showError(t)
            requiredCaptchaLegacy = CaptchaLegacy(sid, img)
        } else if (t is VKIdCaptchaNeedException) {
            requiredVKIdCaptcha = VKIdCaptcha(t.redirect_uri, t.domain, null)
        } else if (t is NeedValidationException) {
            if (Constants.DEFAULT_ACCOUNT_TYPE == AccountType.KATE) {
                redirectUrl = t.validationURL
                if (!redirectUrl.isNullOrEmpty()) {
                    onValidate()
                }
            } else {
                val type = t.validationType
                val sid = t.sid
                val phone = t.phone
                smsSid = sid
                when {
                    "2fa_sms".equals(type, ignoreCase = true) || "2fa_callreset".equals(
                        type,
                        ignoreCase = true
                    ) || "2fa_libverify".equals(
                        type,
                        ignoreCase = true
                    ) -> {
                        requireSmsCode = true
                        redirectUrl = t.validationURL
                        if ("2fa_callreset".equals(
                                type,
                                ignoreCase = true
                            )
                        ) {
                            requireSmsHelp = true
                            twoFaDescription = t.description
                        }
                    }

                    "2fa_app".equals(type, ignoreCase = true) || "2fa_push".equals(
                        type,
                        ignoreCase = true
                    ) -> {
                        requireAppCode = true
                    }

                    else -> {
                        showError(t)
                        redirectUrl = t.validationURL
                        if (!redirectUrl.isNullOrEmpty()) {
                            onValidate()
                        }
                    }
                }
                if (phone.nonNullNoEmpty() && !sid.isNullOrEmpty() && requireSmsCode) {
                    appendJob(
                        networker.vkAuth()
                            .validatePhone(
                                phone,
                                Constants.API_ID,
                                Constants.API_ID,
                                Constants.SECRET,
                                sid,
                                Constants.AUTH_API_VERSION,
                                libverify_support = true,
                                allow_callreset = true
                            )
                            .delayedFlow(1000)
                            .fromIOToMain({
                                if ("callreset" == it.validationType) {
                                    smsCodeHint = Includes.provideApplicationContext()
                                        .getString(R.string.call_reset_description, it.codeLength)
                                    resolveSmsCodeHint()
                                }
                            }) {
                                showError(getCauseIfRuntime(t))
                            })
                }
            }
        } else {
            showError(t)
        }
        resolveCaptchaViews()
        resolveSmsRootVisibility()
        resolveTwoFaDescription()
        resolveSmsHelpVisibility()
        resolveAppCodeRootVisibility()
        resolveButtonLoginState()
        resolveSmsCodeHint()

        when {
            requiredVKIdCaptcha != null -> {
                view?.openVKIdCaptcha(
                    requiredVKIdCaptcha?.redirect_uri,
                    requiredVKIdCaptcha?.domain
                )
            }

            requiredCaptchaLegacy != null -> {
                view?.moveFocusToCaptchaLegacy()
            }

            requireSmsHelp -> {
                view?.moveFocusToSmsHelp()
            }

            requireSmsCode -> {
                view?.moveFocusToSmsCode()
            }

            requireAppCode -> {
                view?.moveFocusToAppCode()
            }
        }
    }

    private fun resolveSmsRootVisibility() {
        view?.setSmsRootVisible(requireSmsCode)
    }

    private fun resolveSmsHelpVisibility() {
        view?.setSmsHelpVisible(requireSmsHelp)
    }

    private fun resolveAppCodeRootVisibility() {
        view?.setAppCodeRootVisible(
            requireAppCode
        )
    }

    private fun resolveCaptchaViews() {
        view?.setCaptchaLegacyRootVisible(requiredCaptchaLegacy != null)
        if (requiredCaptchaLegacy != null) {
            view?.displayCaptchaLegacyImage(
                requiredCaptchaLegacy?.captchaImg
            )
        }
    }

    private fun onLoginResponse(response: LoginResponse) {
        setLoginNow(false)
        var TwFa = "none"
        if (response.access_token.nonNullNoEmpty() && response.user_id > 0) {
            val Pass = if (pass.nonNullNoEmpty()) pass?.trim() else ""
            if (requireSmsCode) TwFa = "2fa_sms" else if (requireAppCode) TwFa = "2fa_app"
            val TwFafin = TwFa
            view?.returnSuccessToParent(
                response.user_id,
                response.access_token,
                if (username.nonNullNoEmpty()) username?.trim() else "",
                Pass,
                TwFafin,
                savePassword
            )
        } else if (response.errorBasic != null && response.errorBasic?.errorCode == ApiErrorCodes.VALIDATE_NEED) {
            view?.startDefaultValidation(response.errorBasic?.redirectUri)
        } else if (response.errorBasic != null) {
            response.errorBasic?.let {
                view?.showThrowable(ApiException(it))
            }
        }
    }

    private fun onValidate() {
        view?.returnSuccessValidation(
            redirectUrl,
            if (username.nonNullNoEmpty()) username?.trim() else "",
            if (pass.nonNullNoEmpty()) pass?.trim() else "",
            "web_validation",
            savePassword
        )
    }

    private fun setLoginNow(loginNow: Boolean) {
        this.loginNow = loginNow
        resolveLoadingViews()
    }

    private fun resolveLoadingViews() {
        view?.displayLoading(loginNow)
    }

    override fun onGuiCreated(viewHost: IDirectAuthView) {
        super.onGuiCreated(viewHost)
        resolveLoadingViews()
        resolveSmsRootVisibility()
        resolveSmsHelpVisibility()
        resolveAppCodeRootVisibility()
        resolveCaptchaViews()
        resolveTwoFaDescription()
        resolveSmsCodeHint()
    }

    fun fireLoginViaWebClick() {
        view?.returnLoginViaWebAction()
    }

    override fun onGuiResumed() {
        super.onGuiResumed()
        resolveButtonLoginState()
    }

    private fun resolveButtonLoginState() {
        resumedView?.setLoginButtonEnabled(
            username.trimmedNonNullNoEmpty()
                    && pass.nonNullNoEmpty()
                    && (requiredCaptchaLegacy == null || captcha.trimmedNonNullNoEmpty())
                    && (!requireSmsCode || smsCode.trimmedNonNullNoEmpty())
                    && (!requireAppCode || appCode.trimmedNonNullNoEmpty())
        )
    }

    private fun resolveTwoFaDescription() {
        view?.updateSmsDescription(twoFaDescription)
    }

    private fun resolveSmsCodeHint() {
        view?.updateSmsHint(smsCodeHint)
    }

    fun fireLoginEdit(sequence: CharSequence?) {
        username = sequence.toString()
        resolveButtonLoginState()
    }

    fun firePasswordEdit(s: CharSequence?) {
        pass = s.toString()
        resolveButtonLoginState()
    }

    fun fireSmsCodeEdit(sequence: CharSequence?) {
        smsCode = sequence.toString()
        resolveButtonLoginState()
    }

    fun fireCaptchaEdit(s: CharSequence?) {
        captcha = s.toString()
        resolveButtonLoginState()
    }

    fun fireSaveEdit(isSave: Boolean) {
        savePassword = isSave
    }

    fun fireButtonSendCodeViaSmsClick() {
        doLogin(true)
    }

    fun fireAppCodeEdit(s: CharSequence?) {
        appCode = s.toString()
        resolveButtonLoginState()
    }

    private class CaptchaLegacy(val captchaSid: String, val captchaImg: String)

    private class VKIdCaptcha(
        val redirect_uri: String,
        val domain: String,
        var success_token: String?
    )
}