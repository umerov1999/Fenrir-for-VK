package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.mvp.core.IMvpView

interface IAddProxyView : IMvpView, IErrorView {
    fun setAuthFieldsEnabled(enabled: Boolean)
    fun setAuthChecked(checked: Boolean)
    fun goBack()
}