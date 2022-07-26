package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface ICommunityLinksView : IAccountDependencyView, IErrorView, IMvpView {
    fun displayRefreshing(loadingNow: Boolean)
    fun notifyDataSetChanged()
    fun displayData(links: List<VKApiCommunity.Link>)
    fun openLink(link: String?)
}