package dev.ragnarok.fenrir.fragment.messages.conversationattachments.conversationaudios

import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.VKApiAudio
import dev.ragnarok.fenrir.api.model.interfaces.VKApiAttachment
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.interfaces.IAudioInteractor
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.fragment.messages.conversationattachments.abschatattachments.BaseChatAttachmentsPresenter
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.observeServiceBinding
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Mp3InfoHelper.getLength
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.sharedFlowToMain
import dev.ragnarok.fenrir.util.hls.M3U8
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatAttachmentAudioPresenter(peerId: Long, accountId: Long, savedInstanceState: Bundle?) :
    BaseChatAttachmentsPresenter<Audio, IChatAttachmentAudiosView>(
        peerId,
        accountId,
        savedInstanceState
    ) {
    private val audioInteractor: IAudioInteractor = InteractorFactory.audio
    private var serviceStatusJob = CancelableJob()
    private var tmpDownloadAudio: Audio? = null
    private var tmpDownloadPos = -1

    override fun onDataChanged() {
        super.onDataChanged()
        resolveToolbar()
    }

    override fun requestAttachments(
        peerId: Long,
        nextFrom: String?
    ): Flow<Pair<String?, List<Audio>>> {
        return messageRepository.getHistoryAttachments(
            accountId = accountId,
            peerId = peerId,
            mediaType = VKApiAttachment.TYPE_AUDIO,
            startFrom = nextFrom,
            count = 50,
            photoSizes = true,
            preserveOrder = true,
            maxForwardsLevel = 45
        )
            .map { response ->
                val audios: MutableList<Audio> = ArrayList(
                    Utils.safeCountOf(response.items)
                )
                response.items.nonNullNoEmpty {
                    for (one in it) {
                        if (one.entry != null && one.entry?.attachment is VKApiAudio) {
                            val dto = one.entry?.attachment as VKApiAudio
                            audios.add(Dto2Model.transform(dto))
                        }
                    }
                }
                Pair(response.next_from, audios)
            }
    }

    fun firePlayAudio(position: Int) {
        fireAudioPlayClick(position, ArrayList(data))
    }

    override fun onGuiCreated(viewHost: IChatAttachmentAudiosView) {
        super.onGuiCreated(viewHost)
        resolveToolbar()
    }

    override fun onDestroyed() {
        serviceStatusJob.cancel()
        super.onDestroyed()
    }

    private fun resolveToolbar() {
        view?.setToolbarTitleString(getString(R.string.attachments_in_chat))
        view?.setToolbarTitleString(getString(R.string.audios_count, Utils.safeCountOf(data)))
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

                if (position < data.size && position >= 0) {
                    data[position].setTitle(title).setArtist(artist)
                    view?.notifyItemChanged(position)
                }
            }) {
                showError(it)
            })
    }

    fun fireOpenMenu(position: Int, audio: Audio) {
        if (position < data.size && position >= 0) {
            view?.onOpenMenu(accountId, position, audio)
        }
    }

    fun fireDelete(position: Int, audio: Audio) {
        appendJob(
            audioInteractor.delete(accountId, audio.id, audio.ownerId).fromIOToMain(
                {
                    if (position < data.size && position >= 0) {
                        data.removeAt(position)
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