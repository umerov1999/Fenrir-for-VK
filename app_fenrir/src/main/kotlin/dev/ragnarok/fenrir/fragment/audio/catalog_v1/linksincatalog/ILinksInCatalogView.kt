package dev.ragnarok.fenrir.fragment.audio.catalog_v1.linksincatalog

import dev.ragnarok.fenrir.fragment.base.IAccountDependencyView
import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.model.Link

interface ILinksInCatalogView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayList(links: MutableList<Link>)
    fun notifyListChanged()
    fun displayRefreshing(refresing: Boolean)
}