package dev.ragnarok.fenrir.dialog.directauth

import android.os.Bundle
import androidx.annotation.StringRes
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.Includes.networkInterfaces
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.Auth
import dev.ragnarok.fenrir.api.exceptions.NeedValidationException
import dev.ragnarok.fenrir.api.exceptions.VKIdCaptchaNeedException
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemGetVerificationMethods
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemProfile
import dev.ragnarok.fenrir.fragment.base.RxSupportPresenter
import dev.ragnarok.fenrir.model.AuthFlow
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.service.ErrorLocalizer
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
    private var currentSid: String? = null
    private var validateFlow: String? = null
    private var validateMethods: ArrayList<EcosystemGetVerificationMethods.Method>? = null
    private var validateMethod: EcosystemGetVerificationMethods.Method? = null
    private var backToPasswordOnError = false
    private var requiredVKIdCaptcha: VKIdCaptcha? = null
    private var redirectUrl: String? = null

    @AuthFlow
    private var currentFlow: Int = AuthFlow.VALIDATE_ACCOUNT

    override fun onGuiCreated(viewHost: IDirectAuthIdVKView) {
        super.onGuiCreated(viewHost)
        resolveFlow()
        resolveProfile()
        resolveInfoOrErrorMessage()
        resolveLoadingViews()
    }

    fun fireVKIdCaptchaSuccess(token: String?) {
        requiredVKIdCaptcha?.successToken = token
    }

    private fun resolveFlow(@AuthFlow flow: Int) {
        currentFlow = flow
        if (currentFlow == AuthFlow.PASSWORD) {
            backToPasswordOnError = true
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
                view?.setCodeRootVisible(false)
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
                view?.setNextFlowButtonParams(username.trimmedNonNullNoEmpty(), R.string.next)
            }

            AuthFlow.SELECT_VALIDATION_METHOD -> {
                view?.setNextFlowButtonParams(true, R.string.next)
            }

            AuthFlow.CODE_VALIDATION -> {
                view?.setNextFlowButtonParams(
                    true,
                    if (validationCode.trimmedIsNullOrEmpty()) R.string.retry else R.string.next
                )
            }

            AuthFlow.PASSWORD -> {
                view?.setNextFlowButtonParams(
                    password.trimmedNonNullNoEmpty(),
                    R.string.next
                )
            }

            AuthFlow.DO_AUTH -> {
                view?.setNextFlowButtonParams(
                    true,
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
    }

    private fun resolveLoadingViews() {
        view?.displayLoading(isLoadingNow)
    }

    private fun resolveProfile() {
        view?.updateAuthProfile(authProfile)
    }

    fun fireUserNameEdit(sequence: CharSequence?) {
        username = sequence.toString()
        resolveNextFlowButton()
    }

    fun firePasswordEdit(s: CharSequence?) {
        password = s.toString()
        resolveNextFlowButton()
        resolveSavePasswordVisibility()
    }

    fun fireValidationCodeEdit(s: CharSequence?) {
        validationCode = s.toString()
        resolveNextFlowButton()
    }

    fun fireSelectValidationMethod(index: Int) {
        validateMethod = validateMethods?.get(index)
    }

    private fun doValidateAccount() {
        clearInfoError()
        validateFlow = null
        val anonymTokenData = Settings.get().accounts().anonymToken
        val anonymToken = anonymTokenData.token
        val tmpUsername = username
        if (anonymToken.isNullOrEmpty() || !anonymTokenData.isValid()) {
            setInfoOrErrorMessage(R.string.anonym_token_not_valid, true)
            return
        }
        if (tmpUsername.isNullOrEmpty()) {
            return
        }
        setLoadingNow(true)
        appendJob(
            networker.vkAuth()
                .validateAccount(
                    apiId = Constants.API_ID,
                    supportedWays = "push,email,sms,callreset,password,reserve_code,codegen",
                    login = tmpUsername.trim(),
                    forcePassword = false,
                    passkeySupported = false,
                    sakVersion = Constants.VK_ANDROID_APP_SAK_VERSION,
                    flowType = "auth_without_password",
                    accessToken = anonymToken,
                    v = Constants.AUTH_API_VERSION
                ).fromIOToMain({
                    val nextStep = it.nextStep
                    validateFlow = it.flowName
                    if (it.flowName == "need_registration") {
                        setLoadingNow(false)
                        setInfoOrErrorMessage(
                            R.string.account_not_registered, true
                        )
                        return@fromIOToMain
                    } else if (nextStep == null || nextStep.verificationMethod == "password") {
                        validateMethod = EcosystemGetVerificationMethods.Method()
                            .setName("password")
                        setLoadingNow(false)
                        currentSid = it.sid
                        resolveFlow(AuthFlow.PASSWORD)
                        return@fromIOToMain
                    }
                    currentSid = it.sid

                    if (nextStep.hasAnotherVerificationMethods) {
                        appendJob(
                            networker.vkAuth().getEcosystemVerificationMethods(
                                apiId = Constants.API_ID,
                                sid = it.sid,
                                accessToken = anonymToken,
                                v = Constants.AUTH_API_VERSION
                            ).fromIOToMain({ verifications ->
                                setLoadingNow(false)
                                validateMethods = ArrayList(verifications.methods?.size ?: 1)
                                verifications.methods?.let { c -> validateMethods?.addAll(c) }
                                var hasMethod = false
                                for (i in validateMethods.orEmpty()) {
                                    if (i.name == nextStep.verificationMethod) {
                                        hasMethod = true
                                        break
                                    }
                                }
                                validateMethods?.sortBy { d ->
                                    d.priority
                                }
                                if (!hasMethod) {
                                    validateMethods?.add(
                                        0, EcosystemGetVerificationMethods.Method()
                                            .setName(nextStep.verificationMethod)
                                    )
                                }
                                if (validateMethods?.size == 1) {
                                    validateMethod = validateMethods?.get(0)
                                    resolveFlow(AuthFlow.CODE_VALIDATION)
                                } else {
                                    validateMethod = EcosystemGetVerificationMethods.Method()
                                        .setName(nextStep.verificationMethod)
                                    setInfoOrErrorMessage(R.string.sms_auth_help, false)
                                    resolveFlow(AuthFlow.SELECT_VALIDATION_METHOD)
                                }
                            }, { vErr ->
                                vErr.printStackTrace()
                                setLoadingNow(false)
                                validateMethod = EcosystemGetVerificationMethods.Method()
                                    .setName(nextStep.verificationMethod)
                                resolveFlow(AuthFlow.CODE_VALIDATION)
                            })
                        )
                    } else {
                        validateMethod = EcosystemGetVerificationMethods.Method()
                            .setName(nextStep.verificationMethod)
                        resolveFlow(AuthFlow.CODE_VALIDATION)
                    }
                }, {
                    setLoadingNow(false)
                    setInfoOrErrorMessage(
                        ErrorLocalizer.localizeThrowable(
                            Includes.provideApplicationContext(),
                            it
                        ), true
                    )
                })
        )
    }

    private fun doSelectValidationMethod() {
        clearInfoError()
        when (validateMethod?.name) {
            "password" -> {
                resolveFlow(AuthFlow.PASSWORD)
            }

            "reserve_code" -> {
                setInfoOrErrorMessage(R.string.auth_validate_reserve_code_help, false)
                resolveFlow(AuthFlow.CODE_VALIDATION)
            }

            "codegen" -> {
                setInfoOrErrorMessage(R.string.auth_validate_codegen_help, false)
                resolveFlow(AuthFlow.CODE_VALIDATION)
            }

            else -> {
                val anonymTokenData = Settings.get().accounts().anonymToken
                val anonymToken = anonymTokenData.token
                if (anonymToken.isNullOrEmpty() || !anonymTokenData.isValid()) {
                    setInfoOrErrorMessage(R.string.anonym_token_not_valid, true)
                    return
                }

                setLoadingNow(true)
                appendJob(
                    networker.vkAuth().sendEcosystemOtp(
                        apiId = Constants.API_ID,
                        sid = currentSid,
                        accessToken = anonymToken,
                        v = Constants.AUTH_API_VERSION,
                        suffix = validateMethod?.name ?: return
                    ).fromIOToMain({
                        setLoadingNow(false)
                        it.sid.nonNullNoEmpty { sid ->
                            currentSid = sid
                        }
                        when (validateMethod?.name) {
                            "push" -> setInfoOrErrorMessageParams(
                                R.string.auth_validate_push_help,
                                false,
                                it.info
                            )

                            "email" -> setInfoOrErrorMessageParams(
                                R.string.auth_validate_email_help,
                                false,
                                it.info
                            )

                            "sms" -> setInfoOrErrorMessageParams(
                                R.string.auth_validate_sms_help,
                                false,
                                it.info
                            )

                            "callreset" -> setInfoOrErrorMessageParams(
                                R.string.auth_validate_callreset_help,
                                false,
                                it.info,
                                it.codeLength
                            )

                            else -> {
                                setInfoOrErrorMessage(validateMethod?.name, true)
                            }
                        }
                        resolveFlow(AuthFlow.CODE_VALIDATION)
                    }, {
                        setLoadingNow(false)
                        setInfoOrErrorMessage(
                            ErrorLocalizer.localizeThrowable(
                                Includes.provideApplicationContext(),
                                it
                            ), true
                        )
                    })
                )
            }
        }
    }

    private fun doCodeValidation() {
        if (validationCode.trimmedIsNullOrEmpty()) {
            setInfoOrErrorMessage(R.string.sms_auth_help, false)
            resolveFlow(AuthFlow.SELECT_VALIDATION_METHOD)
        } else {
            clearInfoError()
            val anonymTokenData = Settings.get().accounts().anonymToken
            val anonymToken = anonymTokenData.token
            if (anonymToken.isNullOrEmpty() || !anonymTokenData.isValid()) {
                setInfoOrErrorMessage(R.string.anonym_token_not_valid, true)
                return
            }
            val method = validateMethod?.name
            val tmpCode = validationCode
            if (method.trimmedIsNullOrEmpty() || tmpCode.trimmedIsNullOrEmpty()) {
                return
            }

            setLoadingNow(true)
            appendJob(
                networker.vkAuth().checkEcosystemOtp(
                    apiId = Constants.API_ID,
                    sid = currentSid,
                    accessToken = anonymToken,
                    v = Constants.AUTH_API_VERSION,
                    verificationMethod = method,
                    code = tmpCode.trim()
                ).fromIOToMain({
                    setLoadingNow(false)
                    it.sid.nonNullNoEmpty { sid ->
                        currentSid = sid
                    }
                    authProfile = it.profile
                    if (authProfile?.photo200.isNullOrEmpty()) {
                        authProfile?.photo200 = "https://vk.ru/images/camera_200.png"
                    }
                    resolveProfile()
                    if (validateFlow != "need_password_and_validation" || it.canSkipPassword) {
                        resolveFlow(AuthFlow.DO_AUTH)
                    } else {
                        resolveFlow(AuthFlow.PASSWORD)
                    }
                }, {
                    setLoadingNow(false)
                    setInfoOrErrorMessage(
                        ErrorLocalizer.localizeThrowable(
                            Includes.provideApplicationContext(),
                            it
                        ), true
                    )
                })
            )
        }
    }

    private fun doAuth() {
        clearInfoError()
        val anonymTokenData = Settings.get().accounts().anonymToken
        val anonymToken = anonymTokenData.token
        if (anonymToken.isNullOrEmpty() || !anonymTokenData.isValid()) {
            setInfoOrErrorMessage(R.string.anonym_token_not_valid, true)
            return
        }

        val grantType = when {
            validateMethod?.name == "password" -> "password"
            backToPasswordOnError -> "phone_confirmation_sid"
            else -> "without_password"
        }
        setLoadingNow(true)
        appendJob(
            networker.vkDirectAuth().directLogin(
                grantType = grantType,
                clientId = Constants.API_ID,
                username = username?.trim(),
                password = password?.trim(),
                v = Constants.AUTH_API_VERSION,
                twoFaSupported = true,
                scope = Auth.scope,
                captchaSuccessToken = requiredVKIdCaptcha?.successToken,
                libVerifySupport = false,
                sid = currentSid,
                anonymousToken = anonymToken,
                sakVersion = Constants.VK_ANDROID_APP_SAK_VERSION,
                flowType = "tg_flow"
            ).fromIOToMain({
                setLoadingNow(false)
                var twoFa = "yes"
                for (i in validateMethods.orEmpty()) {
                    if (i.name == "password") {
                        twoFa = "no"
                        break
                    }
                }
                view?.returnSuccessToParent(
                    userId = it.user_id,
                    accessToken = it.access_token,
                    login = if (username.nonNullNoEmpty()) username?.trim() else "",
                    password = password,
                    twoFA = twoFa,
                    isSave = needSavePassword
                )
            }, {
                setLoadingNow(false)

                if (it is VKIdCaptchaNeedException) {
                    requiredVKIdCaptcha = VKIdCaptcha(it.redirect_uri, it.domain, null)
                    view?.openVKIdCaptcha(
                        requiredVKIdCaptcha?.redirectUri,
                        requiredVKIdCaptcha?.domain
                    )
                } else if (it is NeedValidationException) {
                    setInfoOrErrorMessage(
                        ErrorLocalizer.localizeThrowable(
                            Includes.provideApplicationContext(),
                            it
                        ), true
                    )
                    redirectUrl = it.validationURL
                    if (!redirectUrl.isNullOrEmpty()) {
                        view?.returnSuccessValidation(
                            redirectUrl,
                            if (username.nonNullNoEmpty()) username?.trim() else "",
                            if (password.nonNullNoEmpty()) password?.trim() else "",
                            "web_validation",
                            needSavePassword
                        )
                    }
                } else {
                    setInfoOrErrorMessage(
                        ErrorLocalizer.localizeThrowable(
                            Includes.provideApplicationContext(),
                            it
                        ), true
                    )
                    if (backToPasswordOnError) {
                        resolveFlow(AuthFlow.PASSWORD)
                    }
                }
            })
        )
    }

    fun fireNextFlowClick() {
        when (currentFlow) {
            AuthFlow.VALIDATE_ACCOUNT -> {
                doValidateAccount()
            }

            AuthFlow.CODE_VALIDATION -> {
                doCodeValidation()
            }

            AuthFlow.SELECT_VALIDATION_METHOD -> {
                doSelectValidationMethod()
            }

            AuthFlow.PASSWORD -> {
                resolveFlow(AuthFlow.DO_AUTH)
            }

            AuthFlow.DO_AUTH -> {
                doAuth()
            }
        }
    }

    fun fireSavePasswordChanged(isSave: Boolean) {
        needSavePassword = isSave
    }

    private class VKIdCaptcha(
        val redirectUri: String,
        val domain: String,
        var successToken: String?
    )
}