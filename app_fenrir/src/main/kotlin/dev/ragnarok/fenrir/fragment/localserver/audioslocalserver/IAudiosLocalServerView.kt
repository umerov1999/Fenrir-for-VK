package dev.ragnarok.fenrir.fragment.localserver.audioslocalserver

import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.model.Audio

interface IAudiosLocalServerView : IMvpView, IErrorView {
    fun displayList(audios: List<Audio>)
    fun notifyListChanged()
    fun notifyItemChanged(index: Int)
    fun notifyItemRemoved(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun displayLoading(loading: Boolean)
    fun displayOptionsDialog(accountId: Long, isReverse: Boolean, isDiscography: Boolean)

    fun onOpenMenu(accountId: Long, position: Int, audio: Audio)
    fun onDownloadAudioNextStage()
    fun onStartAudioDownload(accountId: Long, position: Int, audio: Audio)
    fun onShowBitrate(bitrate: Int, size: Int)
    fun onServiceBindEvent(@PlayerStatus status: Int)
    fun showSuccess()
    fun displayEditFileNameDialog(hash: String, fileName: String)
}