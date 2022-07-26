package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.FaveLink
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IFaveLinksView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayLinks(links: List<FaveLink>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun displayRefreshing(refreshing: Boolean)
    fun openLink(accountId: Int, link: FaveLink)
    fun notifyItemRemoved(index: Int)
}