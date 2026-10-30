package dev.ragnarok.fenrir.dialog.directauth

import android.os.Bundle
import androidx.annotation.StringRes
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.Includes.networkInterfaces
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.Auth
import dev.ragnarok.fenrir.api.auth.AccountCaptchaChallenge
import dev.ragnarok.fenrir.api.auth.AuthCaptchaAnswer
import dev.ragnarok.fenrir.api.auth.AuthFailurePolicy
import dev.ragnarok.fenrir.api.auth.VkIdSession
import dev.ragnarok.fenrir.api.exceptions.ApiException
import dev.ragnarok.fenrir.api.exceptions.AuthException
import dev.ragnarok.fenrir.api.exceptions.NeedValidationException
import dev.ragnarok.fenrir.api.exceptions.VKIdCaptchaNeedException
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.VKApiUser
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemGetVerificationMethods
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemProfile
import dev.ragnarok.fenrir.fragment.base.RxSupportPresenter
import dev.ragnarok.fenrir.model.AuthFlow
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.trimmedIsNullOrEmpty
import dev.ragnarok.fenrir.trimmedNonNullNoEmpty
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain

class DirectAuthIdVKPresenter(savedInstanceState: Bundle?) :
    RxSupportPresenter<IDirectAuthIdVKView>(savedInstanceState) {
    private val networker: INetworker = networkInterfaces

    private var username: String? = null
    private var password: String? = null
    private var validationCode: String? = null
    private var needSavePassword = false
    private var authProfile: EcosystemProfile? = null

    private var currentInfoMessage: String? = null
    private var currentInfoIsError: Boolean = false
    private var isLoadingNow: Boolean = false
    private val session = VkIdSession()
    private var validateFlow: String? = null
    private var validateMethods: ArrayList<EcosystemGetVerificationMethods.Method>? = null
    private var validateMethod: EcosystemGetVerificationMethods.Method? = null
    private var requiresPassword = false
    private var requiredVKIdCaptcha: VKIdCaptcha? = null
    private var accountCaptcha: AccountCaptchaChallenge? = null
    private var accountCaptchaAnswer: AuthCaptchaAnswer? = null
    private var accountCaptchaDiagnostic: String? = null
    private var redirectUrl: String? = null
    private val tokenNeedCode get() = session.needsTokenCode

    @AuthFlow
    private var currentFlow: Int = AuthFlow.VALIDATE_ACCOUNT

    override fun onGuiCreated(viewHost: IDirectAuthIdVKView) {
        super.onGuiCreated(viewHost)
        resolveFlow()
        resolveProfile()
        resolveInfoOrErrorMessage()
        resolveLoadingViews()
    }

    fun fireVKIdCaptchaSuccess(request: VkIdSession.Request, token: String?) {
        val challenge = accountCaptcha
        if (challenge != null) {
            accountCaptchaAnswer = challenge.webAnswer(request, token) ?: return
            setInfoOrErrorMessage(
                getString(R.string.auth_captcha_ready) + "\n" + accountCaptchaDiagnostic.orEmpty(),
                false
            )
        } else {
            val pending = requiredVKIdCaptcha?.takeIf { it.request === request } ?: return
            pending.successToken = token?.takeIf { it.isNotBlank() } ?: return
            setInfoOrErrorMessage(R.string.auth_captcha_ready, false)
        }
        // Completion only stores the answer. The user must press Next/Login to submit it.
    }

    fun fireImageCaptchaSuccess(request: VkIdSession.Request, key: String?) {
        accountCaptchaAnswer = accountCaptcha?.imageAnswer(request, key) ?: return
        setInfoOrErrorMessage(
            getString(R.string.auth_captcha_ready) + "\n" + accountCaptchaDiagnostic.orEmpty(),
            false
        )
    }

    private fun resolveFlow(@AuthFlow flow: Int) {
        currentFlow = flow
        if (currentFlow == AuthFlow.PASSWORD) {
            requiresPassword = true
        }
        view?.hideKeyboard()
        resolveFlow()
    }

    private fun resolveFlow() {
        resolveSavePasswordVisibility()
        resolveNextFlowButton()
        when (currentFlow) {
            AuthFlow.VALIDATE_ACCOUNT -> {
                view?.setValidationMethods(false, null)
                view?.setPasswordRootVisible(false)
                view?.setCodeRootVisible(false)
                view?.setUserNameRootVisible(true)
            }

            AuthFlow.SELECT_VALIDATION_METHOD -> {
                view?.setUserNameRootVisible(false)
                view?.setPasswordRootVisible(false)
                view?.setCodeRootVisible(false)
                view?.setValidationMethods(true, validateMethods)
            }

            AuthFlow.CODE_VALIDATION -> {
                view?.setValidationMethods(false, null)
                view?.setUserNameRootVisible(false)
                view?.setPasswordRootVisible(false)
                view?.setCodeRootVisible(true)
            }

            AuthFlow.PASSWORD -> {
                view?.setValidationMethods(false, null)
                view?.setUserNameRootVisible(false)
                view?.setCodeRootVisible(false)
                view?.setPasswordRootVisible(true)
            }

            AuthFlow.DO_AUTH -> {
                view?.setValidationMethods(false, null)
                view?.setUserNameRootVisible(false)
                view?.setCodeRootVisible(tokenNeedCode)
                view?.setPasswordRootVisible(false)
            }
        }
    }

    private fun resolveSavePasswordVisibility() {
        view?.setSavePasswordVisible(currentFlow == AuthFlow.PASSWORD && password.trimmedNonNullNoEmpty())
    }

    private fun resolveNextFlowButton() {
        when (currentFlow) {
            AuthFlow.VALIDATE_ACCOUNT -> {
                view?.setNextFlowButtonParams(
                    !isLoadingNow && !session.busy && (!session.blocked || username.trimmedIsNullOrEmpty()),
                    if (username.trimmedIsNullOrEmpty()) R.string.cancel else R.string.next
                )
            }

            AuthFlow.SELECT_VALIDATION_METHOD -> {
                view?.setNextFlowButtonParams(
                    !isLoadingNow && !session.busy && !session.blocked,
                    R.string.next
                )
            }

            AuthFlow.CODE_VALIDATION -> {
                view?.setNextFlowButtonParams(
                    !isLoadingNow && !session.busy && !session.blocked,
                    if (validationCode.trimmedIsNullOrEmpty()) R.string.retry else R.string.next
                )
            }

            AuthFlow.PASSWORD -> {
                view?.setNextFlowButtonParams(
                    password.trimmedNonNullNoEmpty() && !isLoadingNow && !session.busy && !session.blocked,
                    R.string.next
                )
            }

            AuthFlow.DO_AUTH -> {
                view?.setNextFlowButtonParams(
                    !isLoadingNow && !session.busy && !session.blocked,
                    R.string.button_login
                )
            }
        }
    }

    private fun resolveInfoOrErrorMessage() {
        view?.onSetInfoOrErrorMessage(currentInfoMessage, currentInfoIsError)
    }

    private fun setInfoOrErrorMessage(message: String?, isError: Boolean) {
        currentInfoMessage = message
        currentInfoIsError = isError
        resolveInfoOrErrorMessage()
    }

    private fun setInfoOrErrorMessage(@StringRes message: Int, isError: Boolean) {
        setInfoOrErrorMessage(Includes.provideApplicationContext().getString(message), isError)
    }

    private fun setInfoOrErrorMessageParams(
        @StringRes message: Int,
        isError: Boolean,
        vararg args: Any?
    ) {
        setInfoOrErrorMessage(
            Includes.provideApplicationContext().getString(message, *args),
            isError
        )
    }

    private fun clearInfoError() {
        currentInfoMessage = null
        currentInfoIsError = false
        resolveInfoOrErrorMessage()
    }

    private fun setLoadingNow(loading: Boolean) {
        isLoadingNow = loading
        if (loading) {
            view?.hideKeyboard()
        }
        resolveLoadingViews()
        resolveNextFlowButton()
    }

    private fun resolveLoadingViews() {
        view?.displayLoading(isLoadingNow)
    }

    private fun resolveProfile() {
        view?.updateAuthProfile(authProfile)
    }

    fun fireUserNameEdit(sequence: CharSequence?) {
        val next = sequence?.toString()
        if (username != next && (session.busy || session.phase != VkIdSession.Phase.EMPTY || accountCaptcha != null)) {
            compositeJob.clear()
            restartAuthFlow(null)
        }
        username = next
        resolveNextFlowButton()
    }

    fun firePasswordEdit(s: CharSequence?) {
        if (password != s?.toString() && session.busy) {
            compositeJob.clear()
            restartAuthFlow(null)
        }
        password = s?.toString()
        resolveNextFlowButton()
        resolveSavePasswordVisibility()
    }

    fun fireValidationCodeEdit(s: CharSequence?) {
        validationCode = s?.toString()
        resolveNextFlowButton()
    }

    fun fireSelectValidationMethod(index: Int) {
        if (!session.busy) validateMethod = validateMethods?.getOrNull(index)
    }

    private var attemptToken: dev.ragnarok.fenrir.settings.AnonymToken? = null
    private var attemptUsername: String? = null

    private fun start(request: VkIdSession.Request) {
        clearInfoError()
        setLoadingNow(true)
        VkIdDiagnostics.record(
            request,
            VkIdDiagnostics.Event.START,
            session,
            verificationMethod = validateMethod?.name
        )
    }

    private fun current(request: VkIdSession.Request): Boolean {
        if (session.isCurrent(request)) return true
        VkIdDiagnostics.record(
            request,
            VkIdDiagnostics.Event.STALE,
            session,
            verificationMethod = validateMethod?.name
        )
        return false
    }

    private fun token(): String? {
        if (attemptToken?.isValid() == true) return attemptToken?.token
        restartAuthFlow(getString(R.string.anonym_token_not_valid))
        return null
    }

    private fun doValidateAccount() {
        val login = username?.trim()
        if (login.isNullOrEmpty()) {
            view?.cancelAuth(); return
        }
        if (accountCaptcha != null && accountCaptchaAnswer == null) {
            showAccountCaptcha()
            return
        }
        // A solved challenge stays bound to the original phone and anonymous token.
        val anonymous =
            if (accountCaptcha != null) attemptToken else Settings.get().accounts().anonymToken
        if (anonymous?.isValid() != true) {
            restartAuthFlow(getString(R.string.anonym_token_not_valid))
            return
        }
        val request = session.beginAccount() ?: return
        val captchaAnswer = accountCaptchaAnswer
        accountCaptcha = null
        accountCaptchaAnswer = null
        accountCaptchaDiagnostic = null
        attemptToken = anonymous
        attemptUsername = login
        validateFlow = null
        validateMethods = null
        validateMethod = null
        requiresPassword = false
        validationCode = null
        requiredVKIdCaptcha = null
        authProfile = null
        view?.cleanCode()
        resolveProfile()
        start(request)
        appendJob(
            networker.vkAuth().validateAccount(
                apiId = Constants.API_ID,
                supportedWays = "push,email,sms,callreset,password,reserve_code,codegen",
                login = login,
                forcePassword = false,
                passkeySupported = false,
                sakVersion = Constants.VK_ANDROID_APP_SAK_VERSION,
                flowType = "auth_without_password",
                accessToken = anonymous.token,
                v = Constants.AUTH_API_VERSION,
                captcha = captchaAnswer
            ).fromIOToMain({ result ->
                if (!current(request)) return@fromIOToMain
                session.account(request, result.sid)
                VkIdDiagnostics.record(
                    request,
                    VkIdDiagnostics.Event.SUCCESS,
                    session,
                    verificationMethod = validateMethod?.name
                )
                setLoadingNow(false)
                validateFlow = result.flowName
                if (result.flowName == "need_registration") {
                    restartAuthFlow(getString(R.string.account_not_registered))
                    return@fromIOToMain
                }
                val next = result.nextStep
                validateMethod = EcosystemGetVerificationMethods.Method()
                    .setName(next?.verificationMethod ?: "password")
                if (validateMethod?.name == "password") resolveFlow(AuthFlow.PASSWORD)
                else if (next?.hasAnotherVerificationMethods == true) doGetValidationMethods()
                else doSelectValidationMethod()
            }, { handleFailure(request, it) })
        )
    }

    private fun doGetValidationMethods() {
        val anonymous = token() ?: return
        val request = try {
            session.begin(VkIdSession.Operation.METHODS)
        } catch (_: VkIdSession.MissingSession) {
            restartAuthFlow(getString(R.string.auth_session_expired)); return
        }
            ?: return
        val preferred = validateMethod?.name
        start(request)
        appendJob(
            networker.vkAuth().getEcosystemVerificationMethods(
                Constants.API_ID, request.sid, anonymous, Constants.AUTH_API_VERSION
            ).fromIOToMain({ result ->
                if (!current(request)) return@fromIOToMain
                session.finish(request)
                VkIdDiagnostics.record(
                    request,
                    VkIdDiagnostics.Event.SUCCESS,
                    session,
                    verificationMethod = validateMethod?.name
                )
                validateMethods =
                    ArrayList(result.methods.orEmpty().filter { supportedMethod(it.name) }
                        .sortedBy { it.priority })
                if (supportedMethod(preferred) && validateMethods.orEmpty()
                        .none { it.name == preferred }
                )
                    validateMethods?.add(
                        0,
                        EcosystemGetVerificationMethods.Method().setName(preferred)
                    )
                setLoadingNow(false)
                if (validateMethods?.size == 1) {
                    validateMethod = validateMethods?.first()
                    doSelectValidationMethod()
                } else if (!validateMethods.isNullOrEmpty()) {
                    setInfoOrErrorMessage(R.string.sms_auth_help, false)
                    resolveFlow(AuthFlow.SELECT_VALIDATION_METHOD)
                } else restartAuthFlow(getString(R.string.auth_invalid_response))
            }, { handleFailure(request, it) })
        )
    }

    private fun supportedMethod(method: String?) = method in setOf(
        "password", "push", "email", "sms", "callreset", "reserve_code", "codegen"
    )

    private fun doSelectValidationMethod() {
        val method = validateMethod?.name
        clearInfoError()
        when (method) {
            "password" -> resolveFlow(AuthFlow.PASSWORD)
            "reserve_code", "codegen" -> {
                setInfoOrErrorMessage(
                    if (method == "reserve_code") R.string.auth_validate_reserve_code_help
                    else R.string.auth_validate_codegen_help, false
                )
                resolveFlow(AuthFlow.CODE_VALIDATION)
            }

            "push", "email", "sms", "callreset" -> {
                val anonymous = token() ?: return
                val request = try {
                    session.begin(VkIdSession.Operation.SEND_OTP)
                } catch (_: VkIdSession.MissingSession) {
                    restartAuthFlow(getString(R.string.auth_session_expired)); return
                }
                    ?: return
                start(request)
                appendJob(
                    networker.vkAuth().sendEcosystemOtp(
                        Constants.API_ID, request.sid, anonymous, Constants.AUTH_API_VERSION, method
                    ).fromIOToMain({ result ->
                        if (!current(request)) return@fromIOToMain
                        session.otpSent(request, result.sid)
                        VkIdDiagnostics.record(
                            request,
                            VkIdDiagnostics.Event.SUCCESS,
                            session,
                            verificationMethod = validateMethod?.name
                        )
                        setLoadingNow(false)
                        validationCode = null
                        view?.cleanCode()
                        when (method) {
                            "push" -> setInfoOrErrorMessageParams(
                                R.string.auth_validate_push_help,
                                false,
                                result.info
                            )

                            "email" -> setInfoOrErrorMessageParams(
                                R.string.auth_validate_email_help,
                                false,
                                result.info
                            )

                            "sms" -> setInfoOrErrorMessageParams(
                                R.string.auth_validate_sms_help,
                                false,
                                result.info
                            )

                            "callreset" -> setInfoOrErrorMessageParams(
                                R.string.auth_validate_callreset_help,
                                false,
                                result.info,
                                result.codeLength
                            )
                        }
                        resolveFlow(AuthFlow.CODE_VALIDATION)
                    }, { handleFailure(request, it) })
                )
            }

            else -> restartAuthFlow(getString(R.string.auth_invalid_response))
        }
    }

    private fun doCodeValidation() {
        if (validationCode.trimmedIsNullOrEmpty()) {
            setInfoOrErrorMessage(R.string.sms_auth_help, false)
            resolveFlow(AuthFlow.SELECT_VALIDATION_METHOD)
            return
        }
        val anonymous = token() ?: return
        val method = validateMethod?.name?.takeIf { supportedMethod(it) } ?: return
        val code = validationCode?.trim() ?: return
        val request = try {
            session.begin(VkIdSession.Operation.CHECK_OTP)
        } catch (_: VkIdSession.MissingSession) {
            restartAuthFlow(getString(R.string.auth_session_expired)); return
        }
            ?: return
        start(request)
        appendJob(
            networker.vkAuth().checkEcosystemOtp(
                Constants.API_ID, request.sid, anonymous, Constants.AUTH_API_VERSION, method, code
            ).fromIOToMain({ result ->
                if (!current(request)) return@fromIOToMain
                try {
                    session.confirmed(request, result.sid)
                } catch (error: VkIdSession.MissingSession) {
                    handleFailure(request, error); return@fromIOToMain
                }
                VkIdDiagnostics.record(
                    request,
                    VkIdDiagnostics.Event.SUCCESS,
                    session,
                    verificationMethod = validateMethod?.name
                )
                validationCode = null
                view?.cleanCode()
                authProfile = result.profile
                if (authProfile?.photo200.isNullOrEmpty()) authProfile?.photo200 =
                    VKApiUser.getPhotoLink200()
                resolveProfile()
                setLoadingNow(false)
                requiresPassword =
                    validateFlow == "need_password_and_validation" && !result.canSkipPassword
                resolveFlow(if (requiresPassword) AuthFlow.PASSWORD else AuthFlow.DO_AUTH)
            }, { handleFailure(request, it) })
        )
    }

    private fun restartAuthFlow(message: String?) {
        session.reset()
        attemptToken = null
        attemptUsername = null
        validateFlow = null
        validateMethods = null
        validateMethod = null
        validationCode = null
        requiresPassword = false
        requiredVKIdCaptcha = null
        accountCaptcha = null
        accountCaptchaAnswer = null
        accountCaptchaDiagnostic = null
        redirectUrl = null
        authProfile = null
        setLoadingNow(false)
        view?.cleanCode()
        resolveProfile()
        resolveFlow(AuthFlow.VALIDATE_ACCOUNT)
        setInfoOrErrorMessage(message, true)
    }

    private fun handleFailure(request: VkIdSession.Request, thrown: Throwable) {
        if (!current(request)) return
        val error = VkIdDiagnostics.cause(thrown)
        val diagnostic = VkIdDiagnostics.record(
            request,
            VkIdDiagnostics.Event.ERROR,
            session,
            error,
            validateMethod?.name
        )
        val kind = VkIdDiagnostics.kind(error)
        val captcha = kind == AuthFailurePolicy.Kind.CAPTCHA
        val uncertain = error !is AuthException && error !is ApiException && !captcha
        session.failed(request, explicitCaptcha = captcha, uncertain = uncertain)
        setLoadingNow(false)
        when (val recovery = AuthFailurePolicy.recovery(kind, session.phase)) {
            AuthFailurePolicy.Recovery.BLOCK -> {
                session.block((error as? dev.ragnarok.fenrir.api.rest.HttpException)?.retryAfterSeconds)
                restartAuthFlow(getString(R.string.auth_temporarily_limited) + "\n" + diagnostic)
            }

            AuthFailurePolicy.Recovery.RESTART_SESSION ->
                restartAuthFlow(getString(R.string.auth_session_expired) + "\n" + diagnostic)

            AuthFailurePolicy.Recovery.CAPTCHA -> {
                when (error) {
                    is ApiException if request.operation == VkIdSession.Operation.ACCOUNT -> {
                        accountCaptcha = AccountCaptchaChallenge(
                            request, error.error.redirectUri,
                            error.error.captchaSid, error.error.captchaImg
                        )
                        accountCaptchaAnswer = null
                        accountCaptchaDiagnostic = diagnostic
                        showAccountCaptcha()
                    }

                    is VKIdCaptchaNeedException -> {
                        requiredVKIdCaptcha = VKIdCaptcha(request, error.redirect_uri)
                        view?.openVKIdCaptcha(request, error.redirect_uri, error.domain)
                        setInfoOrErrorMessage(
                            getString(R.string.auth_complete_captcha) + "\n" + diagnostic,
                            false
                        )
                    }

                    else -> setInfoOrErrorMessage(
                        getString(R.string.auth_captcha_unavailable) + "\n" + diagnostic,
                        true
                    )
                }
            }

            AuthFailurePolicy.Recovery.RESTART_REQUEST, AuthFailurePolicy.Recovery.RETRY_MANUALLY -> {
                val message = getString(
                    when (kind) {
                        AuthFailurePolicy.Kind.NETWORK -> R.string.auth_network_failed
                        AuthFailurePolicy.Kind.HTTP -> R.string.auth_http_failed
                        AuthFailurePolicy.Kind.API -> R.string.auth_api_failed
                        AuthFailurePolicy.Kind.AUTHENTICATION -> R.string.auth_credentials_rejected
                        AuthFailurePolicy.Kind.INVALID_RESPONSE -> R.string.auth_invalid_response
                        else -> R.string.auth_request_failed
                    }
                ) + "\n" + diagnostic
                if (recovery == AuthFailurePolicy.Recovery.RESTART_REQUEST) {
                    // A discarded/absent session needs a fresh attempt, without relabelling the cause as SID.
                    restartAuthFlow(message)
                    return
                }
                validationCode = null
                view?.cleanCode()
                setInfoOrErrorMessage(message, true)
                if (currentFlow == AuthFlow.DO_AUTH && !tokenNeedCode && validateMethod?.name == "password")
                    resolveFlow(AuthFlow.PASSWORD)
            }
        }
    }

    private fun showAccountCaptcha() {
        val challenge = accountCaptcha ?: return
        val available = challenge.kind != AccountCaptchaChallenge.Kind.UNAVAILABLE
        setInfoOrErrorMessage(
            getString(
                if (available) R.string.auth_complete_captcha
                else R.string.auth_captcha_unavailable
            ) + "\n" + accountCaptchaDiagnostic.orEmpty(), !available
        )
        when (challenge.kind) {
            AccountCaptchaChallenge.Kind.WEB -> view?.openVKIdCaptcha(
                challenge.request,
                challenge.redirectUri, "https://" + Settings.get().main().apiDomain
            )

            AccountCaptchaChallenge.Kind.IMAGE -> view?.openImageCaptcha(
                challenge.request,
                challenge.image.orEmpty()
            )

            AccountCaptchaChallenge.Kind.UNAVAILABLE -> Unit // No challenge to solve; never replay blindly.
        }
    }

    private fun doAuth() {
        val anonymous = token() ?: return
        if (tokenNeedCode && validationCode.trimmedIsNullOrEmpty()) {
            setInfoOrErrorMessage(R.string.auth_enter_code, true)
            return
        }
        val request = try {
            session.beginToken(validateMethod?.name == "password", requiresPassword)
        } catch (_: VkIdSession.MissingSession) {
            restartAuthFlow(getString(R.string.auth_session_expired)); return
        }
            ?: return
        start(request)
        appendJob(
            networker.vkDirectAuth().directLogin(
                grantType = request.grant, clientId = Constants.API_ID,
                username = attemptUsername, password = password,
                code = if (request.needsCode) validationCode?.trim() else null,
                v = Constants.AUTH_API_VERSION, twoFaSupported = true, scope = Auth.scope,
                captchaSuccessToken = requiredVKIdCaptcha?.successToken, libVerifySupport = false,
                sid = request.sid, anonymousToken = anonymous,
                sakVersion = Constants.VK_ANDROID_APP_SAK_VERSION, flowType = "tg_flow"
            ).fromIOToMain({ result ->
                if (!current(request)) return@fromIOToMain
                if (result.access_token.isNullOrEmpty() || result.user_id == 0L) {
                    handleFailure(request, AuthException("invalid_response", null))
                    return@fromIOToMain
                }
                session.succeeded(request)
                VkIdDiagnostics.record(
                    request,
                    VkIdDiagnostics.Event.SUCCESS,
                    session,
                    verificationMethod = validateMethod?.name
                )
                setLoadingNow(false)
                view?.returnSuccessToParent(
                    result.user_id,
                    result.access_token,
                    attemptUsername.orEmpty(),
                    password,
                    if (validateMethods.orEmpty().any { it.name == "password" }) "no" else "yes",
                    needSavePassword
                )
            }, { thrown ->
                if (!current(request)) return@fromIOToMain
                val error = VkIdDiagnostics.cause(thrown)
                if (error is NeedValidationException) handleTokenValidation(request, error)
                else handleFailure(request, error)
            })
        )
    }

    private fun handleTokenValidation(
        request: VkIdSession.Request,
        error: NeedValidationException
    ) {
        VkIdDiagnostics.record(
            request,
            VkIdDiagnostics.Event.ERROR,
            session,
            error,
            validateMethod?.name
        )
        val type = error.validationType?.lowercase()
        if (type !in setOf("2fa_sms", "2fa_callreset", "2fa_app", "2fa_push", "2fa_libverify")) {
            session.failed(request, uncertain = true)
            setLoadingNow(false)
            redirectUrl = error.validationURL
            if (!redirectUrl.isNullOrEmpty()) view?.returnSuccessValidation(
                redirectUrl,
                attemptUsername.orEmpty(),
                password.orEmpty(),
                "web_validation",
                needSavePassword
            ) else restartAuthFlow(getString(R.string.auth_invalid_response))
            return
        }
        try {
            session.challenge(request, error.sid, error.expiresAtEpochSeconds)
        } catch (missing: VkIdSession.MissingSession) {
            handleFailure(request, missing); return
        }
        validationCode = null
        requiredVKIdCaptcha = null
        view?.cleanCode()
        setInfoOrErrorMessage(R.string.auth_enter_code, false)
        resolveFlow(AuthFlow.DO_AUTH)
        if (type == "2fa_libverify") doValidatePhone() else setLoadingNow(false)
    }

    private fun doValidatePhone() {
        val request = session.begin(VkIdSession.Operation.VALIDATE_PHONE) ?: return
        start(request)
        appendJob(
            networker.vkAuth().validatePhone(
                attemptUsername, Constants.API_ID, request.sid, Constants.AUTH_API_VERSION,
                libVerifySupport = false, allowCallReset = true
            ).fromIOToMain({ result ->
                if (!current(request)) return@fromIOToMain
                session.phoneValidated(request, result.sid)
                VkIdDiagnostics.record(
                    request,
                    VkIdDiagnostics.Event.SUCCESS,
                    session,
                    verificationMethod = validateMethod?.name
                )
                setLoadingNow(false)
                if (result.validationType == "callreset") setInfoOrErrorMessageParams(
                    R.string.call_reset_description, false, result.codeLength
                ) else setInfoOrErrorMessage(R.string.auth_enter_code, false)
            }, { handleFailure(request, it) })
        )
    }

    fun fireNextFlowClick() {
        if (currentFlow == AuthFlow.VALIDATE_ACCOUNT && username.trimmedIsNullOrEmpty()) {
            view?.cancelAuth()
            return
        }
        if (isLoadingNow || session.busy || session.blocked) return
        when (currentFlow) {
            AuthFlow.VALIDATE_ACCOUNT -> doValidateAccount()
            AuthFlow.CODE_VALIDATION -> doCodeValidation()
            AuthFlow.SELECT_VALIDATION_METHOD -> doSelectValidationMethod()
            AuthFlow.PASSWORD -> resolveFlow(AuthFlow.DO_AUTH)
            AuthFlow.DO_AUTH -> doAuth()
        }
    }

    fun fireSavePasswordChanged(isSave: Boolean) {
        needSavePassword = isSave
    }

    private class VKIdCaptcha(val request: VkIdSession.Request, var successToken: String?)
}
