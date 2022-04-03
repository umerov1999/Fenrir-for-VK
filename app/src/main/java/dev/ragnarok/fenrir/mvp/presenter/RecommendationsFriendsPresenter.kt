package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IRelationshipInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

class RecommendationsFriendsPresenter(accountId: Int, savedInstanceState: Bundle?) :
    SimpleOwnersPresenter<ISimpleOwnersView>(accountId, savedInstanceState) {
    private val relationshipInteractor: IRelationshipInteractor =
        InteractorFactory.createRelationshipInteractor()
    private val actualDataDisposable = CompositeDisposable()
    private var actualDataLoading = false
    private var doLoadTabs = false
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
        requestActualData()
    }

    private fun requestActualData() {
        actualDataLoading = true
        resolveRefreshingView()
        val accountId = accountId
        actualDataDisposable.add(relationshipInteractor.getRecommendations(accountId, 50)
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
        data.clear()
        data.addAll(users)
        view?.notifyDataSetChanged()
        resolveRefreshingView()
    }

    override fun onUserScrolledToEnd() {}
    override fun onUserRefreshed() {
        actualDataDisposable.clear()
        requestActualData()
    }

    override fun onDestroyed() {
        actualDataDisposable.dispose()
        super.onDestroyed()
    }

}