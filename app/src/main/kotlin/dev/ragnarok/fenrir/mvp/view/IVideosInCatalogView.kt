package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IVideosInCatalogView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayList(videos: List<Video>)
    fun notifyListChanged()
    fun displayRefreshing(refresing: Boolean)
}