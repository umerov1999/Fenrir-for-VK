package dev.ragnarok.fenrir.dialog.directauth

import androidx.annotation.StringRes
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemGetVerificationMethods
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemProfile
import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView

interface IDirectAuthIdVKView : IMvpView, IErrorView {
    fun setNextFlowButtonParams(enabled: Boolean, @StringRes text: Int)
    fun setSavePasswordVisible(visible: Boolean)
    fun displayLoading(loading: Boolean)
    fun hideKeyboard()
    fun setCodeRootVisible(visible: Boolean)
    fun setUserNameRootVisible(visible: Boolean)
    fun setValidationMethods(
        visible: Boolean,
        methods: List<EcosystemGetVerificationMethods.Method>?
    )

    fun setPasswordRootVisible(visible: Boolean)
    fun updateAuthProfile(authProfile: EcosystemProfile?)
    fun onSetInfoOrErrorMessage(currentInfoMessage: String?, currentInfoIsError: Boolean)
    fun openVKIdCaptcha(redirect_uri: String?, domain: String?)
    fun returnSuccessToParent(
        userId: Long,
        accessToken: String?,
        login: String?,
        password: String?,
        twoFA: String?,
        isSave: Boolean
    )

    fun returnSuccessValidation(
        url: String?,
        login: String?,
        password: String?,
        twoFA: String?,
        isSave: Boolean
    )

    fun cleanCode()
}