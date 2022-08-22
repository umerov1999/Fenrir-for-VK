package dev.ragnarok.fenrir.fragment.audio.catalog_v1.videosincatalog

import dev.ragnarok.fenrir.fragment.base.IAccountDependencyView
import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.model.Video

interface IVideosInCatalogView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayList(videos: List<Video>)
    fun notifyListChanged()
    fun displayRefreshing(refresing: Boolean)
}