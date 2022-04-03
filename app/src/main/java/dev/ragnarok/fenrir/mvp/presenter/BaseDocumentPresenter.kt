package dev.ragnarok.fenrir.mvp.presenter

import android.content.Context
import android.os.Bundle
import android.view.View
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.IDocsInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IBasicDocumentView
import dev.ragnarok.fenrir.util.RxUtils.applyCompletableIOToMainSchedulers
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime

open class BaseDocumentPresenter<V : IBasicDocumentView>(
    accountId: Int,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<V>(accountId, savedInstanceState) {
    private val docsInteractor: IDocsInteractor = InteractorFactory.createDocsInteractor()
    fun fireWritePermissionResolved(context: Context, view: View?) {
        onWritePermissionResolved(context, view)
    }

    protected open fun onWritePermissionResolved(context: Context, view: View?) {
        // hook for child classes
    }

    protected fun addYourself(document: Document) {
        val accountId = accountId
        val docId = document.id
        val ownerId = document.ownerId
        appendDisposable(docsInteractor.add(accountId, docId, ownerId, document.accessKey)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({
                onDocAddedSuccessfully()
            }) {
                showError(getCauseIfRuntime(it))
            })
    }

    protected fun delete(id: Int, ownerId: Int) {
        val accountId = accountId
        appendDisposable(docsInteractor.delete(accountId, id, ownerId)
            .compose(applyCompletableIOToMainSchedulers())
            .subscribe({
                onDocDeleteSuccessfully()
            }) { t: Throwable -> onDocDeleteError(t) })
    }

    private fun onDocDeleteError(t: Throwable) {
        showError(getCauseIfRuntime(t))
    }

    private fun onDocDeleteSuccessfully() {
        showToast(R.string.deleted, true)
    }

    private fun onDocAddedSuccessfully() {
        showToast(R.string.added, true)
    }

}