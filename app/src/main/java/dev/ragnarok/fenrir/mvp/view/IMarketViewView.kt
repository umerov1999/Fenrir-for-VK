package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Market
import dev.ragnarok.fenrir.model.Peer
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IMarketViewView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayLoading(loading: Boolean)
    fun displayMarket(market: Market, accountId: Int)
    fun sendMarket(accountId: Int, market: Market)
    fun onWriteToMarketer(accountId: Int, market: Market, peer: Peer)
}