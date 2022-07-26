package dev.ragnarok.filegallery.mvp.view

import dev.ragnarok.filegallery.model.Video
import dev.ragnarok.filegallery.mvp.core.IMvpView

interface IVideosLocalServerView : IMvpView, IErrorView {
    fun displayList(videos: List<Video>)
    fun notifyListChanged()
    fun notifyItemChanged(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun displayLoading(loading: Boolean)
}