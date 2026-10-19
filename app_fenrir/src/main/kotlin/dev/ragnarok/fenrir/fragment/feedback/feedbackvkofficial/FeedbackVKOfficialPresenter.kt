package dev.ragnarok.fenrir.fragment.feedback.feedbackvkofficial

import android.os.Bundle
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.interfaces.IFeedbackInteractor
import dev.ragnarok.fenrir.fragment.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.model.FeedbackVKOfficial
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.coroutines.CompositeJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain

class FeedbackVKOfficialPresenter(accountId: Long, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IFeedbackVKOfficialView>(accountId, savedInstanceState) {
    private val items: MutableList<FeedbackVKOfficial> = ArrayList()
    private val fInteractor: IFeedbackInteractor = InteractorFactory.feedback
    private val actualDataDisposable = CompositeJob()
    private val cacheDataDisposable = CompositeJob()
    private var actualDataReceived = false
    private var endOfContent = false
    private var actualDataLoading = false
    private var cacheDataLoading = false
    private var currentOffset = 0
    override fun onGuiCreated(viewHost: IFeedbackVKOfficialView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(items)
        resolveEmptyTextVisibility()
    }

    fun resolveEmptyTextVisibility() {
        view?.setEmptyTextVisible(items.isEmpty())
    }

    private fun loadActualData(offset: Int) {
        actualDataLoading = true
        resolveRefreshingView()
        actualDataDisposable.add(
            fInteractor.getActualFeedbacksOfficial(accountId, COUNT, offset)
                .fromIOToMain({ data ->
                    onActualDataReceived(
                        offset,
                        data
                    )
                }) { t -> onActualDataGetError(t) })
    }

    private fun loadCachedData() {
        cacheDataLoading = true
        resolveRefreshingView()
        cacheDataDisposable.add(
            fInteractor.getCachedFeedbacksOfficial(accountId)
                .fromIOToMain({ data ->
                    onCachedDataReceived(
                        data
                    )
                }) {
                    cacheDataLoading = false
                    resolveRefreshingView()
                    it.printStackTrace()
                })
    }

    fun hideNotification(position: Int, query: String?) {
        if (Utils.isHiddenAccount(accountId)) {
            return
        }
        actualDataLoading = true
        resolveRefreshingView()
        actualDataDisposable.add(
            fInteractor.hide(accountId, query)
                .fromIOToMain({
                    actualDataLoading = false
                    resolveRefreshingView()
                    items.removeAt(position)
                    view?.notifyItemRemoved(
                        position
                    )
                    resolveEmptyTextVisibility()
                }) { t -> onActualDataGetError(t) })
    }

    private fun onActualDataGetError(t: Throwable) {
        actualDataLoading = false
        showError(t)
        resolveRefreshingView()
    }

    private fun onActualDataReceived(offset: Int, data: List<FeedbackVKOfficial>) {
        currentOffset = offset + COUNT
        cacheDataDisposable.clear()
        cacheDataLoading = false
        actualDataLoading = false
        endOfContent = data.size < COUNT
        actualDataReceived = true
        if (offset == 0) {
            items.clear()
            items.addAll(data)
            view?.notifyDataSetChanges()
            view?.notifyMarkViewedNotifications()
        } else {
            val startSize = items.size
            items.addAll(data)
            view?.notifyDataAdded(
                startSize,
                data.size
            )
        }
        resolveRefreshingView()
        resolveEmptyTextVisibility()
    }

    private fun onCachedDataReceived(data: List<FeedbackVKOfficial>) {
        cacheDataLoading = false
        items.clear()
        items.addAll(data)
        view?.notifyDataSetChanges()
        resolveRefreshingView()
        resolveEmptyTextVisibility()
    }

    override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
    }

    private fun resolveRefreshingView() {
        resumedView?.showRefreshing(
            cacheDataLoading || actualDataLoading
        )
    }

    override fun onDestroyed() {
        actualDataDisposable.cancel()
        cacheDataDisposable.cancel()
        super.onDestroyed()
    }

    fun fireScrollToEnd(): Boolean {
        if (!endOfContent && items.isNotEmpty() && actualDataReceived && !actualDataLoading) {
            loadActualData(currentOffset)
            return false
        }
        return true
    }

    fun fireRefresh() {
        if (cacheDataLoading) {
            return
        }
        actualDataDisposable.clear()
        actualDataLoading = false
        loadActualData(0)
    }

    init {
        loadCachedData()
        loadActualData(0)
    }

    companion object {
        private const val COUNT = 100
    }
}