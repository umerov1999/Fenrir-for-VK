package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.mvp.core.IMvpView

interface IEnterPinView : IMvpView, IErrorView, IToastView {
    fun displayPin(value: IntArray, noValue: Int)
    fun sendSuccessAndClose()
    fun displayErrorAnimation()
    fun displayAvatarFromUrl(url: String?)
    fun displayDefaultAvatar()
    fun showBiometricPrompt()
}