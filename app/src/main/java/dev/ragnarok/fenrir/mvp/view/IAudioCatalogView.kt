package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.AudioCatalog
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IAudioCatalogView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(pages: List<AudioCatalog>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
}