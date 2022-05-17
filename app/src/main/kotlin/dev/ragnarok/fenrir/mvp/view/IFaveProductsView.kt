package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Market
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IFaveProductsView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(markets: List<Market>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun onMarketOpen(accountId: Int, market: Market)
}