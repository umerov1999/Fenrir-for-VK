package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView
import dev.ragnarok.fenrir.upload.Upload

interface IAudiosLocalView : IMvpView, IErrorView, IToastView, IAccountDependencyView {
    fun displayList(audios: List<Audio>)
    fun notifyItemChanged(index: Int)
    fun notifyItemRemoved(index: Int)
    fun notifyListChanged()
    fun notifyUploadItemsAdded(position: Int, count: Int)
    fun notifyUploadItemRemoved(position: Int)
    fun notifyUploadItemChanged(position: Int)
    fun notifyUploadProgressChanged(position: Int, progress: Int, smoothly: Boolean)
    fun setUploadDataVisible(visible: Boolean)
    fun displayUploads(data: List<Upload>)
    fun displayRefreshing(refreshing: Boolean)
    fun checkPermission()
}