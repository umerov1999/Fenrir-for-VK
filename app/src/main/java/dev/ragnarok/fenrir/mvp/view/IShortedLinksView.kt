package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.ShortLink
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IShortedLinksView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(links: List<ShortLink>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun updateLink(url: String?)
    fun showLinkStatus(status: String?)
}