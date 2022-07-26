package dev.ragnarok.fenrir.mvp.presenter.base

import android.os.Bundle
import androidx.annotation.CallSuper
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.toMainThread

abstract class AccountDependencyPresenter<V>(accountId: Int, savedInstanceState: Bundle?) :
    RxSupportPresenter<V>(savedInstanceState) where V : IMvpView, V : IAccountDependencyView {
    var accountId = 0
        private set
    private var mSupportAccountHotSwap = false
    private var mInvalidAccountContext = false
    private fun onAccountChange(newAccountId: Int) {
        val oldAccountId = accountId
        if (oldAccountId == newAccountId) {
            mInvalidAccountContext = false
            return
        }
        if (mSupportAccountHotSwap) {
            beforeAccountChange(oldAccountId, newAccountId)
            accountId = newAccountId
            afterAccountChange(oldAccountId, newAccountId)
        } else {
            mInvalidAccountContext = true
            view?.displayAccountNotSupported()
        }
    }

    override fun onGuiCreated(viewHost: V) {
        super.onGuiCreated(viewHost)
        if (mInvalidAccountContext) {
            viewHost.displayAccountNotSupported()
        }
    }

    @CallSuper
    protected open fun afterAccountChange(oldAccountId: Int, newAccountId: Int) {
    }

    @Suppress("UNUSED_PARAMETER")
    @CallSuper
    protected fun beforeAccountChange(oldAccountId: Int, newAccountId: Int) {
    }

    protected fun setSupportAccountHotSwap(supportAccountHotSwap: Boolean) {
        mSupportAccountHotSwap = supportAccountHotSwap
    }

    override fun saveState(outState: Bundle) {
        super.saveState(outState)
        outState.putInt(SAVE_ACCOUNT_ID, accountId)
        outState.putBoolean(SAVE_INVALID_ACCOUNT_CONTEXT, mInvalidAccountContext)
    }

    companion object {
        private const val SAVE_ACCOUNT_ID = "save_account_id"
        private const val SAVE_INVALID_ACCOUNT_CONTEXT = "save_invalid_account_context"
    }

    init {
        if (savedInstanceState != null) {
            this.accountId = savedInstanceState.getInt(SAVE_ACCOUNT_ID)
            mInvalidAccountContext = savedInstanceState.getBoolean(SAVE_INVALID_ACCOUNT_CONTEXT)
        } else {
            this.accountId = accountId
        }
        appendDisposable(
            Settings.get()
                .accounts()
                .observeChanges()
                .toMainThread()
                .subscribe { onAccountChange(it) })
    }
}