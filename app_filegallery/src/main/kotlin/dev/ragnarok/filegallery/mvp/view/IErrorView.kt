package dev.ragnarok.filegallery.mvp.view

import androidx.annotation.StringRes

interface IErrorView {
    fun showError(errorText: String?)
    fun showThrowable(throwable: Throwable?)
    fun showError(@StringRes titleTes: Int, vararg params: Any?)
}
