package dev.ragnarok.fenrir.fragment.audio.audioplaylists

import dev.ragnarok.fenrir.fragment.base.IAccountDependencyView
import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.fragment.base.core.IToastView
import dev.ragnarok.fenrir.model.AudioPlaylist

interface IAudioPlaylistsView : IAccountDependencyView, IMvpView, IErrorView, IToastView {
    fun displayData(pages: List<AudioPlaylist>)
    fun notifyDataSetChanged()
    fun notifyItemRemoved(position: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun doAddAudios(accountId: Int)
    fun showHelper()
}