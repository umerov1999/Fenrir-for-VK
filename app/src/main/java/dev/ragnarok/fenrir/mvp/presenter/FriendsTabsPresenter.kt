package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IOwnersRepository
import dev.ragnarok.fenrir.domain.IRelationshipInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.Repository.owners
import dev.ragnarok.fenrir.model.FriendsCounters
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IFriendsTabsView
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers

class FriendsTabsPresenter(
    accountId: Int,
    private val userId: Int,
    counters: FriendsCounters?,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<IFriendsTabsView>(accountId, savedInstanceState) {
    private val relationshipInteractor: IRelationshipInteractor =
        InteractorFactory.createRelationshipInteractor()
    private val ownersRepository: IOwnersRepository = owners
    private var counters: FriendsCounters? = null
    private var owner: Owner? = null
    private fun requestOwnerInfo() {
        val accountId = accountId
        appendDisposable(ownersRepository.getBaseOwnerInfo(
            accountId,
            userId,
            IOwnersRepository.MODE_ANY
        )
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ owner: Owner -> onOwnerInfoReceived(owner) }) { })
    }

    private fun onOwnerInfoReceived(owner: Owner) {
        this.owner = owner
        view?.displayUserNameAtToolbar(
            owner.fullName
        )
    }

    private fun requestCounters() {
        val accountId = accountId
        appendDisposable(relationshipInteractor.getFriendsCounters(accountId, userId)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ counters: FriendsCounters -> onCountersReceived(counters) }) { t: Throwable ->
                onCountersGetError(
                    t
                )
            })
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        view?.setDrawerFriendsSectionSelected(
            userId == accountId
        )
    }

    private fun onCountersGetError(t: Throwable) {
        view?.displayCounters(
            counters ?: FriendsCounters(0, 0, 0, 0)
        )
        showError(t)
    }

    private fun onCountersReceived(counters: FriendsCounters) {
        this.counters = counters
        view?.displayCounters(
            counters
        )
    }

    override fun onGuiCreated(viewHost: IFriendsTabsView) {
        super.onGuiCreated(viewHost)
        viewHost.configTabs(accountId, userId, userId != accountId)
        viewHost.displayCounters(counters ?: FriendsCounters(0, 0, 0, 0))
    }

    companion object {
        private const val SAVE_COUNTERS = "save_counters"
    }

    init {
        if (savedInstanceState != null) {
            this.counters = savedInstanceState.getParcelable(SAVE_COUNTERS)
        } else {
            this.counters = counters
        }
        if (this.counters == null) {
            this.counters = FriendsCounters(0, 0, 0, 0)
            requestCounters()
        }
        if (owner == null && userId != accountId) {
            requestOwnerInfo()
        }
    }
}