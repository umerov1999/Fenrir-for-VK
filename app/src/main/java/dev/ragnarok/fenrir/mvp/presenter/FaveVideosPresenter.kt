package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IFaveInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IFaveVideosView
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

class FaveVideosPresenter(accountId: Int, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IFaveVideosView>(accountId, savedInstanceState) {
    private val faveInteractor: IFaveInteractor = InteractorFactory.createFaveInteractor()
    private val mVideos: ArrayList<Video> = ArrayList()
    private val cacheDisposable = CompositeDisposable()
    private val netDisposable = CompositeDisposable()
    private var mEndOfContent = false
    private var cacheLoadingNow = false
    private var netLoadingNow = false
    private var doLoadTabs = false
    private fun resolveRefreshingView() {
        view?.showRefreshing(netLoadingNow)
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
        doLoadTabs = if (doLoadTabs) {
            return
        } else {
            true
        }
        requestAtLast()
    }

    private fun loadCachedData() {
        cacheLoadingNow = true
        val accountId = accountId
        cacheDisposable.add(faveInteractor.getCachedVideos(accountId)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ videos: List<Video> -> onCachedDataReceived(videos) }) { t: Throwable ->
                onCacheGetError(
                    t
                )
            })
    }

    private fun onCacheGetError(t: Throwable) {
        cacheLoadingNow = false
        showError(t)
    }

    private fun onCachedDataReceived(videos: List<Video>) {
        cacheLoadingNow = false
        mVideos.clear()
        mVideos.addAll(videos)
        view?.notifyDataSetChanged()
    }

    override fun onDestroyed() {
        cacheDisposable.dispose()
        netDisposable.dispose()
        super.onDestroyed()
    }

    private fun request(offset: Int) {
        netLoadingNow = true
        resolveRefreshingView()
        val accountId = accountId
        netDisposable.add(faveInteractor.getVideos(accountId, COUNT_PER_REQUEST, offset)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ videos: List<Video> ->
                onNetDataReceived(
                    offset,
                    videos
                )
            }) { t: Throwable -> onNetDataGetError(t) })
    }

    private fun onNetDataGetError(t: Throwable) {
        netLoadingNow = false
        resolveRefreshingView()
        showError(t)
    }

    private fun onNetDataReceived(offset: Int, videos: List<Video>) {
        cacheDisposable.clear()
        cacheLoadingNow = false
        mEndOfContent = videos.isEmpty()
        netLoadingNow = false
        if (offset == 0) {
            mVideos.clear()
            mVideos.addAll(videos)
            view?.notifyDataSetChanged()
        } else {
            val startSize = mVideos.size
            mVideos.addAll(videos)
            view?.notifyDataAdded(
                startSize,
                videos.size
            )
        }
        resolveRefreshingView()
    }

    private fun requestAtLast() {
        request(0)
    }

    private fun requestNext() {
        request(mVideos.size)
    }

    override fun onGuiCreated(viewHost: IFaveVideosView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(mVideos)
    }

    private fun canLoadMore(): Boolean {
        return mVideos.isNotEmpty() && !cacheLoadingNow && !netLoadingNow && !mEndOfContent
    }

    fun fireRefresh() {
        cacheDisposable.clear()
        netDisposable.clear()
        netLoadingNow = false
        requestAtLast()
    }

    fun fireVideoClick(video: Video) {
        view?.goToPreview(
            accountId,
            video
        )
    }

    fun fireVideoDelete(index: Int, video: Video) {
        netDisposable.add(faveInteractor.removeVideo(accountId, video.ownerId, video.id)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({
                mVideos.removeAt(index)
                view?.notifyDataSetChanged()
            }) { t: Throwable -> onNetDataGetError(t) })
    }

    fun fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext()
        }
    }

    companion object {
        private const val COUNT_PER_REQUEST = 25
    }

    init {
        loadCachedData()
    }
}