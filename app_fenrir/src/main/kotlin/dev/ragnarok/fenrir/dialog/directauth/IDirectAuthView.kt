package dev.ragnarok.fenrir.dialog.directauth

import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView

interface IDirectAuthView : IMvpView, IErrorView {
    fun setLoginButtonEnabled(enabled: Boolean, passwordEntered: Boolean)
    fun setSmsRootVisible(visible: Boolean)
    fun setSmsHelpVisible(visible: Boolean)
    fun setAppCodeRootVisible(visible: Boolean)
    fun moveFocusToSmsCode()
    fun moveFocusToSmsHelp()
    fun moveFocusToAppCode()
    fun displayLoading(loading: Boolean)
    fun setCaptchaLegacyRootVisible(visible: Boolean)
    fun displayCaptchaLegacyImage(img: String?)
    fun moveFocusToCaptchaLegacy()
    fun hideKeyboard()
    fun returnSuccessToParent(
        userId: Long,
        accessToken: String?,
        Login: String?,
        Password: String?,
        twoFA: String?,
        isSave: Boolean
    )

    fun returnSuccessValidation(
        url: String?,
        Login: String?,
        Password: String?,
        twoFA: String?,
        isSave: Boolean
    )

    fun returnLoginViaWebAction()

    fun startDefaultValidation(url: String?)

    fun openVKIdCaptcha(redirect_uri: String?, domain: String?)

    fun updateSmsDescription(description: String?)
    fun updateSmsHint(hint: String?)
}