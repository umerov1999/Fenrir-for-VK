package dev.ragnarok.filegallery.mvp.view

import dev.ragnarok.filegallery.mvp.core.IMvpView

interface IEnterPinView : IMvpView, IErrorView, IToastView {
    fun displayPin(value: IntArray, noValue: Int)
    fun sendSuccessAndClose()
    fun displayErrorAnimation()
    fun showBiometricPrompt()
}