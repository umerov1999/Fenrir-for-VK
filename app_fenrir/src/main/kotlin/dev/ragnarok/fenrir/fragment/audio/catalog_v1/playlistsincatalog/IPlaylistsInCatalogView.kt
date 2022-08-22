package dev.ragnarok.fenrir.fragment.audio.catalog_v1.playlistsincatalog

import dev.ragnarok.fenrir.fragment.base.IAccountDependencyView
import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.fragment.base.core.IToastView
import dev.ragnarok.fenrir.model.AudioPlaylist

interface IPlaylistsInCatalogView : IMvpView, IErrorView, IToastView, IAccountDependencyView {
    fun displayList(audios: List<AudioPlaylist>)
    fun notifyListChanged()
    fun displayRefreshing(refresing: Boolean)
}