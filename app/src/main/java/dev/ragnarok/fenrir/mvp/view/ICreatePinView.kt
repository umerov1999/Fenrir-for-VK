package dev.ragnarok.fenrir.mvp.view

import androidx.annotation.StringRes
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface ICreatePinView : IMvpView, IErrorView {
    fun displayTitle(@StringRes titleRes: Int)
    fun displayErrorAnimation()
    fun displayPin(value: IntArray, noValue: Int)
    fun sendSuccessAndClose(values: IntArray)
}