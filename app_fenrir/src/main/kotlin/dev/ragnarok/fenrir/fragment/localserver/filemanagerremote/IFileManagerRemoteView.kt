package dev.ragnarok.fenrir.fragment.localserver.filemanagerremote

import android.os.Parcelable
import androidx.annotation.StringRes
import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.FileRemote
import dev.ragnarok.fenrir.model.Video

interface IFileManagerRemoteView : IMvpView, IErrorView {
    fun displayData(items: ArrayList<FileRemote>)
    fun resolveEmptyText(visible: Boolean)
    fun resolveLoading(visible: Boolean)
    fun onError(throwable: Throwable)
    fun notifyAllChanged()
    fun notifyItemRemoved(index: Int)
    fun updatePathString(file: String?)
    fun restoreScroll(scroll: Parcelable, appVerticalOffset: Int)

    fun onScrollTo(pos: Int)
    fun notifyItemChanged(pos: Int)
    fun showMessage(@StringRes res: Int)
    fun displayGalleryUnSafe(
        accountId: Long,
        parcelNativePointer: Long,
        position: Int,
        reversed: Boolean
    )

    fun displayVideo(video: Video)
    fun startPlayAudios(accountId: Long, audios: ArrayList<Audio>, position: Int)

    fun doAudioMenu(accountId: Long, position: Int, audio: FileRemote)
    fun doFileMenu(accountId: Long, position: Int, file: FileRemote)
    fun onServiceBindEvent(@PlayerStatus status: Int)
    fun onDownloadAudioNextStage()
    fun onStartAudioDownload(accountId: Long, position: Int, audio: Audio)
    fun showSuccess()
    fun displayEditFileNameDialog(hash: String, fileName: String)
}