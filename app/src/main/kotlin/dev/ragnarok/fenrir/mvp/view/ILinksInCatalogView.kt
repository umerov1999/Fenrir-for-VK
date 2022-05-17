package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Link
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface ILinksInCatalogView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayList(links: MutableList<Link>)
    fun notifyListChanged()
    fun displayRefreshing(refresing: Boolean)
}