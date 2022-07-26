package dev.ragnarok.fenrir.mvp.view

import androidx.annotation.StringRes

interface ISnackbarView {
    fun showSnackbar(@StringRes res: Int, isLong: Boolean)
}