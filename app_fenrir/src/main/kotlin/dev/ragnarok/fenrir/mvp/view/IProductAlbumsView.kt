package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.MarketAlbum
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IProductAlbumsView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(market_albums: List<MarketAlbum>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun onMarketAlbumOpen(accountId: Int, market_album: MarketAlbum)
}