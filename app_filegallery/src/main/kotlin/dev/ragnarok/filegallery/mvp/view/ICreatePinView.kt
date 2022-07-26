package dev.ragnarok.filegallery.mvp.view

import androidx.annotation.StringRes
import dev.ragnarok.filegallery.mvp.core.IMvpView

interface ICreatePinView : IMvpView, IErrorView {
    fun displayTitle(@StringRes titleRes: Int)
    fun displayErrorAnimation()
    fun displayPin(value: IntArray, noValue: Int)
    fun sendSuccessAndClose(values: IntArray)
}