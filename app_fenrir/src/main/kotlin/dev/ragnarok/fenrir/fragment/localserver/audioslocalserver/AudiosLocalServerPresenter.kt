package dev.ragnarok.fenrir.fragment.localserver.audioslocalserver

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Bundle
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.interfaces.ILocalServerInteractor
import dev.ragnarok.fenrir.fragment.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.link.VKLinkParser
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.observeServiceBinding
import dev.ragnarok.fenrir.media.music.MusicPlaybackService.Companion.startForPlayList
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.FindAt
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.delayTaskFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.sharedFlowToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toMain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AudiosLocalServerPresenter(accountId: Long, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IAudiosLocalServerView>(accountId, savedInstanceState) {
    private val audios: MutableList<Audio> = ArrayList()
    private val fInteractor: ILocalServerInteractor =
        InteractorFactory.localServer
    private var sleepDataJob = CancelableJob()
    private var serviceStatusJob = CancelableJob()
    private var fOffset = 0
    private var actualDataReceived = false
    private var endOfContent = false
    private var actualDataLoading = false
    private var searchAt: FindAt
    private var reverse = false
    private var discography = false
    private var doAudioLoadTabs = false

    private var tmpDownloadAudio: Audio? = null
    private var tmpDownloadPos = -1
    fun updateReverse(reverse: Boolean) {
        this.reverse = reverse
        fireRefresh(false)
    }

    fun updateDiscography(discography: Boolean) {
        this.discography = discography
        if (searchAt.isSearchMode()) {
            searchAt.reset(true)
        }
        fireRefresh(false)
    }

    fun fireOptions() {
        view?.displayOptionsDialog(
            accountId,
            reverse,
            discography
        )
    }

    override fun onGuiCreated(viewHost: IAudiosLocalServerView) {
        super.onGuiCreated(viewHost)
        viewHost.displayList(audios)
    }

    private fun loadActualData(offset: Int) {
        actualDataLoading = true
        resolveRefreshingView()
        appendJob(
            (if (discography) fInteractor.getDiscography(
                offset,
                GET_COUNT,
                reverse
            ) else fInteractor.getAudios(offset, GET_COUNT, reverse))
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

    private fun onActualDataReceived(offset: Int, data: List<Audio>) {
        fOffset = offset + GET_COUNT
        actualDataLoading = false
        endOfContent = data.isEmpty()
        actualDataReceived = true
        if (offset == 0) {
            audios.clear()
            audios.addAll(data)
            view?.notifyListChanged()
        } else {
            val startSize = audios.size
            audios.addAll(data)
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
        doAudioLoadTabs = if (doAudioLoadTabs) {
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
        serviceStatusJob.cancel()
        sleepDataJob.cancel()
        super.onDestroyed()
    }

    fun fireScrollToEnd(): Boolean {
        if (!endOfContent && audios.nonNullNoEmpty() && actualDataReceived && !actualDataLoading) {
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
            (if (discography) fInteractor.searchDiscography(
                searchAt.getQuery(),
                searchAt.getOffset(),
                SEARCH_COUNT,
                reverse
            ) else fInteractor.searchAudios(
                searchAt.getQuery(),
                searchAt.getOffset(),
                SEARCH_COUNT,
                reverse
            ))
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

    private fun onSearched(searchAt: FindAt, data: List<Audio>) {
        actualDataLoading = false
        actualDataReceived = true
        endOfContent = searchAt.isEnded()
        if (this.searchAt.getOffset() == 0) {
            audios.clear()
            audios.addAll(data)
            view?.notifyListChanged()
        } else {
            if (data.nonNullNoEmpty()) {
                val startSize = audios.size
                audios.addAll(data)
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
        sleepDataJob.cancel()
        sleepDataJob += delayTaskFlow(WEB_SEARCH_DELAY.toLong())
            .toMain { doSearch() }
    }

    fun fireSearchRequestChanged(q: String?) {
        val query = q?.trim()
        if (!searchAt.do_compare(query)) {
            actualDataLoading = false
            if (query.isNullOrEmpty()) {
                sleepDataJob.cancel()
                fireRefresh(false)
            } else {
                fireRefresh(true)
            }
        }
    }

    fun getAudioPos(audio: Audio?): Int {
        if (audios.isNotEmpty() && audio != null) {
            for ((pos, i) in audios.withIndex()) {
                if (i.id == audio.id && i.ownerId == audio.ownerId) {
                    i.isAnimationNow = true
                    view?.notifyItemChanged(
                        pos
                    )
                    return pos
                }
            }
        }
        return -1
    }

    fun firePlayAudio(context: Context, position: Int) {
        startForPlayList(context, ArrayList(audios), position)
        if (!Settings.get().main().isShow_mini_player) getPlayerPlace(accountId).tryOpenWith(
            context
        )
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

    fun fireOpenMenu(position: Int, audio: Audio) {
        if (position < audios.size && position >= 0) {
            view?.onOpenMenu(accountId, position, audio)
        }
    }

    private fun doBitrate(url: String): Flow<Int> {
        return flow {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(url, HashMap())
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            if (bitrate != null) {
                emit((bitrate.toLong() / 1000).toInt())
            } else {
                throw Throwable("Can't receipt bitrate!")
            }
        }
    }

    fun fireGetBitrate(audio: Audio) {
        val url = audio.url
        if (url.isNullOrEmpty()) {
            return
        }
        actualDataLoading = true
        resolveRefreshingView()
        appendJob(
            doBitrate(url).fromIOToMain(
                {
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.onShowBitrate(it, audio.duration)
                }
            ) {
                actualDataLoading = false
                resolveRefreshingView()

                view?.showThrowable(it)
            })
    }

    fun firePrepareDownload(position: Int, audio: Audio) {
        tmpDownloadPos = position
        tmpDownloadAudio = audio
        view?.onDownloadAudioNextStage()
    }

    fun continueDownload() {
        val position = tmpDownloadPos
        val audio = tmpDownloadAudio
        if (position < 0 || position > audios.size - 1 || audio == null) {
            return
        }
        view?.onStartAudioDownload(accountId, position, audio)
        tmpDownloadPos = -1
        tmpDownloadAudio = null
    }

    fun fireUpdateTime(audio: Audio) {
        val hash = VKLinkParser.parseLocalServerURL(audio.url)
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

    fun firePrepareEditFileName(audio: Audio) {
        val hash = VKLinkParser.parseLocalServerURL(audio.url)
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

    fun fireDeleteAudio(position: Int, audio: Audio) {
        if (position >= audios.size || position < 0) {
            return
        }
        val hash = VKLinkParser.parseLocalServerURL(audio.url)
        if (hash.isNullOrEmpty()) {
            return
        }
        actualDataLoading = true
        resolveRefreshingView()

        appendJob(
            fInteractor.deleteMedia(hash).fromIOToMain(
                {
                    audios.removeAt(position)
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

    fun fireUploadAudio(audio: Audio) {
        val hash = VKLinkParser.parseLocalServerURL(audio.url)
        if (hash.isNullOrEmpty()) {
            return
        }
        actualDataLoading = true
        resolveRefreshingView()

        appendJob(
            fInteractor.uploadAudio(hash).fromIOToMain(
                {
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.showSuccess()
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
        serviceStatusJob += observeServiceBinding()
            .sharedFlowToMain {
                view?.onServiceBindEvent(it)
            }
    }
}