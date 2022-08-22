package dev.ragnarok.fenrir.fragment.audio.catalog_v1.audiosincatalog

import dev.ragnarok.fenrir.fragment.base.IAccountDependencyView
import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.model.Audio

interface IAudiosInCatalogView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayList(audios: MutableList<Audio>)
    fun notifyListChanged()
    fun notifyItemChanged(index: Int)
    fun displayRefreshing(refresing: Boolean)
}