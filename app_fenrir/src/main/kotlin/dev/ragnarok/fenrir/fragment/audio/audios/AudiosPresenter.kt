package dev.ragnarok.fenrir.fragment.audio.audios

import android.content.Context
import android.os.Bundle
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.AccessIdPair
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.interfaces.IAudioInteractor
import dev.ragnarok.fenrir.fragment.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.observeServiceBinding
import dev.ragnarok.fenrir.media.music.MusicPlaybackService.Companion.startForPlayList
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.AudioPlaylist
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.DownloadWorkUtils.CheckDirectory
import dev.ragnarok.fenrir.util.DownloadWorkUtils.TrackIsDownloaded
import dev.ragnarok.fenrir.util.DownloadWorkUtils.makeDownloadRequestAudio
import dev.ragnarok.fenrir.util.FindAtWithContent
import dev.ragnarok.fenrir.util.HelperSimple
import dev.ragnarok.fenrir.util.HelperSimple.hasHelp
import dev.ragnarok.fenrir.util.Mp3InfoHelper.getLength
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.Utils.safeCheck
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CompositeJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.delayTaskFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.hiddenIO
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.sharedFlowToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toMain
import dev.ragnarok.fenrir.util.hls.M3U8
import kotlinx.coroutines.flow.Flow

class AudiosPresenter(
    accountId: Long,
    private val ownerId: Long,
    private val playlistId: Int?,
    private val accessKey: String?,
    private val iSSelectMode: Boolean,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<IAudiosView>(accountId, savedInstanceState) {
    private val audioInteractor: IAudioInteractor = InteractorFactory.audio
    private val audios: ArrayList<Audio> = ArrayList()
    private val searcher: FindAudio = FindAudio(compositeJob)
    private var sleepDataJob = CancelableJob()
    private var serviceStatusJob = CancelableJob()
    private var actualReceived = false
    private var currentPlaylist: MutableList<AudioPlaylist>? = null
    private var loadingNow = false
    private var endOfContent = false
    private var doAudioLoadTabs = false
    private var needDeadHelper: Boolean

    private var tmpDownloadAudio: Audio? = null
    private var tmpDownloadPos = -1
    private var tmpMoveFromAudio: Audio? = null
    private var tmpMoveFromPosition = -1
    private var tmpMoveToPosition = -1
    private fun loadedPlaylist(t: AudioPlaylist) {
        val ret: MutableList<AudioPlaylist> = ArrayList(1)
        ret.add(t)
        view?.updatePlaylists(ret)
        currentPlaylist = ret
    }

    val isMyAudio: Boolean
        get() = playlistId == null && ownerId == accountId
    val isNotSearch: Boolean
        get() = !searcher.isSearchMode

    internal fun setLoadingNow(loadingNow: Boolean) {
        this.loadingNow = loadingNow
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
        if (audios.isEmpty()) {
            if (!iSSelectMode && playlistId == null) {
                appendJob(
                    Includes.stores.tempStore().getAudiosAll(ownerId)
                        .fromIOToMain({
                            if (it.isEmpty()) {
                                fireRefresh()
                            } else {
                                audios.addAll(it)
                                actualReceived = true
                                setLoadingNow(false)
                                view?.notifyListChanged()
                            }
                        }, {
                            fireRefresh()
                        })
                )
            } else fireRefresh()
        }
    }

    private fun resolveRefreshingView() {
        resumedView?.displayRefreshing(loadingNow)
    }

    private fun requestNext() {
        setLoadingNow(true)
        val offset = audios.size
        requestList(offset)
    }

    private fun requestList(offset: Int) {
        setLoadingNow(true)
        appendJob(
            audioInteractor.get(accountId, playlistId, ownerId, offset, GET_COUNT, accessKey)
                .fromIOToMain({
                    onListReceived(
                        offset,
                        it
                    )
                }) { t -> onListGetError(t) })
    }

    private fun onListReceived(offset: Int, data: List<Audio>) {
        endOfContent = data.isEmpty()
        actualReceived = true
        if (playlistId == null && !iSSelectMode) {
            appendJob(
                Includes.stores.tempStore().addAudios(ownerId, data, offset == 0)
                    .hiddenIO()
            )
        }
        if (offset == 0) {
            audios.clear()
            audios.addAll(data)
            view?.notifyListChanged()
        } else {
            val startOwnSize = audios.size
            audios.addAll(data)
            view?.notifyDataAdded(
                startOwnSize,
                data.size
            )
        }
        setLoadingNow(false)
        if (needDeadHelper) {
            for (i in audios) {
                if (i.url.isNullOrEmpty() || i.url?.contains("audio_api_unavailable") == true) {
                    needDeadHelper = false
                    view?.showAudioDeadHelper()
                    break
                }
            }
        }
    }

    fun firePlayAudio(context: Context, position: Int) {
        startForPlayList(context, audios, position)
        if (!Settings.get().main().isShow_mini_player) getPlayerPlace(accountId).tryOpenWith(
            context
        )
    }

    override fun onDestroyed() {
        sleepDataJob.cancel()
        serviceStatusJob.cancel()
        super.onDestroyed()
    }

    internal fun onListGetError(t: Throwable) {
        setLoadingNow(false)
        showError(t)
    }

    fun fireSelectAll() {
        for (i in audios) {
            i.isSelected = true
        }
        view?.notifyListChanged()
    }

    private fun getSelected(noDownloaded: Boolean): ArrayList<Audio> {
        val ret = ArrayList<Audio>()
        for (i in audios) {
            if (i.isSelected) {
                if (noDownloaded) {
                    if (TrackIsDownloaded(i) == 0 && i.url
                            .nonNullNoEmpty() && !i.url!!.contains("file://") && !i.url!!.contains("content://")
                    ) {
                        ret.add(i)
                    }
                } else {
                    ret.add(i)
                }
            }
        }
        return ret
    }

    fun fireSelectToSendAttachment() {
        view?.onSelectToSendAttachment(getSelected(false))
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

    fun fireUpdateSelectMode() {
        for (i in audios) {
            if (i.isSelected) {
                i.isSelected = false
            }
        }
        view?.notifyListChanged()
    }

    private fun sleep_search(q: String?) {
        if (loadingNow) return
        sleepDataJob.cancel()
        if (q.isNullOrEmpty()) {
            searcher.cancel()
        } else {
            if (!searcher.isSearchMode) {
                searcher.insertCache(audios, audios.size)
            }
            sleepDataJob += delayTaskFlow(WEB_SEARCH_DELAY.toLong())
                .toMain { searcher.do_search(q) }
        }
    }

    fun fireSearchRequestChanged(q: String?) {
        sleep_search(q?.trim())
    }

    fun fireRefresh() {
        if (searcher.isSearchMode) {
            searcher.reset()
        } else {
            if (playlistId != null && playlistId != 0) {
                appendJob(
                    audioInteractor.getPlaylistById(
                        accountId,
                        playlistId,
                        ownerId,
                        accessKey
                    )
                        .fromIOToMain({ t -> loadedPlaylist(t) }) { t ->
                            showError(
                                t
                            )
                        })
            }
            requestList(0)
        }
    }

    fun onDelete(album: AudioPlaylist) {
        appendJob(
            audioInteractor.deletePlaylist(
                accountId,
                album.id,
                album.owner_id
            )
                .fromIOToMain({
                    view?.customToast?.showToast(
                        R.string.success
                    )
                }) { throwable ->
                    showError(throwable)
                })
    }

    fun onAdd(album: AudioPlaylist) {
        appendJob(
            audioInteractor.followPlaylist(
                accountId,
                album.id,
                album.owner_id,
                album.access_key
            )
                .fromIOToMain({
                    view?.customToast?.showToast(R.string.success)
                }) { throwable ->
                    showError(throwable)
                })
    }

    fun fireScrollToEnd() {
        if (audios.nonNullNoEmpty() && !loadingNow && actualReceived) {
            if (searcher.isSearchMode) {
                searcher.do_search()
            } else if (!endOfContent) {
                requestNext()
            }
        }
    }

    fun fireEditTrack(position: Int, artist: String, title: String?, audio: Audio) {
        loadingNow = true
        resolveRefreshingView()

        appendJob(
            audioInteractor.edit(
                accountId,
                audio.ownerId,
                audio.id,
                artist,
                title
            ).fromIOToMain({
                loadingNow = false
                resolveRefreshingView()
                if (position < audios.size && position >= 0) {
                    audios[position].setTitle(title).setArtist(artist)
                    view?.notifyItemChanged(position)
                }
            }) {
                loadingNow = false
                resolveRefreshingView()

                showError(it)
            })
    }

    fun fireItemMoved(fromPosition: Int, toPosition: Int): Boolean {
        if (audios.size < 2 || fromPosition < 0 || fromPosition >= audios.size || toPosition < 0 || toPosition >= audios.size) {
            return false
        }
        tmpMoveToPosition = toPosition
        val audioFrom = audios[fromPosition]
        audios.removeAt(fromPosition)
        audios.add(toPosition, audioFrom)

        view?.notifyItemMoved(
            fromPosition,
            toPosition
        )
        return true
    }

    fun fireDoFinalMove() {
        val finalMoveFromAudio = tmpMoveFromAudio
        if (audios.size < 2 || finalMoveFromAudio == null || tmpMoveFromPosition == tmpMoveToPosition || tmpMoveFromPosition < 0 || tmpMoveFromPosition >= audios.size || tmpMoveToPosition < 0 || tmpMoveToPosition >= audios.size) {
            tmpMoveFromPosition = -1
            tmpMoveToPosition = -1
            tmpMoveFromAudio = null
            fireRefresh()
            return
        }
        val before = if (tmpMoveToPosition == 0) audios.getOrNull(0 + 1) else null
        val after = if (tmpMoveToPosition == 0) null else audios.getOrNull(tmpMoveToPosition - 1)
        loadingNow = true
        resolveRefreshingView()

        appendJob(
            audioInteractor.reorder(
                accountId,
                ownerId,
                finalMoveFromAudio.id,
                before?.id,
                after?.id
            )
                .fromIOToMain({
                    loadingNow = false
                    resolveRefreshingView()
                    tmpMoveToPosition = -1
                    tmpMoveFromPosition = -1
                    tmpMoveFromAudio = null

                    view?.customToast?.showToastSuccessBottom(R.string.success)
                }, {
                    loadingNow = false
                    resolveRefreshingView()
                    view?.showThrowable(it)
                    fireItemMoved(tmpMoveToPosition, tmpMoveFromPosition)
                    tmpMoveToPosition = -1
                    tmpMoveFromPosition = -1
                    tmpMoveFromAudio = null
                })
        )
    }

    override fun onGuiCreated(viewHost: IAudiosView) {
        super.onGuiCreated(viewHost)
        viewHost.displayList(audios)
        currentPlaylist?.let {
            viewHost.updatePlaylists(it)
        }
    }

    fun fireOpenMenu(position: Int, audio: Audio) {
        if (position < audios.size && position >= 0) {
            view?.onOpenMenu(accountId, position, audio, playlistId)
        }
    }

    fun fireDelete(position: Int, audio: Audio) {
        loadingNow = true
        resolveRefreshingView()

        appendJob(
            if (playlistId == null || playlistId == 0) {
                audioInteractor.delete(accountId, audio.id, audio.ownerId).fromIOToMain(
                    {
                        loadingNow = false
                        resolveRefreshingView()

                        if (position < audios.size && position >= 0) {
                            audios.removeAt(position)
                            view?.notifyItemRemoved(position)
                        }

                        view?.customToast?.showToastSuccessBottom(R.string.deleted)
                    }) {
                    loadingNow = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                }
            } else {
                audioInteractor.removeFromPlaylist(
                    accountId, audio.ownerId, playlistId, listOf(
                        AccessIdPair(
                            audio.id, audio.ownerId, audio.accessKey
                        )
                    )
                ).fromIOToMain(
                    {
                        loadingNow = false
                        resolveRefreshingView()

                        if (it != 0) {
                            audios.removeAt(position)
                            view?.notifyItemRemoved(position)
                            view?.customToast?.showToastSuccessBottom(R.string.deleted)
                        } else {
                            view?.customToast?.showToastSuccessBottom(R.string.error)
                        }
                    }) {
                    loadingNow = false
                    resolveRefreshingView()
                    view?.showThrowable(it)
                }
            })
    }

    fun fireAddTrack(audio: Audio) {
        loadingNow = true
        resolveRefreshingView()

        appendJob(
            audioInteractor.add(accountId, audio, null).fromIOToMain(
                {
                    loadingNow = false
                    resolveRefreshingView()
                    view?.customToast?.showToastSuccessBottom(R.string.added)
                }) {
                loadingNow = false
                resolveRefreshingView()
                view?.showThrowable(it)
            })
    }

    fun fireGetBitrate(audio: Audio) {
        loadingNow = true
        resolveRefreshingView()

        if (audio.needRefresh()) {
            appendJob(
                audioInteractor.getById(
                    accountId,
                    listOf(audio)
                ).fromIOToMain({
                    getBitrateTask(it[0])
                }) {
                    getBitrateTask(
                        audio
                    )
                })
        } else {
            getBitrateTask(audio)
        }
    }

    private fun getBitrateTask(audio: Audio) {
        val pUrl = audio.url
        if (pUrl.isNullOrEmpty()) {
            loadingNow = false
            resolveRefreshingView()
            return
        }
        appendJob(
            if (audio.isHLS) {
                M3U8(pUrl).length.fromIOToMain(
                    {
                        loadingNow = false
                        resolveRefreshingView()

                        view?.onShowBitrate(audio.duration, it)
                    }
                ) {
                    loadingNow = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                }
            } else {
                getLength(pUrl).fromIOToMain(
                    {
                        loadingNow = false
                        resolveRefreshingView()

                        view?.onShowBitrate(audio.duration, it)
                    }
                ) {
                    loadingNow = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                }
            })
    }

    fun fireGetLyrics(audio: Audio) {
        loadingNow = true
        resolveRefreshingView()

        appendJob(
            audioInteractor.getLyrics(accountId, audio)
                .fromIOToMain({
                    loadingNow = false
                    resolveRefreshingView()

                    view?.onAudioLyricsReceived(
                        it,
                        audio
                    )
                }) {
                    loadingNow = false
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

    fun fireSharePlaylistClick(playlist: AudioPlaylist) {
        view?.onSharePlaylist(accountId, playlist)
    }

    fun fireMultipleDownload(context: Context) {
        val tracks: List<Audio> = getSelected(true)
        fireUpdateSelectMode()
        if (tracks.isNotEmpty()) {
            CheckDirectory(Settings.get().main().musicDir)
            var obj = WorkManager.getInstance(context).beginWith(
                makeDownloadRequestAudio(
                    tracks[0], accountId
                )
            )
            var indexAudio = audios.indexOf(tracks[0])
            if (indexAudio >= 0) {
                audios[indexAudio].downloadIndicator = 1
                view?.notifyItemChanged(indexAudio)
            }
            if (tracks.size > 1) {
                val requests: MutableList<OneTimeWorkRequest> =
                    ArrayList(tracks.size - 1)
                var isFirst = true
                for (i in tracks) {
                    if (isFirst) {
                        isFirst = false
                        continue
                    }
                    requests.add(makeDownloadRequestAudio(i, accountId))

                    indexAudio = audios.indexOf(i)
                    if (indexAudio >= 0) {
                        audios[indexAudio].downloadIndicator = 1
                        view?.notifyItemChanged(indexAudio)
                    }
                }
                obj = obj.then(requests)
            }
            obj.enqueue()
        }
    }

    fun fireStoreMoveFromPosition(position: Int?) {
        tmpMoveFromPosition = position ?: -1
        tmpMoveFromAudio = audios.getOrNull(tmpMoveFromPosition)
    }

    private inner class FindAudio(disposable: CompositeJob) : FindAtWithContent<Audio>(
        disposable, SEARCH_VIEW_COUNT, SEARCH_COUNT
    ) {
        override fun search(offset: Int, count: Int): Flow<List<Audio>> {
            return audioInteractor.get(accountId, playlistId, ownerId, offset, count, accessKey)
        }

        override fun onError(e: Throwable) {
            onListGetError(e)
        }

        override fun onResult(data: MutableList<Audio>) {
            actualReceived = true
            val startSize = audios.size
            audios.addAll(data)
            view?.notifyDataAdded(
                startSize,
                data.size
            )
        }

        override fun updateLoading(loading: Boolean) {
            setLoadingNow(loading)
        }

        override fun clean() {
            audios.clear()
            view?.notifyListChanged()
        }

        private fun checkArtists(data: Map<String, String>?, q: String): Boolean {
            if (data.isNullOrEmpty()) {
                return false
            }
            val locale = Utils.appLocale
            for (i in data.values) {
                if (i.lowercase(locale)
                        .contains(q.lowercase(locale))
                ) {
                    return true
                }
            }
            return false
        }

        private fun checkTitleArtists(data: Audio, q: String): Boolean {
            val r = q.split(Regex("( - )|( )|( {2})"), 2).toTypedArray()
            return r.size >= 2 && (safeCheck(
                data.artist
            ) {
                data.artist?.lowercase(Utils.appLocale)?.contains(
                    r[0].lowercase(
                        Utils.appLocale
                    )
                ) == true
            }
                    || checkArtists(
                data.main_artists,
                r[0]
            )) && safeCheck(
                data.title
            ) {
                data.title?.lowercase(Utils.appLocale)?.contains(
                    r[1].lowercase(
                        Utils.appLocale
                    )
                ) == true
            }
        }

        override fun compare(data: Audio, q: String): Boolean {
            return if (q == "dw") {
                TrackIsDownloaded(data) == 0
            } else safeCheck(
                data.title
            ) {
                data.title?.lowercase(Utils.appLocale)
                    ?.contains(q.lowercase(Utils.appLocale)) == true
            }
                    || safeCheck(
                data.artist
            ) {
                data.artist?.lowercase(Utils.appLocale)?.contains(
                    q.lowercase(
                        Utils.appLocale
                    )
                ) == true
            }
                    || checkArtists(data.main_artists, q) || checkTitleArtists(data, q)
        }

        override fun onReset(data: MutableList<Audio>, offset: Int, isEnd: Boolean) {
            if (data.isEmpty()) {
                fireRefresh()
            } else {
                endOfContent = isEnd
                audios.clear()
                audios.addAll(data)
                if (playlistId == null && !iSSelectMode) {
                    appendJob(
                        Includes.stores.tempStore().addAudios(ownerId, data, true)
                            .hiddenIO()
                    )
                }
                view?.notifyListChanged()
            }
        }
    }

    companion object {
        private const val GET_COUNT = 100
        private const val SEARCH_COUNT = 1000
        private const val SEARCH_VIEW_COUNT = 20
        private const val WEB_SEARCH_DELAY = 1000
    }

    init {
        needDeadHelper = hasHelp(HelperSimple.AUDIO_DEAD, 1)
        serviceStatusJob += observeServiceBinding()
            .sharedFlowToMain {
                view?.onServiceBindEvent(it)
            }
    }
}