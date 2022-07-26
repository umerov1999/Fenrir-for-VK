package dev.ragnarok.filegallery.mvp.view

import android.os.Parcelable
import androidx.annotation.StringRes
import dev.ragnarok.filegallery.model.Audio
import dev.ragnarok.filegallery.model.FileRemote
import dev.ragnarok.filegallery.model.Video
import dev.ragnarok.filegallery.mvp.core.IMvpView

interface IFileManagerRemoteView : IMvpView, IErrorView {
    fun displayData(items: ArrayList<FileRemote>)
    fun resolveEmptyText(visible: Boolean)
    fun resolveLoading(visible: Boolean)
    fun notifyAllChanged()
    fun updatePathString(file: String?)
    fun restoreScroll(scroll: Parcelable)

    fun onScrollTo(pos: Int)
    fun notifyItemChanged(pos: Int)
    fun showMessage(@StringRes res: Int)
    fun displayGalleryUnSafe(parcelNativePointer: Long, position: Int, reversed: Boolean)
    fun displayVideo(video: Video)
    fun startPlayAudios(audios: ArrayList<Audio>, position: Int)
}
