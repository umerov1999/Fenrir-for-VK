package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.AudioPlaylist
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IPlaylistsInCatalogView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayList(audios: List<AudioPlaylist>)
    fun notifyListChanged()
    fun displayRefreshing(refresing: Boolean)
}