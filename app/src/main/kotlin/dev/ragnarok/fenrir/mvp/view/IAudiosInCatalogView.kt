package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IAudiosInCatalogView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayList(audios: MutableList<Audio>)
    fun notifyListChanged()
    fun notifyItemChanged(index: Int)
    fun displayRefreshing(refresing: Boolean)
}