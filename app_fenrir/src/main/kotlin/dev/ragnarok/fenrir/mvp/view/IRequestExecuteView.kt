package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IRequestExecuteView : IMvpView, IErrorView, IProgressView, IAccountDependencyView,
    IToastView {
    fun displayBody(body: String?)
    fun hideKeyboard()
    fun requestWriteExternalStoragePermission()
}