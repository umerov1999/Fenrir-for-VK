package dev.ragnarok.fenrir.mvp.view.wallattachments

import dev.ragnarok.fenrir.model.Link
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.IAttachmentsPlacesView
import dev.ragnarok.fenrir.mvp.view.IErrorView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IWallLinksAttachmentsView : IAccountDependencyView, IMvpView, IErrorView,
    IAttachmentsPlacesView {
    fun displayData(links: MutableList<Link>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun toolbarTitle(title: String)
    fun toolbarSubtitle(subtitle: String)
    fun onSetLoadingStatus(isLoad: Int)
}