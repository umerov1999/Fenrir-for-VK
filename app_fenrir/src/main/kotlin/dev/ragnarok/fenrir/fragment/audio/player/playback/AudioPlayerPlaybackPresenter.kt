package dev.ragnarok.fenrir.fragment.audio.player.playback

import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.interfaces.IAudioInteractor
import dev.ragnarok.fenrir.fragment.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.media.music.MusicPlaybackController
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.observeServiceBinding
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.util.Mp3InfoHelper.getLength
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.sharedFlowToMain
import dev.ragnarok.fenrir.util.hls.M3U8

class AudioPlayerPlaybackPresenter(accountId: Long, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IAudioPlayerPlaybackView>(accountId, savedInstanceState) {

    private val audios: ArrayList<Audio> = ArrayList(MusicPlaybackController.queue)
    private var firstLoading = true
    private var serviceStatusJob = CancelableJob()
    private val audioInteractor: IAudioInteractor = InteractorFactory.audio

    private var tmpDownloadAudio: Audio? = null
    private var tmpDownloadPos = -1

    override fun onGuiCreated(viewHost: IAudioPlayerPlaybackView) {
        super.onGuiCreated(viewHost)
        var currentPosUpdate = -1
        if (firstLoading) {
            firstLoading = false
            val my = MusicPlaybackController.currentAudio
            if (my != null) {
                for ((o, i) in audios.withIndex()) {
                    if (i === my) {
                        currentPosUpdate = o
                        break
                    }
                }
            }
        }
        viewHost.displayList(audios, currentPosUpdate)
    }

    override fun onDestroyed() {
        serviceStatusJob.cancel()
        super.onDestroyed()
    }

    fun fireRefresh() {
        audios.clear()
        audios.addAll(MusicPlaybackController.queue)
        view?.notifyListChanged()
    }

    fun fireEditTrack(position: Int, artist: String, title: String?, audio: Audio) {
        appendJob(
            audioInteractor.edit(
                accountId,
                audio.ownerId,
                audio.id,
                artist,
                title
            ).fromIOToMain({
                if (position < audios.size && position >= 0) {
                    audios[position].setTitle(title).setArtist(artist)
                    view?.notifyItemChanged(position)
                }
            }) {
                showError(it)
            })
    }

    fun firePlayAudio(position: Int) {
        if (position < 0 || position > audios.size - 1) {
            return
        }
        if (MusicPlaybackController.queue == audios) {
            MusicPlaybackController.skip(position)
        } else {
            view?.onStartForPlayList(audios, position)
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

    fun fireOpenMenu(position: Int, audio: Audio) {
        if (position < audios.size && position >= 0) {
            view?.onOpenMenu(accountId, position, audio)
        }
    }

    fun fireDelete(position: Int, audio: Audio) {
        appendJob(
            audioInteractor.delete(accountId, audio.id, audio.ownerId).fromIOToMain(
                {
                    if (position < audios.size && position >= 0) {
                        audios.removeAt(position)
                        view?.notifyItemRemoved(position)
                    }

                    view?.customToast?.showToastSuccessBottom(R.string.deleted)
                }) {
                view?.showThrowable(it)
            })
    }

    fun fireAddTrack(audio: Audio) {
        appendJob(
            audioInteractor.add(accountId, audio, null).fromIOToMain(
                {
                    view?.customToast?.showToastSuccessBottom(R.string.added)
                }) {
                view?.showThrowable(it)
            })
    }

    fun fireGetBitrate(audio: Audio) {
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
            return
        }
        appendJob(
            if (audio.isHLS) {
                M3U8(pUrl).length.fromIOToMain(
                    {
                        view?.onShowBitrate(audio.duration, it)
                    }
                ) {
                    view?.showThrowable(it)
                }
            } else {
                getLength(pUrl).fromIOToMain(
                    {
                        view?.onShowBitrate(audio.duration, it)
                    }
                ) {
                    view?.showThrowable(it)
                }
            })
    }

    fun fireGetLyrics(audio: Audio) {
        appendJob(
            audioInteractor.getLyrics(accountId, audio)
                .fromIOToMain({
                    view?.onAudioLyricsReceived(
                        it,
                        audio
                    )
                }) {
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


    init {
        serviceStatusJob += observeServiceBinding()
            .sharedFlowToMain {
                view?.onServiceBindEvent(it)
            }
    }
}