package dev.ragnarok.fenrir.mvp.presenter

import android.content.Context
import android.os.Bundle
import dev.ragnarok.fenrir.domain.IRelationshipInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IFriendsByPhonesView
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers

class FriendsByPhonesPresenter(accountId: Int, context: Context, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IFriendsByPhonesView>(accountId, savedInstanceState) {
    private val data: MutableList<Owner>
    private val friendsInteractor: IRelationshipInteractor =
        InteractorFactory.createRelationshipInteractor()
    private val context: Context
    private var netLoadingNow = false
    private fun resolveRefreshingView() {
        view?.displayLoading(
            netLoadingNow
        )
    }

    private fun requestActualData() {
        netLoadingNow = true
        resolveRefreshingView()
        val accountId = accountId
        appendDisposable(friendsInteractor.getByPhones(accountId, context)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ owners: List<User> -> onActualDataReceived(owners) }) { t: Throwable ->
                onActualDataGetError(
                    t
                )
            })
    }

    private fun onActualDataGetError(t: Throwable) {
        netLoadingNow = false
        resolveRefreshingView()
        showError(t)
    }

    private fun onActualDataReceived(owners: List<User>) {
        netLoadingNow = false
        resolveRefreshingView()
        data.clear()
        data.addAll(owners)
        view?.notifyDataSetChanged()
    }

    override fun onGuiCreated(viewHost: IFriendsByPhonesView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(data)
        resolveRefreshingView()
    }

    fun fireRefresh() {
        requestActualData()
    }

    fun onUserOwnerClicked(owner: Owner) {
        view?.showOwnerWall(
            accountId,
            owner
        )
    }

    init {
        data = ArrayList()
        this.context = context
        requestActualData()
    }
}