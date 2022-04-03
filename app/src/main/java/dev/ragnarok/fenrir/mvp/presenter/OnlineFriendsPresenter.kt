package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IRelationshipInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

class OnlineFriendsPresenter(accountId: Int, private val userId: Int, savedInstanceState: Bundle?) :
    SimpleOwnersPresenter<ISimpleOwnersView>(accountId, savedInstanceState) {
    private val relationshipInteractor: IRelationshipInteractor =
        InteractorFactory.createRelationshipInteractor()
    private val actualDataDisposable = CompositeDisposable()
    private var endOfContent = false
    private var actualDataLoading = false
    private var doLoadTabs = false
    private var offset = 0
    private fun resolveRefreshingView() {
        resumedView?.displayRefreshing(
            actualDataLoading
        )
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
        doLoadTabs = if (doLoadTabs) {
            return
        } else {
            true
        }
        offset = 0
        requestActualData()
    }

    private fun requestActualData() {
        actualDataLoading = true
        resolveRefreshingView()
        val accountId = accountId
        actualDataDisposable.add(relationshipInteractor.getOnlineFriends(
            accountId,
            userId,
            200,
            offset
        )
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ users: List<User> -> onDataReceived(users) }) { t: Throwable ->
                onDataGetError(
                    t
                )
            })
    }

    private fun onDataGetError(t: Throwable) {
        actualDataLoading = false
        resolveRefreshingView()
        showError(t)
    }

    private fun onDataReceived(users: List<User>) {
        actualDataLoading = false
        endOfContent = users.isEmpty()
        if (offset == 0) {
            data.clear()
            data.addAll(users)
            view?.notifyDataSetChanged()
        } else {
            val sizeBefore = data.size
            data.addAll(users)
            view?.notifyDataAdded(
                sizeBefore,
                users.size
            )
        }
        resolveRefreshingView()
        offset += 200
    }

    override fun onUserScrolledToEnd() {
        if (!endOfContent && !actualDataLoading && offset > 0) {
            requestActualData()
        }
    }

    override fun onUserRefreshed() {
        actualDataDisposable.clear()
        offset = 0
        requestActualData()
    }

    override fun onDestroyed() {
        actualDataDisposable.dispose()
        super.onDestroyed()
    }

}