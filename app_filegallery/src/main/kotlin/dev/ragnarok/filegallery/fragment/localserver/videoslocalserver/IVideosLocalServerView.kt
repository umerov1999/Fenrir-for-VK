package dev.ragnarok.filegallery.fragment.localserver.videoslocalserver

import dev.ragnarok.filegallery.fragment.base.core.IErrorView
import dev.ragnarok.filegallery.fragment.base.core.IMvpView
import dev.ragnarok.filegallery.model.Video

interface IVideosLocalServerView : IMvpView, IErrorView {
    fun displayList(videos: List<Video>)
    fun notifyListChanged()
    fun notifyItemChanged(index: Int)
    fun notifyItemRemoved(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun displayLoading(loading: Boolean)
    fun doVideoMenu(position: Int, video: Video)
    fun onDownloadVideoNextStage()
    fun onStartVideoDownload(position: Int, video: Video)
    fun showSuccess()
    fun displayEditFileNameDialog(hash: String, fileName: String)
}