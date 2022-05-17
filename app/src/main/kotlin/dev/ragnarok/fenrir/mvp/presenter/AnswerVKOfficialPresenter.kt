package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IFeedbackInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.AnswerVKOfficialList
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IAnswerVKOfficialView
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AnswerVKOfficialPresenter(accountId: Int, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IAnswerVKOfficialView>(accountId, savedInstanceState) {
    private val pages: AnswerVKOfficialList = AnswerVKOfficialList()
    private val fInteractor: IFeedbackInteractor
    private val actualDataDisposable = CompositeDisposable()
    private var actualDataReceived = false
    private var endOfContent = false
    private var actualDataLoading = false
    override fun onGuiCreated(viewHost: IAnswerVKOfficialView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(pages)
    }

    private fun loadActualData(offset: Int) {
        actualDataLoading = true
        resolveRefreshingView()
        val accountId = accountId
        actualDataDisposable.add(fInteractor.getOfficial(accountId, 100, offset)
            .fromIOToMain()
            .subscribe({ data ->
                onActualDataReceived(
                    offset,
                    data
                )
            }) { t -> onActualDataGetError(t) })
    }

    fun hideNotification(position: Int, query: String?) {
        val accountId = accountId
        actualDataDisposable.add(fInteractor.hide(accountId, query)
            .fromIOToMain()
            .subscribe({
                pages.items?.removeAt(position)
                view?.notifyItemRemoved(
                    position
                )
            }) { t -> onActualDataGetError(t) })
    }

    private fun onActualDataGetError(t: Throwable) {
        actualDataLoading = false
        showError(getCauseIfRuntime(t))
        resolveRefreshingView()
    }

    private fun onActualDataReceived(offset: Int, data: AnswerVKOfficialList) {
        actualDataLoading = false
        endOfContent = (data.items?.size ?: 0) < 100
        actualDataReceived = true
        if (offset == 0) {
            pages.items?.clear()
            pages.fields?.clear()
            data.items?.let { pages.items?.addAll(it) }
            data.fields?.let { pages.fields?.addAll(it) }
            view?.notifyFirstListReceived()
        } else {
            val startSize = pages.items?.size.orZero()
            data.items?.let { pages.items?.addAll(it) }
            data.fields?.let { pages.fields?.addAll(it) }
            view?.notifyDataAdded(
                startSize,
                data.items?.size.orZero()
            )
        }
        resolveRefreshingView()
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
    }

    private fun resolveRefreshingView() {
        resumedView?.showRefreshing(
            actualDataLoading
        )
    }

    override fun onDestroyed() {
        actualDataDisposable.dispose()
        super.onDestroyed()
    }

    fun fireScrollToEnd(): Boolean {
        if (!endOfContent && pages.items.nonNullNoEmpty() && actualDataReceived && !actualDataLoading) {
            loadActualData(pages.items?.size.orZero())
            return false
        }
        return true
    }

    fun fireRefresh() {
        actualDataDisposable.clear()
        actualDataLoading = false
        loadActualData(0)
    }

    init {
        pages.fields = ArrayList()
        pages.items = ArrayList()
        fInteractor = InteractorFactory.createFeedbackInteractor()
        loadActualData(0)
    }
}