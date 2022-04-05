package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.domain.ICommunitiesInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.ICommunityFriendsView
import java.util.*

class CommunityFriendsPresenter(
    accountId: Int,
    private val groupId: Int,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<ICommunityFriendsView>(accountId, savedInstanceState) {
    private val communitiesInteractor: ICommunitiesInteractor
    private val users: MutableList<Owner>
    private val original: MutableList<Owner>
    private var refreshing = false
    private var query: String? = null
    fun setLoadingNow(loadingNow: Boolean) {
        refreshing = loadingNow
        resolveRefreshing()
    }

    private fun updateCriteria() {
        setLoadingNow(true)
        users.clear()
        if (query.isNullOrEmpty()) {
            users.addAll(original)
            setLoadingNow(false)
            view?.notifyDataSetChanged()
            return
        }
        for (i in original) {
            if (query?.lowercase(Locale.getDefault())
                    ?.let {
                        i.fullName.lowercase(Locale.getDefault()).contains(it)
                    } == true || query?.lowercase(Locale.getDefault())
                    ?.let {
                        i.domain.lowercase(
                            Locale.getDefault()
                        )
                            .contains(
                                it
                            )
                    } == true
            ) {
                users.add(i)
            }
        }
        setLoadingNow(false)
        view?.notifyDataSetChanged()
    }

    fun fireQuery(q: String?) {
        query = if (q.isNullOrEmpty()) null else {
            q
        }
        updateCriteria()
    }

    override fun onGuiCreated(viewHost: ICommunityFriendsView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(users)
    }

    private fun resolveRefreshing() {
        view?.displayRefreshing(
            refreshing
        )
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshing()
    }

    private fun setRefreshing(refreshing: Boolean) {
        this.refreshing = refreshing
        resolveRefreshing()
    }

    private fun requestData() {
        val accountId = accountId
        setRefreshing(true)
        appendDisposable(communitiesInteractor.getGroupFriends(accountId, groupId)
            .fromIOToMain()
            .subscribe({ users -> onDataReceived(users) }) { t ->
                onDataGetError(
                    t
                )
            })
    }

    private fun onDataGetError(t: Throwable) {
        setRefreshing(false)
        showError(t)
    }

    private fun onDataReceived(users: List<Owner>) {
        setRefreshing(false)
        original.clear()
        original.addAll(users)
        updateCriteria()
    }

    fun fireRefresh() {
        if (!refreshing) {
            requestData()
        }
    }

    fun fireUserClick(user: Owner) {
        view?.openUserWall(
            accountId,
            user
        )
    }

    init {
        users = ArrayList()
        original = ArrayList()
        communitiesInteractor = InteractorFactory.createCommunitiesInteractor()
        requestData()
    }
}