package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.AudioPlaylist
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IAudioPlaylistsView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(pages: List<AudioPlaylist>)
    fun notifyDataSetChanged()
    fun notifyItemRemoved(position: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun doAddAudios(accountId: Int)
    fun showHelper()
}