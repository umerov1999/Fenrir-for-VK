package dev.ragnarok.fenrir.fragment.audio.audiosrecommendation

import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.fragment.base.core.IToastView
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.model.Audio

interface IAudiosRecommendationView : IMvpView, IErrorView, IToastView {
    fun displayList(audios: MutableList<Audio>)
    fun notifyListChanged()
    fun notifyItemRemoved(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyItemChanged(index: Int)
    fun displayRefreshing(refreshing: Boolean)
    fun onOpenMenu(accountId: Long, position: Int, audio: Audio)
    fun onAudioLyricsReceived(lyricText: String, audio: Audio)
    fun onDownloadAudioNextStage()
    fun onStartAudioDownload(accountId: Long, position: Int, audio: Audio)
    fun onShowBitrate(duration: Int, size: Long)
    fun onSelectToSendAttachment(audios: ArrayList<Audio>)
    fun onServiceBindEvent(@PlayerStatus status: Int)
}