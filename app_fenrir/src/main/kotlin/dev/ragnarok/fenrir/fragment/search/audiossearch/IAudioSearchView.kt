package dev.ragnarok.fenrir.fragment.search.audiossearch

import dev.ragnarok.fenrir.fragment.search.abssearch.IBaseSearchView
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.model.Audio

interface IAudioSearchView : IBaseSearchView<Audio> {
    fun notifyAudioChanged(index: Int)
    fun notifyAudioRemoved(index: Int)

    fun onOpenMenu(accountId: Long, position: Int, audio: Audio)
    fun onAudioLyricsReceived(lyricText: String, audio: Audio)
    fun onDownloadAudioNextStage()
    fun onStartAudioDownload(accountId: Long, position: Int, audio: Audio)
    fun onShowBitrate(duration: Int, size: Long)
    fun onSelectToSendAttachment(audios: ArrayList<Audio>)
    fun onServiceBindEvent(@PlayerStatus status: Int)
}