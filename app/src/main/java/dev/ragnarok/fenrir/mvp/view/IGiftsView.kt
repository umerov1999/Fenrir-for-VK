package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Gift
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IGiftsView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(gifts: List<Gift>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun onOpenWall(accountId: Int, ownerId: Int)
}