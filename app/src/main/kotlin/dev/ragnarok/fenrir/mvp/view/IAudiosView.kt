package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.AudioPlaylist
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IAudiosView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayList(audios: MutableList<Audio>)
    fun notifyListChanged()
    fun notifyItemMoved(fromPosition: Int, toPosition: Int)
    fun notifyItemRemoved(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyItemChanged(index: Int)
    fun displayRefreshing(refreshing: Boolean)
    fun updatePlaylists(playlist: MutableList<AudioPlaylist>)
    fun showAudioDeadHelper()
}