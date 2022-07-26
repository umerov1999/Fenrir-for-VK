package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView

abstract class SimpleOwnersPresenter<V : ISimpleOwnersView>(
    accountId: Int,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<V>(accountId, savedInstanceState) {
    val data: MutableList<Owner>
    override fun onGuiCreated(viewHost: V) {
        super.onGuiCreated(viewHost)
        viewHost.displayOwnerList(data)
    }

    fun fireRefresh() {
        onUserRefreshed()
    }

    protected open fun onUserRefreshed() {}
    fun fireScrollToEnd() {
        onUserScrolledToEnd()
    }

    protected open fun onUserScrolledToEnd() {}

    fun fireOwnerClick(owner: Owner) {
        view?.showOwnerWall(accountId, owner)
    }

    init {
        data = ArrayList()
    }
}