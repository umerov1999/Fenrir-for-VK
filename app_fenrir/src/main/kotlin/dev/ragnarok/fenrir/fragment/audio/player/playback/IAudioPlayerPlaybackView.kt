package dev.ragnarok.fenrir.fragment.audio.player.playback

import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.fragment.base.core.IToastView
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.model.Audio

interface IAudioPlayerPlaybackView : IMvpView, IErrorView, IToastView {
    fun displayList(audios: MutableList<Audio>, currentPos: Int)

    fun notifyListChanged()
    fun notifyItemMoved(fromPosition: Int, toPosition: Int)
    fun notifyItemRemoved(index: Int)
    fun notifyItemChanged(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun onStartForPlayList(audios: ArrayList<Audio>, position: Int)
    fun onOpenMenu(accountId: Long, position: Int, audio: Audio)
    fun onAudioLyricsReceived(lyricText: String, audio: Audio)
    fun onDownloadAudioNextStage()
    fun onStartAudioDownload(accountId: Long, position: Int, audio: Audio)
    fun onShowBitrate(duration: Int, size: Long)
    fun onSelectToSendAttachment(audios: ArrayList<Audio>)
    fun onServiceBindEvent(@PlayerStatus status: Int)
}