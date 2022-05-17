package dev.ragnarok.fenrir.mvp.view

import androidx.annotation.StringRes

interface IProgressView {
    fun displayProgressDialog(@StringRes title: Int, @StringRes message: Int, cancelable: Boolean)
    fun dismissProgressDialog()
}