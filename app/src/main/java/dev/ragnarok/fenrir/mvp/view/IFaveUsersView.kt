package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.FavePage
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IFaveUsersView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(pages: List<FavePage>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun openOwnerWall(accountId: Int, owner: Owner)
    fun openMention(accountId: Int, owner: Owner)
    fun notifyItemRemoved(index: Int)
}