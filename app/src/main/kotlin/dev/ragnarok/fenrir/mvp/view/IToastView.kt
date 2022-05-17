package dev.ragnarok.fenrir.mvp.view

import androidx.annotation.StringRes
import dev.ragnarok.fenrir.util.CustomToast

interface IToastView {
    fun showToast(@StringRes titleTes: Int, isLong: Boolean, vararg params: Any?)
    val customToast: CustomToast?
}