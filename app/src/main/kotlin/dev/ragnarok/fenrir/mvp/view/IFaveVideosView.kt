package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IFaveVideosView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(videos: List<Video>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun goToPreview(accountId: Int, video: Video)
}