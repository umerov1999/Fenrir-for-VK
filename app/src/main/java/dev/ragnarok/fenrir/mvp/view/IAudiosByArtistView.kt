package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IAudiosByArtistView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayList(audios: MutableList<Audio>)
    fun notifyListChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyItemChanged(index: Int)
    fun displayRefreshing(refreshing: Boolean)
}