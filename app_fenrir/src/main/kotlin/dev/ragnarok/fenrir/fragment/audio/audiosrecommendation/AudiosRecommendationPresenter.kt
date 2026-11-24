package dev.ragnarok.fenrir.fragment.audio.audiosrecommendation

import android.content.Context
import android.os.Bundle
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.interfaces.IAudioInteractor
import dev.ragnarok.fenrir.fragment.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.observeServiceBinding
import dev.ragnarok.fenrir.media.music.MusicPlaybackService.Companion.startForPlayList
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.DownloadWorkUtils.CheckDirectory
import dev.ragnarok.fenrir.util.DownloadWorkUtils.TrackIsDownloaded
import dev.ragnarok.fenrir.util.DownloadWorkUtils.makeDownloadRequestAudio
import dev.ragnarok.fenrir.util.Mp3InfoHelper.getLength
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.sharedFlowToMain
import dev.ragnarok.fenrir.util.hls.M3U8

class AudiosRecommendationPresenter(
    accountId: Long,
    private val ownerId: Long,
    private val top: Boolean,
    private val option_menu_id: Int,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<IAudiosRecommendationView>(accountId, savedInstanceState) {
    private val audioInteractor: IAudioInteractor = InteractorFactory.audio
    private val audios: ArrayList<Audio> = ArrayList()
    private var serviceStatusJob = CancelableJob()
    private var loadingNow = false
    private var doAudioLoadTabs = false

    private var tmpDownloadAudio: Audio? = null
    private var tmpDownloadPos = -1
    private fun setLoadingNow(loadingNow: Boolean) {
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
        fireRefresh()
    }

    private fun resolveRefreshingView() {
        resumedView?.displayRefreshing(
            loadingNow
        )
    }

    private fun onEndlessListReceived(data: List<Audio>) {
        audios.clear()
        audios.addAll(data)
        setLoadingNow(false)
        view?.notifyListChanged()
    }

    fun firePlayAudio(context: Context, position: Int) {
        startForPlayList(context, audios, position)
        if (!Settings.get().main().isShow_mini_player) getPlayerPlace(accountId).tryOpenWith(
            context
        )
    }

    fun fireDelete(position: Int) {
        audios.removeAt(position)
        view?.notifyItemRemoved(
            position
        )
    }

    private fun getListByGenre(foreign: Boolean, genre: Int) {
        setLoadingNow(true)
        appendJob(
            audioInteractor.getPopular(
                accountId,
                if (foreign) 1 else 0,
                genre,
                REC_COUNT
            )
                .fromIOToMain({ onEndlessListReceived(it) }) { t ->
                    onListGetError(
                        t
                    )
                })
    }

    private fun getRecommendations() {
        setLoadingNow(true)
        appendJob(
            if (option_menu_id != 0) {
                audioInteractor.getRecommendationsByAudio(
                    accountId,
                    ownerId.toString() + "_" + option_menu_id,
                    REC_COUNT
                )
                    .fromIOToMain({ onEndlessListReceived(it) }) {
                        onListGetError(
                            it
                        )
                    }
            } else {
                audioInteractor.getRecommendations(
                    accountId,
                    ownerId,
                    REC_COUNT
                )
                    .fromIOToMain({ onEndlessListReceived(it) }) { t ->
                        onListGetError(
                            t
                        )
                    }
            })
    }

    override fun onDestroyed() {
        serviceStatusJob.cancel()
        super.onDestroyed()
    }

    private fun onListGetError(t: Throwable) {
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

    fun fireRefresh() {
        if (loadingNow) {
            return
        }
        if (top) {
            getListByGenre(false, option_menu_id)
        } else {
            getRecommendations()
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

    override fun onGuiCreated(viewHost: IAudiosRecommendationView) {
        super.onGuiCreated(viewHost)
        viewHost.displayList(audios)
    }

    fun fireOpenMenu(position: Int, audio: Audio) {
        if (position < audios.size && position >= 0) {
            view?.onOpenMenu(accountId, position, audio)
        }
    }

    fun fireDelete(position: Int, audio: Audio) {
        loadingNow = true
        resolveRefreshingView()

        appendJob(
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

    init {
        serviceStatusJob += observeServiceBinding()
            .sharedFlowToMain {
                view?.onServiceBindEvent(it)
            }
    }

    companion object {
        private const val REC_COUNT = 1000
    }
}