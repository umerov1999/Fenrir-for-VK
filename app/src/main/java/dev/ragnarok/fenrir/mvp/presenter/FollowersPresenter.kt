package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IAccountsInteractor
import dev.ragnarok.fenrir.domain.IRelationshipInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.mvp.view.IFollowersView
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.RxUtils.applyCompletableIOToMainSchedulers
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.RxUtils.ignore
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import dev.ragnarok.fenrir.util.Utils.indexOf
import dev.ragnarok.fenrir.util.Utils.indexOfOwner
import io.reactivex.rxjava3.disposables.CompositeDisposable

class FollowersPresenter(accountId: Int, private val userId: Int, savedInstanceState: Bundle?) :
    SimpleOwnersPresenter<IFollowersView>(accountId, savedInstanceState) {
    private val relationshipInteractor: IRelationshipInteractor =
        InteractorFactory.createRelationshipInteractor()
    private val accountsInteractor: IAccountsInteractor =
        InteractorFactory.createAccountInteractor()
    private val actualDataDisposable = CompositeDisposable()
    private val cacheDisposable = CompositeDisposable()
    private val isNotFriendShow: Boolean = Settings.get().other().isNot_friend_show
    private var actualDataLoading = false
    private var actualDataReceived = false
    private var endOfContent = false
    private var cacheLoadingNow = false
    private var doLoadTabs = false
    private var not_followers: MutableList<Owner>? = null
    private var add_followers: MutableList<Owner>? = null
    private var offset = 0
    private fun requestActualData(do_scan: Boolean) {
        actualDataLoading = true
        resolveRefreshingView()
        val accountId = accountId
        actualDataDisposable.add(relationshipInteractor.getFollowers(
            accountId,
            userId,
            if (isNotFriendShow) 1000 else 200,
            offset
        )
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ users: List<User> ->
                onActualDataReceived(
                    users,
                    do_scan
                )
            }) { t: Throwable -> onActualDataGetError(t) })
    }

    fun removeFollower(owner: Owner) {
        appendDisposable(
            accountsInteractor.banUsers(accountId, listOf(owner as User))
                .compose(applyCompletableIOToMainSchedulers())
                .subscribe({
                    val pos = indexOfOwner(data, owner)
                    if (pos >= 0) {
                        data.removeAt(pos)
                        view?.notifyRemoved(
                            pos
                        )
                    }
                }, ignore())
        )
    }

    override fun onGuiCreated(viewHost: IFollowersView) {
        super.onGuiCreated(viewHost)
        checkAndShowModificationFriends()
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
        doLoadTabs = if (doLoadTabs) {
            return
        } else {
            true
        }
        loadAllCacheData()
        if (!isNotFriendShow) {
            offset = 0
            requestActualData(false)
        }
    }

    private fun resolveRefreshingView() {
        view?.displayRefreshing(
            actualDataLoading
        )
    }

    private fun onActualDataGetError(t: Throwable) {
        actualDataLoading = false
        showError(getCauseIfRuntime(t))
        resolveRefreshingView()
    }

    private fun checkAndShowModificationFriends() {
        if (!add_followers.isNullOrEmpty() || !not_followers.isNullOrEmpty()) {
            view?.showModFollowers(
                add_followers,
                not_followers,
                accountId
            )
        }
    }

    fun clearModificationFollowers(add: Boolean, not: Boolean) {
        if (add && !add_followers.isNullOrEmpty()) {
            add_followers?.clear()
            add_followers = null
        }
        if (not && !not_followers.isNullOrEmpty()) {
            not_followers?.clear()
            not_followers = null
        }
    }

    private fun onActualDataReceived(users: List<User>, do_scan: Boolean) {
        if (do_scan && isNotFriendShow) {
            not_followers = ArrayList()
            for (i in data) {
                if (indexOf(users, i.ownerId) == -1) {
                    not_followers?.add(i)
                }
            }
            add_followers = ArrayList()
            for (i in users) {
                if (indexOfOwner(data, i.id) == -1) {
                    add_followers?.add(i)
                }
            }
            checkAndShowModificationFriends()
        }
        actualDataLoading = false
        cacheDisposable.clear()
        actualDataReceived = true
        endOfContent = users.isEmpty()
        if (offset == 0) {
            data.clear()
            data.addAll(users)
            view?.notifyDataSetChanged()
        } else {
            val startSzie = data.size
            data.addAll(users)
            view?.notifyDataAdded(
                startSzie,
                users.size
            )
        }
        offset += if (isNotFriendShow) 1000 else 200
        resolveRefreshingView()
    }

    override fun onUserScrolledToEnd() {
        if (!endOfContent && !cacheLoadingNow && !actualDataLoading && actualDataReceived) {
            requestActualData(false)
        }
    }

    public override fun onUserRefreshed() {
        cacheDisposable.clear()
        cacheLoadingNow = false
        actualDataDisposable.clear()
        offset = 0
        requestActualData(false)
    }

    private fun loadAllCacheData() {
        cacheLoadingNow = true
        val accountId = accountId
        cacheDisposable.add(relationshipInteractor.getCachedFollowers(accountId, userId)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ users: List<User> -> onCachedDataReceived(users) }) { t: Throwable ->
                onCacheDataGetError(
                    t
                )
            })
    }

    private fun onCacheDataGetError(t: Throwable) {
        cacheLoadingNow = false
        showError(getCauseIfRuntime(t))
        if (isNotFriendShow) {
            offset = 0
            requestActualData(false)
        }
    }

    private fun onCachedDataReceived(users: List<User>) {
        cacheLoadingNow = false
        data.addAll(users)
        view?.notifyDataSetChanged()
        if (isNotFriendShow) {
            offset = 0
            requestActualData(users.isNotEmpty())
        }
    }

    override fun onDestroyed() {
        cacheDisposable.dispose()
        actualDataDisposable.dispose()
        super.onDestroyed()
    }

}