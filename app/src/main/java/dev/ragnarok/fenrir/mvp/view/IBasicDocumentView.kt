package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IBasicDocumentView : IMvpView, IAccountDependencyView, IToastView, IErrorView {
    fun shareDocument(accountId: Int, document: Document)
    fun requestWriteExternalStoragePermission()
}