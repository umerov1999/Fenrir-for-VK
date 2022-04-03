package dev.ragnarok.fenrir.mvp.view

import android.os.Parcelable
import androidx.annotation.StringRes
import dev.ragnarok.fenrir.model.FileItem
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface IFileManagerSelectView : IMvpView, IErrorView {
    fun displayData(items: ArrayList<FileItem>)
    fun resolveEmptyText(visible: Boolean)
    fun resolveLoading(visible: Boolean)
    fun onError(throwable: Throwable)
    fun notifyAllChanged()
    fun updatePathString(file: String)
    fun restoreScroll(scroll: Parcelable)

    fun onScrollTo(pos: Int)
    fun notifyItemChanged(pos: Int)
    fun showMessage(@StringRes res: Int)

    fun updateSelectVisibility(visible: Boolean)
    fun updateHeader(ext: String?)
}
