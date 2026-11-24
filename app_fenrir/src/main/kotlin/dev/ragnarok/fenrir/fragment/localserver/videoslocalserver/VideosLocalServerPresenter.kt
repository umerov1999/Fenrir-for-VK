package dev.ragnarok.fenrir.fragment.localserver.videoslocalserver

import android.os.Bundle
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.interfaces.ILocalServerInteractor
import dev.ragnarok.fenrir.fragment.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.link.VKLinkParser
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.FindAt
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.delayTaskFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toMain

class VideosLocalServerPresenter(accountId: Long, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IVideosLocalServerView>(accountId, savedInstanceState) {
    private val videos: MutableList<Video> = ArrayList()
    private val fInteractor: ILocalServerInteractor =
        InteractorFactory.localServer
    private var actualDataDisposable = CancelableJob()
    private var fOffset = 0
    private var actualDataReceived = false
    private var endOfContent = false
    private var actualDataLoading = false
    private var searchAt: FindAt
    private var reverse = false
    private var doLoadTabs = false

    private var tmpDownloadVideo: Video? = null
    private var tmpDownloadPos = -1
    override fun onGuiCreated(viewHost: IVideosLocalServerView) {
        super.onGuiCreated(viewHost)
        viewHost.displayList(videos)
    }

    fun toggleReverse() {
        reverse = !reverse
        fireRefresh(false)
    }

    private fun loadActualData(offset: Int) {
        actualDataLoading = true
        resolveRefreshingView()
        appendJob(
            fInteractor.getVideos(offset, GET_COUNT, reverse)
                .fromIOToMain({
                    onActualDataReceived(
                        offset,
                        it
                    )
                }) { t -> onActualDataGetError(t) })
    }

    private fun onActualDataGetError(t: Throwable) {
        actualDataLoading = false
        showError(t)
        resolveRefreshingView()
    }

    private fun onActualDataReceived(offset: Int, data: List<Video>) {
        fOffset = offset + GET_COUNT
        actualDataLoading = false
        endOfContent = data.isEmpty()
        actualDataReceived = true
        if (offset == 0) {
            videos.clear()
            videos.addAll(data)
            view?.notifyListChanged()
        } else {
            val startSize = videos.size
            videos.addAll(data)
            view?.notifyDataAdded(
                startSize,
                data.size
            )
        }
        resolveRefreshingView()
    }

    override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
        doLoadTabs = if (doLoadTabs) {
            return
        } else {
            true
        }
        loadActualData(0)
    }

    private fun resolveRefreshingView() {
        resumedView?.displayLoading(
            actualDataLoading
        )
    }

    override fun onDestroyed() {
        actualDataDisposable.cancel()
        super.onDestroyed()
    }

    fun fireScrollToEnd(): Boolean {
        if (!endOfContent && videos.nonNullNoEmpty() && actualDataReceived && !actualDataLoading) {
            if (searchAt.isSearchMode()) {
                search(false)
            } else {
                loadActualData(fOffset)
            }
            return false
        }
        return true
    }

    private fun doSearch() {
        actualDataLoading = true
        resolveRefreshingView()
        appendJob(
            fInteractor.searchVideos(
                searchAt.getQuery(),
                searchAt.getOffset(),
                SEARCH_COUNT,
                reverse
            )
                .fromIOToMain({
                    onSearched(
                        FindAt(
                            searchAt.getQuery(),
                            searchAt.getOffset() + SEARCH_COUNT,
                            it.size < SEARCH_COUNT
                        ), it
                    )
                }) { t -> onActualDataGetError(t) })
    }

    private fun onSearched(searchAt: FindAt, data: List<Video>) {
        actualDataLoading = false
        actualDataReceived = true
        endOfContent = searchAt.isEnded()
        if (this.searchAt.getOffset() == 0) {
            videos.clear()
            videos.addAll(data)
            view?.notifyListChanged()
        } else {
            if (data.nonNullNoEmpty()) {
                val startSize = videos.size
                videos.addAll(data)
                view?.notifyDataAdded(
                    startSize,
                    data.size
                )
            }
        }
        this.searchAt = searchAt
        resolveRefreshingView()
    }

    private fun search(sleepSearch: Boolean) {
        if (actualDataLoading) return
        if (!sleepSearch) {
            doSearch()
            return
        }
        actualDataDisposable.cancel()
        actualDataDisposable += delayTaskFlow(WEB_SEARCH_DELAY.toLong())
            .toMain { doSearch() }
    }

    fun fireSearchRequestChanged(q: String?) {
        val query = q?.trim()
        if (!searchAt.do_compare(query)) {
            actualDataLoading = false
            if (query.isNullOrEmpty()) {
                actualDataDisposable.cancel()
                fireRefresh(false)
            } else {
                fireRefresh(true)
            }
        }
    }

    fun fireRefresh(sleepSearch: Boolean) {
        if (actualDataLoading) {
            return
        }
        if (searchAt.isSearchMode()) {
            searchAt.reset(false)
            search(sleepSearch)
        } else {
            loadActualData(0)
        }
    }

    fun firePrepareDownload(position: Int, video: Video) {
        tmpDownloadPos = position
        tmpDownloadVideo = video
        view?.onDownloadVideoNextStage()
    }

    fun continueDownload() {
        val position = tmpDownloadPos
        val video = tmpDownloadVideo
        if (position < 0 || position > videos.size - 1 || video == null) {
            return
        }
        view?.onStartVideoDownload(accountId, position, video)
        tmpDownloadPos = -1
        tmpDownloadVideo = null
    }

    fun fireOpenVideoMenu(
        position: Int,
        video: Video
    ) {
        if (position < videos.size && position >= 0) {
            view?.doVideoMenu(accountId, position, video)
        }
    }

    fun fireUpdateTime(video: Video) {
        val hash = VKLinkParser.parseLocalServerURL(video.mp4link720)
        if (hash.isNullOrEmpty()) {
            return
        }
        actualDataLoading = true
        resolveRefreshingView()

        appendJob(
            fInteractor.updateTime(hash).fromIOToMain(
                {
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.showSuccess()
                    fireRefresh(false)
                }, {
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                })
        )
    }

    fun firePrepareEditFileName(video: Video) {
        val hash = VKLinkParser.parseLocalServerURL(video.mp4link720)
        if (hash.isNullOrEmpty()) {
            return
        }
        actualDataLoading = true
        resolveRefreshingView()

        appendJob(
            fInteractor.getFileName(hash).fromIOToMain(
                {
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.displayEditFileNameDialog(hash, it)
                }, {
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                })
        )
    }

    fun firEditFileName(hash: String, newFileName: String) {
        actualDataLoading = true
        resolveRefreshingView()

        appendJob(
            fInteractor.updateFileName(hash, newFileName).fromIOToMain(
                {
                    actualDataLoading = false
                    resolveRefreshingView()
                    view?.showSuccess()
                    fireRefresh(false)
                }, {
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                })
        )
    }

    fun fireDeleteVideo(position: Int, video: Video) {
        if (position >= videos.size || position < 0) {
            return
        }
        val hash = VKLinkParser.parseLocalServerURL(video.mp4link720)
        if (hash.isNullOrEmpty()) {
            return
        }
        actualDataLoading = true
        resolveRefreshingView()

        appendJob(
            fInteractor.deleteMedia(hash).fromIOToMain(
                {
                    videos.removeAt(position)
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.showSuccess()
                    view?.notifyItemRemoved(position)
                }, {
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                })
        )
    }

    companion object {
        private const val SEARCH_COUNT = 50
        private const val GET_COUNT = 100
        private const val WEB_SEARCH_DELAY = 1000
    }

    init {
        searchAt = FindAt()
    }
}