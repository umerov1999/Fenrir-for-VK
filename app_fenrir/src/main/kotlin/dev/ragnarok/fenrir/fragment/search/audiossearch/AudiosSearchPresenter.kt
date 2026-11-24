package dev.ragnarok.fenrir.fragment.search.audiossearch

import android.content.Context
import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.interfaces.IAudioInteractor
import dev.ragnarok.fenrir.fragment.search.abssearch.AbsSearchPresenter
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom
import dev.ragnarok.fenrir.getParcelableCompat
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.observeServiceBinding
import dev.ragnarok.fenrir.media.music.MusicPlaybackService.Companion.startForPlayList
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.trimmedNonNullNoEmpty
import dev.ragnarok.fenrir.util.Mp3InfoHelper.getLength
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.sharedFlowToMain
import dev.ragnarok.fenrir.util.hls.M3U8
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AudiosSearchPresenter(
    accountId: Long,
    criteria: AudioSearchCriteria?,
    savedInstanceState: Bundle?
) : AbsSearchPresenter<IAudioSearchView, AudioSearchCriteria, Audio, IntNextFrom>(
    accountId,
    criteria,
    savedInstanceState
) {
    private val audioInteractor: IAudioInteractor = InteractorFactory.audio
    private var serviceStatusJob = CancelableJob()
    private var tmpDownloadAudio: Audio? = null
    private var tmpDownloadPos = -1

    override val initialNextFrom: IntNextFrom
        get() = IntNextFrom(0)

    override fun readParcelSaved(savedInstanceState: Bundle, key: String): AudioSearchCriteria? {
        return savedInstanceState.getParcelableCompat(key)
    }

    override fun isAtLast(startFrom: IntNextFrom): Boolean {
        return startFrom.offset == 0
    }

    override fun onSearchError(throwable: Throwable) {
        super.onSearchError(throwable)
        showError(throwable)
    }

    override fun doSearch(
        accountId: Long,
        criteria: AudioSearchCriteria,
        startFrom: IntNextFrom
    ): Flow<Pair<List<Audio>, IntNextFrom>> {
        val nextFrom = IntNextFrom(startFrom.offset + 50)
        return audioInteractor.search(accountId, criteria, startFrom.offset, 50)
            .map { audio -> Pair(audio, nextFrom) }
    }

    fun firePlayAudio(context: Context, position: Int) {
        startForPlayList(context, data as ArrayList<Audio>, position)
        if (!Settings.get().main().isShow_mini_player) getPlayerPlace(
            accountId
        ).tryOpenWith(
            context
        )
    }

    override fun onDestroyed() {
        serviceStatusJob.cancel()
        super.onDestroyed()
    }

    override fun canSearch(criteria: AudioSearchCriteria?): Boolean {
        return criteria?.query.trimmedNonNullNoEmpty()
    }

    override fun instantiateEmptyCriteria(): AudioSearchCriteria {
        return AudioSearchCriteria("", by_artist = false, in_main_page = false)
    }

    private val selected: ArrayList<Audio>
        get() {
            val ret = ArrayList<Audio>()
            for (i in data) {
                if (i.isSelected) ret.add(i)
            }
            return ret
        }

    fun getAudioPos(audio: Audio?): Int {
        if (data.isNotEmpty() && audio != null) {
            for ((pos, i) in data.withIndex()) {
                if (i.id == audio.id && i.ownerId == audio.ownerId) {
                    i.isAnimationNow = true
                    view?.notifyAudioChanged(
                        pos
                    )
                    return pos
                }
            }
        }
        return -1
    }

    fun fireSelectToSendAttachment() {
        view?.onSelectToSendAttachment(selected)
    }

    fun fireEditTrack(position: Int, artist: String, title: String?, audio: Audio) {
        setLoadingNow(true)

        appendJob(
            audioInteractor.edit(
                accountId,
                audio.ownerId,
                audio.id,
                artist,
                title
            ).fromIOToMain({
                setLoadingNow(false)
                if (position < data.size && position >= 0) {
                    data[position].setTitle(title).setArtist(artist)
                    view?.notifyItemChanged(position)
                }
            }) {
                setLoadingNow(false)

                showError(it)
            })
    }

    fun fireOpenMenu(position: Int, audio: Audio) {
        if (position < data.size && position >= 0) {
            view?.onOpenMenu(accountId, position, audio)
        }
    }

    fun fireDelete(position: Int, audio: Audio) {
        setLoadingNow(true)

        appendJob(
            audioInteractor.delete(accountId, audio.id, audio.ownerId).fromIOToMain(
                {
                    setLoadingNow(false)

                    if (position < data.size && position >= 0) {
                        data.removeAt(position)
                        view?.notifyAudioRemoved(position)
                    }

                    view?.customToast?.showToastSuccessBottom(R.string.deleted)
                }) {
                setLoadingNow(false)

                view?.showThrowable(it)
            })
    }

    fun fireAddTrack(audio: Audio) {
        setLoadingNow(true)

        appendJob(
            audioInteractor.add(accountId, audio, null).fromIOToMain(
                {
                    setLoadingNow(false)
                    view?.customToast?.showToastSuccessBottom(R.string.added)
                }) {
                setLoadingNow(false)
                view?.showThrowable(it)
            })
    }

    fun fireGetBitrate(audio: Audio) {
        setLoadingNow(true)

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
            setLoadingNow(false)
            return
        }
        appendJob(
            if (audio.isHLS) {
                M3U8(pUrl).length.fromIOToMain(
                    {
                        setLoadingNow(false)

                        view?.onShowBitrate(audio.duration, it)
                    }
                ) {
                    setLoadingNow(false)

                    view?.showThrowable(it)
                }
            } else {
                getLength(pUrl).fromIOToMain(
                    {
                        setLoadingNow(false)

                        view?.onShowBitrate(audio.duration, it)
                    }
                ) {
                    setLoadingNow(false)

                    view?.showThrowable(it)
                }
            })
    }

    fun fireGetLyrics(audio: Audio) {
        setLoadingNow(true)

        appendJob(
            audioInteractor.getLyrics(accountId, audio)
                .fromIOToMain({
                    setLoadingNow(false)

                    view?.onAudioLyricsReceived(
                        it,
                        audio
                    )
                }) {
                    setLoadingNow(false)

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
        if (position < 0 || position > data.size - 1 || audio == null) {
            return
        }
        view?.onStartAudioDownload(accountId, position, audio)
        tmpDownloadPos = -1
        tmpDownloadAudio = null
    }

    init {
        serviceStatusJob += observeServiceBinding()
            .sharedFlowToMain {
                view?.onServiceBindEvent(it)
            }
    }
}
