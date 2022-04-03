package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IVideosLocalServerView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayList(videos: List<Video>)
    fun notifyListChanged()
    fun notifyItemChanged(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun displayLoading(loading: Boolean)
}