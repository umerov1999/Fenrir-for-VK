package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Market
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IProductsView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(market: List<Market>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun onOpenMarket(accountId: Int, market: Market)
}